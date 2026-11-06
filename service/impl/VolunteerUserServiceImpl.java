package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.volunteer.dto.VolunteerUserBatchDTO;
import com.volunteer.dto.VolunteerUserQueryDTO;
import com.volunteer.dto.VolunteerUserUpdateDTO;
import com.volunteer.entity.VolunteerUser;
import com.volunteer.entity.UserSkill;
import com.volunteer.entity.UserCertificate;
import com.volunteer.entity.SkillCategory;
import com.volunteer.mapper.VolunteerUserMapper;
import com.volunteer.mapper.ServiceRecordMapper;
import com.volunteer.mapper.CheckinMapper;
import com.volunteer.service.VolunteerUserService;
import com.volunteer.service.UserSkillService;
import com.volunteer.service.UserCertificateService;
import com.volunteer.service.SkillCategoryService;
import com.volunteer.utils.JwtUtils;
import com.volunteer.vo.VolunteerStatisticsVO;
import com.volunteer.vo.VolunteerUserDetailVO;
import com.volunteer.vo.VolunteerUserListVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * 志愿者用户Service实现类
 * 
 * @author volunteer-team
 * @since 2025-01-17
 */
@Slf4j
@Service
public class VolunteerUserServiceImpl extends ServiceImpl<VolunteerUserMapper, VolunteerUser> implements VolunteerUserService {

    @Autowired
    private JwtUtils jwtUtils;

    @Resource
    private UserSkillService userSkillService;

    @Resource
    private UserCertificateService userCertificateService;

    @Resource
    private SkillCategoryService skillCategoryService;

    @Resource
    private ServiceRecordMapper serviceRecordMapper;

    @Resource
    private CheckinMapper checkinMapper;

    @Override
    public VolunteerUser findByUsername(String username) {
        QueryWrapper<VolunteerUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        queryWrapper.eq("status", 1);
        return this.getOne(queryWrapper);
    }

    @Override
    public VolunteerUser findByPhone(String phone) {
        QueryWrapper<VolunteerUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone", phone);
        queryWrapper.eq("status", 1);
        return this.getOne(queryWrapper);
    }

    @Override
    public boolean register(VolunteerUser user) {
        // 检查用户名是否已存在
        VolunteerUser existUser = findByUsername(user.getUsername());
        if (existUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查手机号是否已存在
        if (user.getPhone() != null && !user.getPhone().trim().isEmpty()) {
            VolunteerUser existPhoneUser = findByPhone(user.getPhone());
            if (existPhoneUser != null) {
                throw new RuntimeException("手机号已被注册");
            }
        }

        // 检查身份证号是否已存在
        if (user.getIdCard() != null && !user.getIdCard().trim().isEmpty()) {
            QueryWrapper<VolunteerUser> idCardQueryWrapper = new QueryWrapper<>();
            idCardQueryWrapper.eq("id_card", user.getIdCard());
            VolunteerUser existIdCardUser = this.getOne(idCardQueryWrapper);
            if (existIdCardUser != null) {
                throw new RuntimeException("身份证号已被使用");
            }
        }

        // 设置默认状态
        user.setStatus(1);
        
        // 保存用户
        boolean success = this.save(user);
        if (!success) {
            throw new RuntimeException("注册失败，请稍后重试");
        }
        return true;
    }

    @Override
    public String login(String username, String password) {
        try {
            // 查找用户
            VolunteerUser user = findByUsername(username);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }

            // 验证密码（这里暂时不加密，直接比较）
            if (!password.equals(user.getPassword())) {
                throw new RuntimeException("密码错误");
            }

            // 生成JWT Token
            return jwtUtils.generateToken(user.getId(), user.getUsername());
        } catch (Exception e) {
            log.error("用户登录失败：{}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        try {
            VolunteerUser user = this.getById(userId);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            
            // 验证旧密码
            if (!oldPassword.equals(user.getPassword())) {
                throw new RuntimeException("旧密码错误");
            }
            
            // 更新密码
            user.setPassword(newPassword);
            return this.updateById(user);
        } catch (Exception e) {
            log.error("修改密码失败：{}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Override
    public Page<VolunteerUserListVO> getVolunteerList(VolunteerUserQueryDTO query) {
        Page<VolunteerUserListVO> page = new Page<>(query.getPageNo(), query.getPageSize());
        return baseMapper.selectVolunteerList(page, query);
    }
    
    @Override
    public VolunteerUserDetailVO getVolunteerDetail(Long userId) {
        VolunteerUser user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        VolunteerUserDetailVO detailVO = new VolunteerUserDetailVO();
        BeanUtils.copyProperties(user, detailVO);
        
        // 设置性别文本
        switch (user.getGender()) {
            case 1:
                detailVO.setGenderText("男");
                break;
            case 2:
                detailVO.setGenderText("女");
                break;
            default:
                detailVO.setGenderText("未知");
                break;
        }
        
        // 设置状态文本
        switch (user.getStatus()) {
            case 1:
                detailVO.setStatusText("正常");
                break;
            case 0:
                detailVO.setStatusText("禁用");
                break;
            default:
                detailVO.setStatusText("未知");
                break;
        }
        
        // 计算服务时长、活动次数、积分等统计信息
        // 总服务时长 - 直接从用户表获取
        detailVO.setTotalServiceHours(user.getTotalServiceHours() != null ? user.getTotalServiceHours().intValue() : 0);
        
        // 活动次数 - 保持从服务记录表统计
        Integer activityCount = serviceRecordMapper.selectDistinctActivityCount(userId);
        detailVO.setActivityCount(activityCount != null ? activityCount : 0);
        detailVO.setTotalPoints(user.getTotalPoints() != null ? user.getTotalPoints() : 0);
        
        // 等级计算逻辑已屏蔽，改为使用星级字段
        // int serviceHours = detailVO.getTotalServiceHours();
        // if (serviceHours >= 500) {
        //     detailVO.setLevel("专家");
        //     detailVO.setLevelValue(4);
        // } else if (serviceHours >= 200) {
        //     detailVO.setLevel("高级");
        //     detailVO.setLevelValue(3);
        // } else if (serviceHours >= 50) {
        //     detailVO.setLevel("中级");
        //     detailVO.setLevelValue(2);
        // } else {
        //     detailVO.setLevel("初级");
        //     detailVO.setLevelValue(1);
        // }
        
        // 设置星级信息
        detailVO.setStarLevel(user.getStarLevel() != null ? user.getStarLevel() : 0);
        detailVO.setStarRatingTime(user.getStarRatingTime());
        
        // 查询用户技能信息
        List<VolunteerUserDetailVO.UserSkillInfo> userSkills = getUserSkillsInfo(userId);
        detailVO.setUserSkills(userSkills);
        
        // 查询用户证书信息
        List<VolunteerUserDetailVO.UserCertificateInfo> userCertificates = getUserCertificatesInfo(userId);
        detailVO.setUserCertificates(userCertificates);
        
        return detailVO;
    }
    
    /**
     * 获取用户技能信息
     */
    private List<VolunteerUserDetailVO.UserSkillInfo> getUserSkillsInfo(Long userId) {
        LambdaQueryWrapper<UserSkill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSkill::getUserId, userId)
               .eq(UserSkill::getStatus, 1)
               .orderByDesc(UserSkill::getCreatedTime);
        
        List<UserSkill> skills = userSkillService.list(wrapper);
        List<VolunteerUserDetailVO.UserSkillInfo> skillInfos = new ArrayList<>();
        
        for (UserSkill skill : skills) {
            VolunteerUserDetailVO.UserSkillInfo skillInfo = new VolunteerUserDetailVO.UserSkillInfo();
            BeanUtils.copyProperties(skill, skillInfo);
            
            // 设置技能分类名称
            if (skill.getSkillCategoryId() != null) {
                SkillCategory category = skillCategoryService.getById(skill.getSkillCategoryId());
                if (category != null) {
                    skillInfo.setSkillCategoryName(category.getCategoryName());
                }
            }
            
            // 设置技能等级文本
            skillInfo.setSkillLevelText(getSkillLevelText(skill.getSkillLevel()));
            
            // 设置验证状态文本
            skillInfo.setVerifiedText(skill.getVerified() == 1 ? "已验证" : "未验证");
            
            // 设置证书名称
            if (skill.getCertificateId() != null) {
                UserCertificate certificate = userCertificateService.getById(skill.getCertificateId());
                if (certificate != null) {
                    skillInfo.setCertificateName(certificate.getCertificateName());
                }
            }
            
            skillInfos.add(skillInfo);
        }
        
        return skillInfos;
    }
    
    /**
     * 获取用户证书信息
     */
    private List<VolunteerUserDetailVO.UserCertificateInfo> getUserCertificatesInfo(Long userId) {
        LambdaQueryWrapper<UserCertificate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCertificate::getUserId, userId)
               .orderByDesc(UserCertificate::getCreatedTime);
        
        List<UserCertificate> certificates = userCertificateService.list(wrapper);
        List<VolunteerUserDetailVO.UserCertificateInfo> certificateInfos = new ArrayList<>();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (UserCertificate certificate : certificates) {
            VolunteerUserDetailVO.UserCertificateInfo certificateInfo = new VolunteerUserDetailVO.UserCertificateInfo();
            BeanUtils.copyProperties(certificate, certificateInfo);
            
            // 设置日期格式
            if (certificate.getIssueDate() != null) {
                certificateInfo.setIssueDate(certificate.getIssueDate().format(formatter));
            }
            if (certificate.getExpiryDate() != null) {
                certificateInfo.setExpiryDate(certificate.getExpiryDate().format(formatter));
            }
            if (certificate.getAuditTime() != null) {
                certificateInfo.setAuditTime(certificate.getAuditTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
            
            // 设置状态文本
            certificateInfo.setStatusText(getCertificateStatusText(certificate.getStatus()));
            
            certificateInfos.add(certificateInfo);
        }
        
        return certificateInfos;
    }
    
    /**
     * 获取技能等级文本
     */
    private String getSkillLevelText(Integer level) {
        if (level == null) {
            return "未知";
        }
        switch (level) {
            case 1:
                return "初级";
            case 2:
                return "中级";
            case 3:
                return "高级";
            case 4:
                return "专家级";
            default:
                return "未知";
        }
    }
    
    /**
     * 获取证书状态文本
     */
    private String getCertificateStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 1:
                return "待审核";
            case 2:
                return "已通过";
            case 3:
                return "已拒绝";
            default:
                return "未知";
        }
    }
    
    @Override
    @Transactional
    public boolean updateVolunteer(VolunteerUserUpdateDTO updateDTO) {
        try {
            VolunteerUser user = this.getById(updateDTO.getId());
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            
            // 检查邮箱和手机号唯一性
            if (updateDTO.getEmail() != null && !updateDTO.getEmail().equals(user.getEmail())) {
                QueryWrapper<VolunteerUser> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("email", updateDTO.getEmail());
                queryWrapper.ne("id", updateDTO.getId());
                VolunteerUser existUser = this.getOne(queryWrapper);
                if (existUser != null) {
                    throw new RuntimeException("邮箱已被使用");
                }
            }
            
            if (updateDTO.getPhone() != null && !updateDTO.getPhone().equals(user.getPhone())) {
                QueryWrapper<VolunteerUser> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("phone", updateDTO.getPhone());
                queryWrapper.ne("id", updateDTO.getId());
                VolunteerUser existUser = this.getOne(queryWrapper);
                if (existUser != null) {
                    throw new RuntimeException("手机号已被使用");
                }
            }
            
            // 更新用户信息
            BeanUtils.copyProperties(updateDTO, user);
            return this.updateById(user);
        } catch (Exception e) {
            log.error("更新志愿者信息失败：{}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public boolean updateVolunteerStatus(Long userId, Integer status) {
        try {
            VolunteerUser user = this.getById(userId);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            
            user.setStatus(status);
            return this.updateById(user);
        } catch (Exception e) {
            log.error("更新志愿者状态失败：{}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public boolean batchOperateVolunteers(VolunteerUserBatchDTO batchDTO) {
        try {
            if (batchDTO.getUserIds() == null || batchDTO.getUserIds().isEmpty()) {
                throw new RuntimeException("请选择要操作的用户");
            }
            
            switch (batchDTO.getOperation()) {
                case "enable":
                    return baseMapper.batchUpdateStatus(batchDTO.getUserIds(), 1) > 0;
                case "disable":
                    return baseMapper.batchUpdateStatus(batchDTO.getUserIds(), 0) > 0;
                case "delete":
                    return this.removeByIds(batchDTO.getUserIds());
                default:
                    throw new RuntimeException("不支持的操作类型");
            }
        } catch (Exception e) {
            log.error("批量操作志愿者失败：{}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public boolean deleteVolunteer(Long userId) {
        try {
            VolunteerUser user = this.getById(userId);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            
            return this.removeById(userId);
        } catch (Exception e) {
            log.error("删除志愿者失败：{}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Override
    public VolunteerStatisticsVO getVolunteerStatistics() {
        return baseMapper.selectVolunteerStatistics();
    }
    
    @Override
    public Map<String, Object> getStarVolunteers(String name, Integer starLevel, Integer page, Integer size) {
        try {
            // 获取所有志愿者及其服务时长统计，增加查询数量
            List<VolunteerUserListVO> volunteers = baseMapper.selectVolunteerList(new Page<>(1, 200), new VolunteerUserQueryDTO()).getRecords();
            
            log.info("查询到志愿者数量: {}", volunteers.size());
            
            // 先过滤数据
            List<VolunteerUserListVO> filteredVolunteers = volunteers.stream()
                .filter(volunteer -> volunteer.getStarLevel() != null && volunteer.getStarLevel() > 0) // 只显示有星级的志愿者
                .filter(volunteer -> {
                    // 按姓名过滤
                    if (name != null && !name.trim().isEmpty()) {
                        return volunteer.getRealName() != null && volunteer.getRealName().contains(name.trim());
                    }
                    return true;
                })
                .filter(volunteer -> {
                    // 按星级过滤
                    if (starLevel != null) {
                        return volunteer.getStarLevel() != null && volunteer.getStarLevel().equals(starLevel);
                    }
                    return true;
                })
                .sorted((a, b) -> {
                    // 按星级从高到低排序，星级相同时按服务时长排序
                    int starCompare = Integer.compare(b.getStarLevel(), a.getStarLevel());
                    if (starCompare != 0) {
                        return starCompare;
                    }
                    return Integer.compare(b.getTotalServiceHours(), a.getTotalServiceHours());
                })
                .collect(Collectors.toList());
            
            // 计算总数
            int totalCount = filteredVolunteers.size();
            
            // 分页处理
            List<Map<String, Object>> result = filteredVolunteers.stream()
                .skip((page - 1) * size) // 分页偏移
                .limit(size) // 分页大小
                .map(volunteer -> {
                    Map<String, Object> starVolunteer = new HashMap<>();
                    starVolunteer.put("id", volunteer.getId());
                    starVolunteer.put("realName", volunteer.getRealName());
                    starVolunteer.put("phone", volunteer.getPhone());
                    starVolunteer.put("starLevel", volunteer.getStarLevel());
                    starVolunteer.put("totalServiceHours", volunteer.getTotalServiceHours() != null ? volunteer.getTotalServiceHours() : 0);
                    starVolunteer.put("totalPoints", volunteer.getTotalPoints() != null ? volunteer.getTotalPoints() : 0);
                    starVolunteer.put("activityCount", volunteer.getActivityCount() != null ? volunteer.getActivityCount() : 0);
                    
                    // 使用数据库中的星级
                    int stars = volunteer.getStarLevel() != null ? volunteer.getStarLevel() : 0;
                    starVolunteer.put("stars", stars);
                    starVolunteer.put("starText", generateStarText(stars));
                    
                    return starVolunteer;
                })
                .collect(Collectors.toList());
            
            // 返回结果
            Map<String, Object> response = new HashMap<>();
            response.put("list", result);
            response.put("total", totalCount);
            response.put("page", page);
            response.put("size", size);
            
            return response;
        } catch (Exception e) {
            log.error("获取星级志愿者列表失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("list", new ArrayList<>());
            errorResponse.put("total", 0);
            errorResponse.put("page", page);
            errorResponse.put("size", size);
            return errorResponse;
        }
    }
    
    @Override
    public List<Map<String, Object>> getPointsRanking() {
        // 获取所有志愿者及其积分统计
        List<VolunteerUserListVO> volunteers = baseMapper.selectVolunteerList(new Page<>(1, 100), new VolunteerUserQueryDTO()).getRecords();
        
        return volunteers.stream()
            .filter(volunteer -> volunteer.getTotalPoints() > 0) // 有积分的志愿者
            .sorted((a, b) -> Integer.compare(b.getTotalPoints(), a.getTotalPoints())) // 按积分降序
            .limit(20) // 取前20名
            .map(volunteer -> {
                Map<String, Object> ranking = new HashMap<>();
                ranking.put("id", volunteer.getId());
                ranking.put("realName", volunteer.getRealName());
                ranking.put("totalPoints", volunteer.getTotalPoints());
                ranking.put("totalServiceHours", volunteer.getTotalServiceHours());
                ranking.put("starLevel", volunteer.getStarLevel());
                ranking.put("activityCount", volunteer.getActivityCount());
                
                return ranking;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 生成星级文本
     * @param stars 星级数量
     * @return 星级文本
     */
    private String generateStarText(int stars) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stars; i++) {
            sb.append("★");
        }
        return sb.toString();
    }
    
    @Override
    @Transactional
    public boolean rateVolunteer(Long userId, Integer starLevel, Long ratingBy) {
        try {
            // 验证星级范围
            if (starLevel < 0 || starLevel > 5) {
                log.warn("星级范围无效: userId={}, starLevel={}", userId, starLevel);
                return false;
            }
            
            VolunteerUser volunteer = getById(userId);
            if (volunteer == null) {
                log.warn("志愿者不存在: userId={}", userId);
                return false;
            }
            
            // 更新星级信息
            volunteer.setStarLevel(starLevel);
            volunteer.setStarRatingTime(LocalDateTime.now());
            volunteer.setStarRatingBy(ratingBy);
            
            boolean result = updateById(volunteer);
            if (result) {
                log.info("志愿者评级成功: userId={}, starLevel={}, ratingBy={}", userId, starLevel, ratingBy);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("志愿者评级失败: userId={}, starLevel={}, ratingBy={}", userId, starLevel, ratingBy, e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean updateVolunteerRating(Long userId, Integer starLevel, String ratingRemark, Long ratingBy) {
        try {
            // 验证星级范围
            if (starLevel < 0 || starLevel > 5) {
                log.warn("星级范围无效: userId={}, starLevel={}", userId, starLevel);
                return false;
            }
            
            VolunteerUser volunteer = getById(userId);
            if (volunteer == null) {
                log.warn("志愿者不存在: userId={}", userId);
                return false;
            }
            
            // 更新星级信息
            volunteer.setStarLevel(starLevel);
            volunteer.setStarRatingTime(LocalDateTime.now());
            volunteer.setStarRatingBy(ratingBy);
            
            boolean result = updateById(volunteer);
            if (result) {
                log.info("志愿者评级更新成功: userId={}, starLevel={}, ratingBy={}, remark={}", 
                    userId, starLevel, ratingBy, ratingRemark);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("志愿者评级更新失败: userId={}, starLevel={}, ratingBy={}, remark={}", 
                userId, starLevel, ratingBy, ratingRemark, e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean updateTotalServiceHours(Long userId, BigDecimal serviceHours) {
        try {
            VolunteerUser user = getById(userId);
            if (user == null) {
                log.warn("用户不存在: userId={}", userId);
                return false;
            }
            
            // 确保服务时长不为负数
            if (serviceHours.compareTo(BigDecimal.ZERO) < 0) {
                log.warn("服务时长不能为负数: userId={}, serviceHours={}", userId, serviceHours);
                return false;
            }
            
            user.setTotalServiceHours(serviceHours);
            boolean result = updateById(user);
            
            if (result) {
                log.info("更新用户总服务时长成功: userId={}, totalServiceHours={}", userId, serviceHours);
            } else {
                log.error("更新用户总服务时长失败: userId={}", userId);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("更新用户总服务时长失败: userId={}, serviceHours={}", userId, serviceHours, e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean addServiceHours(Long userId, BigDecimal additionalHours) {
        try {
            VolunteerUser user = getById(userId);
            if (user == null) {
                log.warn("用户不存在: userId={}", userId);
                return false;
            }
            
            // 获取当前总服务时长，如果为null则初始化为0
            BigDecimal currentHours = user.getTotalServiceHours();
            if (currentHours == null) {
                currentHours = BigDecimal.ZERO;
            }
            
            // 计算新的总服务时长
            BigDecimal newTotalHours = currentHours.add(additionalHours);
            
            // 确保总服务时长不为负数
            if (newTotalHours.compareTo(BigDecimal.ZERO) < 0) {
                log.warn("总服务时长不能为负数: userId={}, currentHours={}, additionalHours={}", 
                    userId, currentHours, additionalHours);
                return false;
            }
            
            user.setTotalServiceHours(newTotalHours);
            boolean result = updateById(user);
            
            if (result) {
                log.info("累加用户服务时长成功: userId={}, additionalHours={}, newTotalHours={}", 
                    userId, additionalHours, newTotalHours);
            } else {
                log.error("累加用户服务时长失败: userId={}", userId);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("累加用户服务时长失败: userId={}, additionalHours={}", userId, additionalHours, e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean recalculateTotalServiceHours(Long userId) {
        try {
            VolunteerUser user = getById(userId);
            if (user == null) {
                log.warn("用户不存在: userId={}", userId);
                return false;
            }
            
            // 从签到表全量统计用户的服务时长（强制从数据库读取最新数据）
            BigDecimal totalCalculatedHours = checkinMapper.selectTotalServiceHoursByUserId(userId);
            if (totalCalculatedHours == null) {
                totalCalculatedHours = BigDecimal.ZERO;
            }
            
            // 获取旧的总服务时长用于日志记录
            BigDecimal oldTotalHours = user.getTotalServiceHours();
            
            // 更新用户总服务时长
            user.setTotalServiceHours(totalCalculatedHours);
            boolean result = updateById(user);
            
            if (result) {
                log.info("全量重新计算用户服务时长成功: userId={}, oldTotalHours={}, newTotalHours={}", 
                    userId, oldTotalHours, totalCalculatedHours);
            } else {
                log.error("全量重新计算用户服务时长失败: userId={}", userId);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("全量重新计算用户服务时长失败: userId={}", userId, e);
            return false;
        }
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean recalculateTotalServiceHoursInNewTransaction(Long userId) {
        try {
            VolunteerUser user = getById(userId);
            if (user == null) {
                log.warn("用户不存在: userId={}", userId);
                return false;
            }
            
            // 在新事务中从签到表全量统计用户的服务时长（确保读取到已提交的数据）
            BigDecimal totalCalculatedHours = checkinMapper.selectTotalServiceHoursByUserId(userId);
            if (totalCalculatedHours == null) {
                totalCalculatedHours = BigDecimal.ZERO;
            }
            
            // 获取旧的总服务时长用于日志记录
            BigDecimal oldTotalHours = user.getTotalServiceHours();
            
            // 更新用户总服务时长
            user.setTotalServiceHours(totalCalculatedHours);
            boolean result = updateById(user);
            
            if (result) {
                log.info("新事务中全量重新计算用户服务时长成功: userId={}, oldTotalHours={}, newTotalHours={}", 
                    userId, oldTotalHours, totalCalculatedHours);
            } else {
                log.error("新事务中全量重新计算用户服务时长失败: userId={}", userId);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("新事务中全量重新计算用户服务时长失败: userId={}", userId, e);
            return false;
        }
    }
}
