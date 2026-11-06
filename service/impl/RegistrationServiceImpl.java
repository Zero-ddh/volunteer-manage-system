package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.Activity;
import com.volunteer.entity.Registration;
import com.volunteer.entity.VolunteerUser;
import com.volunteer.entity.UserSkill;
import com.volunteer.entity.UserCertificate;
import com.volunteer.entity.SkillCategory;
import com.volunteer.mapper.ActivityMapper;
import com.volunteer.mapper.RegistrationMapper;
import com.volunteer.service.RegistrationService;
import com.volunteer.service.VolunteerUserService;
import com.volunteer.service.UserSkillService;
import com.volunteer.service.UserCertificateService;
import com.volunteer.service.SkillCategoryService;
import com.volunteer.service.ActivitySkillRequirementService;
import com.volunteer.service.ActivityCategoryService;
import com.volunteer.entity.ActivityCategory;
import com.volunteer.mapper.ActivityCategoryMapper;
import com.volunteer.vo.RegistrationVO;
import com.volunteer.vo.RegistrationResult;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.html2pdf.ConverterProperties;

@Service
public class RegistrationServiceImpl implements RegistrationService {

    @Resource
    private RegistrationMapper registrationMapper;

    @Resource
    private VolunteerUserService volunteerUserService;

    @Resource
    private ActivityMapper activityMapper;

    @Resource
    private UserSkillService userSkillService;

    @Resource
    private UserCertificateService userCertificateService;

    @Resource
    private SkillCategoryService skillCategoryService;

    @Resource
    private ActivitySkillRequirementService activitySkillRequirementService;

    @Resource
    private ActivityCategoryService activityCategoryService;

    @Resource
    private ActivityCategoryMapper activityCategoryMapper;

    @Override
    public RegistrationResult registerWithStatus(Long activityId, Long userId) {
        // 首先检查活动状态
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            return RegistrationResult.failure("活动不存在！", null);
        }
        
        // 检查活动状态
        if (activity.getStatus() == 4) { // 已结束
            return RegistrationResult.failure("该活动已结束，目前无法报名！", null);
        }
        if (activity.getStatus() == 5) { // 已取消
            return RegistrationResult.failure("该活动已取消，目前无法报名！", null);
        }
        
        // 检查报名人数是否已满
        Integer maxParticipants = activity.getMaxParticipants();
        Integer currentParticipants = activity.getCurrentParticipants();
        if (maxParticipants != null && maxParticipants > 0 && 
            currentParticipants != null && currentParticipants >= maxParticipants) {
            return RegistrationResult.failure("报名人数已满，暂无法报名！", null);
        }
        
        // 查询现有报名记录
        Registration exist = registrationMapper.selectOne(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getActivityId, activityId)
                .eq(Registration::getUserId, userId)
                .last("limit 1"));
        
        if (exist != null) {
            switch (exist.getStatus()) {
                case 2: // 已通过
                    return RegistrationResult.failure("您已经成功报名，不需要重复报名！", 2);
                case 1: // 待审核
                    return RegistrationResult.failure("您已经成功报名，请耐心等待审核！", 1);
                case 3: // 已拒绝
                    return RegistrationResult.failure("经审核后，您不符合报名条件，请看下其他活动是否符合要求！", 3);
                case 4: // 已取消 - 允许重新报名，更新现有记录
                    // 检查报名人数是否已满
                    if (maxParticipants != null && maxParticipants > 0 && 
                        currentParticipants != null && currentParticipants >= maxParticipants) {
                        return RegistrationResult.failure("报名人数已满，暂无法报名！", null);
                    }
                    
                    exist.setStatus(1); // 1-待审核
                    exist.setRegistrationTime(LocalDateTime.now());
                    exist.setRemark(null); // 清除之前的备注
                    boolean updateSuccess = registrationMapper.updateById(exist) > 0;
                    if (updateSuccess) {
                        return RegistrationResult.success("重新报名成功，请等待审核！", 1, exist.getId());
                    } else {
                        return RegistrationResult.failure("重新报名失败，请稍后重试！", null);
                    }
            }
        }
        
        // 创建新报名记录
        Registration r = new Registration();
        r.setActivityId(activityId);
        r.setUserId(userId);
        r.setStatus(1); // 1-待审核
        r.setRegistrationTime(LocalDateTime.now());
        
        boolean success = registrationMapper.insert(r) > 0;
        if (success) {
            return RegistrationResult.success("报名成功，请等待审核！", 1, r.getId());
        } else {
            return RegistrationResult.failure("报名失败，请稍后重试！", null);
        }
    }

    @Override
    public boolean register(Long activityId, Long userId) {
        // 已存在则直接返回true
        Registration exist = registrationMapper.selectOne(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getActivityId, activityId)
                .eq(Registration::getUserId, userId)
                .last("limit 1"));
        if (exist != null && (exist.getStatus() == 1 || exist.getStatus() == 2)) {
            return true;
        }
        Registration r = new Registration();
        r.setActivityId(activityId);
        r.setUserId(userId);
        r.setStatus(1); // 1-待审核
        r.setRegistrationTime(LocalDateTime.now());
        return registrationMapper.insert(r) > 0;
    }

    @Override
    public boolean cancel(Long activityId, Long userId) {
        Registration exist = registrationMapper.selectOne(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getActivityId, activityId)
                .eq(Registration::getUserId, userId)
                .last("limit 1"));
        if (exist == null) {
            return true;
        }
        exist.setStatus(4); // 4-已取消
        return registrationMapper.updateById(exist) > 0;
    }

    @Override
    public Page<Registration> myRegistrations(Long userId, int pageNo, int pageSize) {
        Page<Registration> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<Registration> wrapper = new LambdaQueryWrapper<Registration>()
                .eq(Registration::getUserId, userId)
                .orderByDesc(Registration::getCreatedTime);
        return registrationMapper.selectPage(page, wrapper);
    }

    @Override
    public Page<RegistrationVO> myRegistrationsWithDetails(Long userId, int pageNo, int pageSize) {
        // 查询报名记录
        Page<Registration> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<Registration> wrapper = new LambdaQueryWrapper<Registration>()
                .eq(Registration::getUserId, userId)
                .orderByDesc(Registration::getCreatedTime);
        Page<Registration> registrationPage = registrationMapper.selectPage(page, wrapper);
        
        if (registrationPage.getRecords().isEmpty()) {
            return new Page<>(pageNo, pageSize);
        }
        
        // 提取用户ID和活动ID
        List<Long> userIds = registrationPage.getRecords().stream()
                .map(Registration::getUserId)
                .distinct()
                .collect(Collectors.toList());
        List<Long> activityIds = registrationPage.getRecords().stream()
                .map(Registration::getActivityId)
                .distinct()
                .collect(Collectors.toList());
        
        // 批量查询用户信息
        Map<Long, VolunteerUser> userMap = volunteerUserService.listByIds(userIds)
                .stream()
                .collect(Collectors.toMap(VolunteerUser::getId, user -> user));
        
        // 批量查询活动信息
        Map<Long, Activity> activityMap = activityMapper.selectBatchIds(activityIds)
                .stream()
                .collect(Collectors.toMap(Activity::getId, activity -> activity));
        
        // 转换为VO
        List<RegistrationVO> voList = registrationPage.getRecords().stream().map(registration -> {
            RegistrationVO vo = new RegistrationVO();
            BeanUtils.copyProperties(registration, vo);
            
            // 设置用户信息
            VolunteerUser user = userMap.get(registration.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
                vo.setRealName(user.getRealName());
                vo.setPhone(user.getPhone());
                vo.setEmail(user.getEmail());
            }
            
            // 设置活动信息
            Activity activity = activityMap.get(registration.getActivityId());
            if (activity != null) {
                vo.setActivityTitle(activity.getTitle());
                vo.setActivityDescription(activity.getDescription());
                vo.setActivityStartTime(activity.getStartTime());
                vo.setActivityEndTime(activity.getEndTime());
                vo.setActivityLocation(activity.getLocation());
            }
            
            return vo;
        }).collect(Collectors.toList());
        
        // 构建分页结果
        Page<RegistrationVO> voPage = new Page<>(pageNo, pageSize);
        voPage.setTotal(registrationPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public Page<Registration> activityRegistrations(Long activityId, int pageNo, int pageSize) {
        Page<Registration> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<Registration> wrapper = new LambdaQueryWrapper<Registration>()
                .eq(Registration::getActivityId, activityId)
                .orderByDesc(Registration::getCreatedTime);
        return registrationMapper.selectPage(page, wrapper);
    }

    @Override
    public Page<Registration> adminPageList(int pageNo, int pageSize, String keyword, Long activityId, Integer status) {
        Page<Registration> page = new Page<>(pageNo, pageSize);
        
        // 如果有关键词搜索，需要先查询匹配的用户ID和活动ID
        if (keyword != null && !keyword.trim().isEmpty()) {
            // 查询匹配的用户ID
            List<Long> userIds = volunteerUserService.list(new LambdaQueryWrapper<VolunteerUser>()
                    .like(VolunteerUser::getRealName, keyword)
                    .or()
                    .like(VolunteerUser::getPhone, keyword))
                    .stream()
                    .map(VolunteerUser::getId)
                    .collect(Collectors.toList());
            
            // 查询匹配的活动ID
            List<Long> activityIds = activityMapper.selectList(new LambdaQueryWrapper<Activity>()
                    .like(Activity::getTitle, keyword))
                    .stream()
                    .map(Activity::getId)
                    .collect(Collectors.toList());
            
            LambdaQueryWrapper<Registration> wrapper = new LambdaQueryWrapper<Registration>()
                    .and(w -> w.like(Registration::getRemark, keyword)
                            .or(!userIds.isEmpty(), q -> q.in(Registration::getUserId, userIds))
                            .or(!activityIds.isEmpty(), q -> q.in(Registration::getActivityId, activityIds)))
                    .eq(activityId != null, Registration::getActivityId, activityId)
                    .eq(status != null, Registration::getStatus, status)
                    .orderByDesc(Registration::getCreatedTime);
            return registrationMapper.selectPage(page, wrapper);
        } else {
            // 没有关键词时，按活动ID和状态筛选
            LambdaQueryWrapper<Registration> wrapper = new LambdaQueryWrapper<Registration>()
                    .eq(activityId != null, Registration::getActivityId, activityId)
                    .eq(status != null, Registration::getStatus, status)
                    .orderByDesc(Registration::getCreatedTime);
            return registrationMapper.selectPage(page, wrapper);
        }
    }

    @Override
    public boolean auditRegistration(Long id, Integer status, String remark) {
        Registration registration = registrationMapper.selectById(id);
        if (registration == null) {
            return false;
        }
        registration.setStatus(status);
        registration.setRemark(remark);
        registration.setUpdatedTime(LocalDateTime.now());
        return registrationMapper.updateById(registration) > 0;
    }

    @Override
    public boolean batchAuditRegistrations(java.util.List<Long> ids, Integer status, String remark) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        for (Long id : ids) {
            Registration registration = registrationMapper.selectById(id);
            if (registration != null) {
                registration.setStatus(status);
                registration.setRemark(remark);
                registration.setUpdatedTime(LocalDateTime.now());
                registrationMapper.updateById(registration);
            }
        }
        return true;
    }

    @Override
    public Page<RegistrationVO> adminPageListWithDetails(int pageNo, int pageSize, String keyword, Long activityId, Integer status) {
        // 先查询报名数据
        Page<Registration> registrationPage = adminPageList(pageNo, pageSize, keyword, activityId, status);
        
        // 创建VO分页对象
        Page<RegistrationVO> voPage = new Page<>(pageNo, pageSize);
        voPage.setTotal(registrationPage.getTotal());
        voPage.setPages(registrationPage.getPages());
        
        if (registrationPage.getRecords().isEmpty()) {
            return voPage;
        }
        
        // 获取所有用户ID和活动ID
        List<Long> userIds = registrationPage.getRecords().stream()
                .map(Registration::getUserId)
                .distinct()
                .collect(Collectors.toList());
        
        List<Long> activityIds = registrationPage.getRecords().stream()
                .map(Registration::getActivityId)
                .distinct()
                .collect(Collectors.toList());
        
        // 批量查询用户信息
        Map<Long, VolunteerUser> userMap = volunteerUserService.listByIds(userIds)
                .stream()
                .collect(Collectors.toMap(VolunteerUser::getId, user -> user));
        
        // 批量查询活动信息
        Map<Long, Activity> activityMap = activityMapper.selectBatchIds(activityIds)
                .stream()
                .collect(Collectors.toMap(Activity::getId, activity -> activity));
        
        // 批量查询分类信息
        List<Long> categoryIds = activityMap.values().stream()
                .map(Activity::getCategoryId)
                .filter(categoryId -> categoryId != null)
                .distinct()
                .collect(Collectors.toList());
        
        final Map<Long, ActivityCategory> categoryMap;
        if (!categoryIds.isEmpty()) {
            List<ActivityCategory> categories = activityCategoryMapper.selectBatchIds(categoryIds);
            categoryMap = categories.stream()
                    .collect(Collectors.toMap(ActivityCategory::getId, category -> category));
        } else {
            categoryMap = new java.util.HashMap<>();
        }
        
        // 转换为VO
        List<RegistrationVO> voList = registrationPage.getRecords().stream().map(registration -> {
            RegistrationVO vo = new RegistrationVO();
            BeanUtils.copyProperties(registration, vo);
            
            // 设置用户信息
            VolunteerUser user = userMap.get(registration.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
                vo.setRealName(user.getRealName());
                vo.setPhone(user.getPhone());
                vo.setEmail(user.getEmail());
            }
            
            // 设置活动信息
            Activity activity = activityMap.get(registration.getActivityId());
            if (activity != null) {
                vo.setActivityTitle(activity.getTitle());
                vo.setActivityDescription(activity.getDescription());
                vo.setActivityStartTime(activity.getStartTime());
                vo.setActivityEndTime(activity.getEndTime());
                vo.setActivityLocation(activity.getLocation());
                vo.setActivityRequirements(activity.getRequirements());
                
                // 设置分类信息
                vo.setActivityCategoryId(activity.getCategoryId());
                if (activity.getCategoryId() != null) {
                    ActivityCategory category = categoryMap.get(activity.getCategoryId());
                    if (category != null) {
                        vo.setActivityCategoryName(category.getCategoryName());
                    }
                }
            }
            
            return vo;
        }).collect(Collectors.toList());
        
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public RegistrationVO getRegistrationDetail(Long id) {
        // 查询报名信息
        Registration registration = registrationMapper.selectById(id);
        if (registration == null) {
            return null;
        }
        
        // 查询用户信息
        VolunteerUser user = volunteerUserService.getById(registration.getUserId());
        
        // 查询活动信息
        Activity activity = activityMapper.selectById(registration.getActivityId());
        
        // 构建VO
        RegistrationVO vo = new RegistrationVO();
        BeanUtils.copyProperties(registration, vo);
        
        // 设置用户信息
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setRealName(user.getRealName());
            vo.setPhone(user.getPhone());
            vo.setEmail(user.getEmail());
            vo.setIdCard(user.getIdCard());
        }
        
        // 设置活动信息
        if (activity != null) {
            vo.setActivityTitle(activity.getTitle());
            vo.setActivityDescription(activity.getDescription());
            vo.setActivityStartTime(activity.getStartTime());
            vo.setActivityEndTime(activity.getEndTime());
            vo.setActivityLocation(activity.getLocation());
            vo.setActivityRequirements(activity.getRequirements());
            
            // 设置分类信息
            vo.setActivityCategoryId(activity.getCategoryId());
            if (activity.getCategoryId() != null) {
                ActivityCategory category = activityCategoryService.getById(activity.getCategoryId());
                if (category != null) {
                    vo.setActivityCategoryName(category.getCategoryName());
                }
            }
        }
        
        // 查询用户技能信息
        if (user != null) {
            List<RegistrationVO.UserSkillInfo> userSkills = getUserSkillsInfo(user.getId());
            vo.setUserSkills(userSkills);
            
            // 查询用户证书信息
            List<RegistrationVO.UserCertificateInfo> userCertificates = getUserCertificatesInfo(user.getId());
            vo.setUserCertificates(userCertificates);
        }
        
        // 查询活动技能要求（使用活动表的requirements字段）
        if (activity != null) {
            // 不再查询activity_skill_requirement表，改用手动解析requirements字段
            String requirementsText = activity.getRequirements();
            if (requirementsText != null && !requirementsText.trim().isEmpty()) {
                // 将requirements文本转换为ActivitySkillRequirementInfo列表
                List<RegistrationVO.ActivitySkillRequirementInfo> activitySkillRequirements = parseActivitySkillRequirements(requirementsText);
                vo.setActivitySkillRequirements(activitySkillRequirements);
            }
            
            // 计算技能匹配度
            if (user != null) {
                double skillMatchScore = activitySkillRequirementService.calculateSkillMatchScore(user.getId(), activity.getId());
                vo.setSkillMatchScore(skillMatchScore);
            }
        }
        
        return vo;
    }
    
    /**
     * 获取用户技能信息
     */
    private List<RegistrationVO.UserSkillInfo> getUserSkillsInfo(Long userId) {
        LambdaQueryWrapper<UserSkill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSkill::getUserId, userId)
               .eq(UserSkill::getStatus, 1)
               .orderByDesc(UserSkill::getCreatedTime);
        
        List<UserSkill> skills = userSkillService.list(wrapper);
        List<RegistrationVO.UserSkillInfo> skillInfos = new ArrayList<>();
        
        for (UserSkill skill : skills) {
            RegistrationVO.UserSkillInfo skillInfo = new RegistrationVO.UserSkillInfo();
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
    private List<RegistrationVO.UserCertificateInfo> getUserCertificatesInfo(Long userId) {
        LambdaQueryWrapper<UserCertificate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCertificate::getUserId, userId)
               .orderByDesc(UserCertificate::getCreatedTime);
        
        List<UserCertificate> certificates = userCertificateService.list(wrapper);
        List<RegistrationVO.UserCertificateInfo> certificateInfos = new ArrayList<>();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (UserCertificate certificate : certificates) {
            RegistrationVO.UserCertificateInfo certificateInfo = new RegistrationVO.UserCertificateInfo();
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
     * 解析活动技能要求文本
     */
    private List<RegistrationVO.ActivitySkillRequirementInfo> parseActivitySkillRequirements(String requirementsText) {
        List<RegistrationVO.ActivitySkillRequirementInfo> requirementInfos = new ArrayList<>();
        
        if (requirementsText == null || requirementsText.trim().isEmpty()) {
            return requirementInfos;
        }
        
        // 简单的文本解析：按换行符分割
        String[] lines = requirementsText.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            
            RegistrationVO.ActivitySkillRequirementInfo requirementInfo = new RegistrationVO.ActivitySkillRequirementInfo();
            requirementInfo.setSkillName(line);
            requirementInfo.setDescription(line);
            requirementInfo.setRequiredLevelText("待定");
            requirementInfo.setIsRequiredText("可选");
            
            requirementInfos.add(requirementInfo);
        }
        
        return requirementInfos;
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
    public void generateRegistrationForm(Long registrationId, HttpServletResponse response) throws IOException {
        // 获取报名详情
        RegistrationVO registrationVO = getRegistrationDetail(registrationId);
        if (registrationVO == null) {
            throw new RuntimeException("报名信息不存在");
        }

        try {
            // 生成HTML内容
            String htmlContent = generateHtmlContent(registrationVO);
            
            // 配置Converter，支持中文
            ConverterProperties properties = new ConverterProperties();
            
            // 使用iText将HTML转换为PDF
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            HtmlConverter.convertToPdf(htmlContent, baos, properties);
            byte[] pdfBytes = baos.toByteArray();
            
            // 设置响应头
            response.setContentType("application/pdf");
            response.setCharacterEncoding("UTF-8");
            String fileName = String.format("报名表_%s_%s.pdf", 
                registrationVO.getActivityTitle(), 
                registrationVO.getRealName());
            response.setHeader("Content-Disposition", "attachment; filename=" + new String(fileName.getBytes("UTF-8"), "ISO-8859-1"));
            response.setContentLength(pdfBytes.length);
            
            // 写入响应
            response.getOutputStream().write(pdfBytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            throw new IOException("PDF生成失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成HTML内容
     */
    private String generateHtmlContent(RegistrationVO registrationVO) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<title>志愿者活动电子报名表</title>");
        html.append("<style>");
        html.append("body { font-family: 'Microsoft YaHei', Arial, sans-serif; margin: 40px; background-color: #ffffff; }");
        html.append(".header { text-align: center; margin-bottom: 40px; padding-bottom: 20px; border-bottom: 3px solid #2c5aa0; }");
        html.append(".title { font-size: 28px; font-weight: bold; color: #2c5aa0; }");
        html.append(".section { margin-bottom: 30px; }");
        html.append(".section-title { font-size: 20px; font-weight: bold; color: #2c5aa0; margin-bottom: 15px; padding: 10px; background-color: #f5f7fa; border-left: 5px solid #2c5aa0; }");
        html.append(".info-table { width: 100%; border-collapse: collapse; margin-bottom: 15px; border: 1px solid #ddd; }");
        html.append(".info-table tr { border-bottom: 1px solid #ddd; }");
        html.append(".info-table td { padding: 12px 15px; border-right: 1px solid #ddd; }");
        html.append(".info-table .label { background-color: #f5f7fa; font-weight: bold; width: 150px; color: #333; }");
        html.append(".info-table .value { background-color: #ffffff; }");
        html.append(".skills-table { width: 100%; border-collapse: collapse; margin-top: 15px; border: 1px solid #ddd; }");
        html.append(".skills-table th, .skills-table td { padding: 10px; border: 1px solid #ddd; }");
        html.append(".skills-table th { background-color: #f0f2f5; font-weight: bold; color: #333; }");
        html.append(".skills-table td { text-align: center; background-color: #ffffff; }");
        html.append(".footer { margin-top: 50px; text-align: center; font-size: 12px; color: #666; padding-top: 20px; border-top: 1px solid #ddd; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        
        // 标题
        html.append("<div class='header'>");
        html.append("<div class='title'>志愿者活动电子报名表</div>");
        html.append("</div>");
        
        // 活动信息
        html.append("<div class='section'>");
        html.append("<div class='section-title'>活动基本信息</div>");
        html.append("<table class='info-table'>");
        html.append("<tr><td class='label'>活动名称</td><td class='value'>").append(registrationVO.getActivityTitle() != null ? registrationVO.getActivityTitle() : "").append("</td></tr>");
        html.append("<tr><td class='label'>活动时间</td><td class='value'>").append(formatDateTime(registrationVO.getActivityStartTime())).append(" - ").append(formatDateTime(registrationVO.getActivityEndTime())).append("</td></tr>");
        html.append("<tr><td class='label'>活动地点</td><td class='value'>").append(registrationVO.getActivityLocation() != null ? registrationVO.getActivityLocation() : "").append("</td></tr>");
        html.append("<tr><td class='label'>活动描述</td><td class='value'>").append(registrationVO.getActivityDescription() != null ? registrationVO.getActivityDescription() : "").append("</td></tr>");
        html.append("</table>");
        html.append("</div>");
        
        // 个人信息
        html.append("<div class='section'>");
        html.append("<div class='section-title'>个人信息</div>");
        html.append("<table class='info-table'>");
        html.append("<tr><td class='label'>姓名</td><td class='value'>").append(registrationVO.getRealName() != null ? registrationVO.getRealName() : "").append("</td></tr>");
        html.append("<tr><td class='label'>用户名</td><td class='value'>").append(registrationVO.getUsername() != null ? registrationVO.getUsername() : "").append("</td></tr>");
        html.append("<tr><td class='label'>手机号</td><td class='value'>").append(registrationVO.getPhone() != null ? registrationVO.getPhone() : "").append("</td></tr>");
        html.append("<tr><td class='label'>邮箱</td><td class='value'>").append(registrationVO.getEmail() != null ? registrationVO.getEmail() : "").append("</td></tr>");
        html.append("<tr><td class='label'>身份证号</td><td class='value'>").append(registrationVO.getIdCard() != null ? registrationVO.getIdCard() : "").append("</td></tr>");
        html.append("<tr><td class='label'>报名时间</td><td class='value'>").append(formatDateTime(registrationVO.getRegistrationTime())).append("</td></tr>");
        html.append("<tr><td class='label'>报名状态</td><td class='value'>").append(registrationVO.getStatusText()).append("</td></tr>");
        html.append("</table>");
        html.append("</div>");
        
        // 技能信息
        if (registrationVO.getUserSkills() != null && !registrationVO.getUserSkills().isEmpty()) {
            html.append("<div class='section'>");
            html.append("<div class='section-title'>技能信息</div>");
            html.append("<table class='skills-table'>");
            html.append("<tr><th>技能名称</th><th>技能分类</th><th>技能等级</th><th>是否认证</th><th>相关证书</th></tr>");
            
            for (RegistrationVO.UserSkillInfo skill : registrationVO.getUserSkills()) {
                html.append("<tr>");
                html.append("<td>").append(skill.getSkillName()).append("</td>");
                html.append("<td>").append(skill.getSkillCategoryName()).append("</td>");
                html.append("<td>").append(skill.getSkillLevelText()).append("</td>");
                html.append("<td>").append(skill.getVerifiedText()).append("</td>");
                html.append("<td>").append(skill.getCertificateName() != null ? skill.getCertificateName() : "-").append("</td>");
                html.append("</tr>");
            }
            
            html.append("</table>");
            html.append("</div>");
        }
        
        // 证书信息
        if (registrationVO.getUserCertificates() != null && !registrationVO.getUserCertificates().isEmpty()) {
            html.append("<div class='section'>");
            html.append("<div class='section-title'>证书信息</div>");
            html.append("<table class='skills-table'>");
            html.append("<tr><th>证书名称</th><th>颁发机构</th><th>证书编号</th><th>颁发日期</th><th>有效期</th><th>状态</th></tr>");
            
            for (RegistrationVO.UserCertificateInfo cert : registrationVO.getUserCertificates()) {
                html.append("<tr>");
                html.append("<td>").append(cert.getCertificateName()).append("</td>");
                html.append("<td>").append(cert.getIssuingAuthority()).append("</td>");
                html.append("<td>").append(cert.getCertificateNumber()).append("</td>");
                html.append("<td>").append(cert.getIssueDate()).append("</td>");
                html.append("<td>").append(cert.getExpiryDate()).append("</td>");
                html.append("<td>").append(cert.getStatusText()).append("</td>");
                html.append("</tr>");
            }
            
            html.append("</table>");
            html.append("</div>");
        }
        
        // 技能匹配分析
        if (registrationVO.getSkillMatchScore() != null) {
            html.append("<div class='section'>");
            html.append("<div class='section-title'>技能匹配分析</div>");
            html.append("<table class='info-table'>");
            html.append("<tr><td class='label'>匹配度评分</td><td>").append(String.format("%.1f分", registrationVO.getSkillMatchScore())).append("</td></tr>");
            html.append("</table>");
            html.append("</div>");
        }
        
        // 页脚
        html.append("<div class='footer'>");
        html.append("<p>此报名表由志愿者服务活动平台自动生成</p>");
        html.append("<p>生成时间：").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</p>");
        html.append("</div>");
        
        html.append("</body>");
        html.append("</html>");
        
        return html.toString();
    }

    /**
     * 格式化日期时间
     */
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "-";
        }
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @Override
    public boolean isUserRegisteredAndApproved(Long activityId, Long userId) {
        LambdaQueryWrapper<Registration> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Registration::getActivityId, activityId)
               .eq(Registration::getUserId, userId)
               .eq(Registration::getStatus, 2) // 2-已通过
               .eq(Registration::getDeleted, 0);
        
        Registration registration = registrationMapper.selectOne(wrapper);
        return registration != null;
    }
}


