package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.volunteer.dto.CertificateAuditDTO;
import com.volunteer.dto.UserCertificateDTO;
import com.volunteer.entity.UserCertificate;
import com.volunteer.entity.UserSkill;
import com.volunteer.entity.VolunteerUser;
import com.volunteer.entity.SkillCategory;
import com.volunteer.mapper.UserCertificateMapper;
import com.volunteer.service.UserCertificateService;
import com.volunteer.service.UserSkillService;
import com.volunteer.service.VolunteerUserService;
import com.volunteer.service.SkillCategoryService;
import com.volunteer.vo.PageResult;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用户证书Service实现类
 * 
 * @author volunteer-team
 * @since 2025-01-20
 */
@Service
public class UserCertificateServiceImpl extends ServiceImpl<UserCertificateMapper, UserCertificate> implements UserCertificateService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserCertificateServiceImpl.class);
    
    @Resource
    @Lazy
    private UserSkillService userSkillService;
    
    @Resource
    @Lazy
    private VolunteerUserService volunteerUserService;
    
    @Resource
    @Lazy
    private SkillCategoryService skillCategoryService;
    
    @Override
    public List<UserCertificateDTO> getUserCertificates(Long userId) {
        LambdaQueryWrapper<UserCertificate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCertificate::getUserId, userId)
               .orderByDesc(UserCertificate::getCreatedTime);
        
        List<UserCertificate> certificates = list(wrapper);
        
        return certificates.stream().map(certificate -> {
            UserCertificateDTO dto = new UserCertificateDTO();
            BeanUtils.copyProperties(certificate, dto);
            
            // 设置技能名称
            if (certificate.getSkillId() != null) {
                UserSkill skill = userSkillService.getById(certificate.getSkillId());
                if (skill != null) {
                    dto.setSkillName(skill.getSkillName());
                }
            }
            
            // 设置状态文本
            dto.setStatusText(getStatusText(certificate.getStatus()));
            
            // 设置审核时间
            if (certificate.getAuditTime() != null) {
                dto.setAuditTime(certificate.getAuditTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
            
            return dto;
        }).collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public boolean addCertificate(UserCertificate certificate) {
        try {
            logger.info("开始保存证书: {}", certificate);
            certificate.setStatus(1); // 待审核状态
            boolean result = save(certificate);
            logger.info("证书保存结果: {}, 证书ID: {}", result, certificate.getId());
            
            // 更新关联技能的证书ID
            if (result && certificate.getSkillId() != null) {
                UserSkill skill = userSkillService.getById(certificate.getSkillId());
                if (skill != null) {
                    skill.setCertificateId(certificate.getId());
                    boolean updateResult = userSkillService.updateById(skill);
                    logger.info("更新技能证书ID结果: {}", updateResult);
                } else {
                    logger.warn("未找到技能ID: {}", certificate.getSkillId());
                }
            }
            
            return result;
        } catch (Exception e) {
            logger.error("保存证书异常", e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public boolean auditCertificate(CertificateAuditDTO auditDTO) {
        UserCertificate certificate = getById(auditDTO.getCertificateId());
        if (certificate == null) {
            return false;
        }
        
        certificate.setStatus(auditDTO.getStatus());
        certificate.setAuditRemark(auditDTO.getAuditRemark());
        certificate.setAuditTime(LocalDateTime.now());
        certificate.setAuditorId(auditDTO.getAuditorId());
        
        boolean result = updateById(certificate);
        
        // 如果审核通过，更新技能验证状态
        if (result && auditDTO.getStatus() == 2) {
            UserSkill skill = userSkillService.getById(certificate.getSkillId());
            if (skill != null) {
                skill.setVerified(1); // 已验证
                userSkillService.updateById(skill);
            }
        }
        
        return result;
    }
    
    @Override
    public List<UserCertificateDTO> getPendingCertificates() {
        LambdaQueryWrapper<UserCertificate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCertificate::getStatus, 1) // 待审核状态
               .orderByAsc(UserCertificate::getCreatedTime);
        
        List<UserCertificate> certificates = list(wrapper);
        
        return certificates.stream().map(certificate -> {
            UserCertificateDTO dto = new UserCertificateDTO();
            BeanUtils.copyProperties(certificate, dto);
            
            // 设置技能名称
            if (certificate.getSkillId() != null) {
                UserSkill skill = userSkillService.getById(certificate.getSkillId());
                if (skill != null) {
                    dto.setSkillName(skill.getSkillName());
                }
            }
            
            dto.setStatusText(getStatusText(certificate.getStatus()));
            
            return dto;
        }).collect(Collectors.toList());
    }
    
    @Override
    public PageResult<UserCertificateDTO> getPendingCertificatesWithPage(
            Integer pageNo, Integer pageSize, String certificateName, 
            String userName, Long skillCategoryId, Integer status) {
        
        Page<UserCertificate> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<UserCertificate> wrapper = new LambdaQueryWrapper<>();
        
        // 构建查询条件
        if (status != null) {
            wrapper.eq(UserCertificate::getStatus, status);
        }
        // 当status为null时，不添加状态过滤条件，返回所有状态的数据
        
        if (certificateName != null && !certificateName.trim().isEmpty()) {
            wrapper.like(UserCertificate::getCertificateName, certificateName.trim());
        }
        
        // 按ID倒序排序
        wrapper.orderByDesc(UserCertificate::getId);
        
        // 执行分页查询
        Page<UserCertificate> certificatePage = page(page, wrapper);
        
        // 转换为DTO
        List<UserCertificateDTO> dtoList = certificatePage.getRecords().stream().map(certificate -> {
            UserCertificateDTO dto = new UserCertificateDTO();
            BeanUtils.copyProperties(certificate, dto);
            
            // 设置用户信息
            if (certificate.getUserId() != null) {
                VolunteerUser user = volunteerUserService.getById(certificate.getUserId());
                if (user != null) {
                    dto.setUserName(user.getRealName());
                    dto.setUserPhone(user.getPhone());
                    dto.setUserEmail(user.getEmail());
                    dto.setUserIdCard(user.getIdCard());
                }
            }
            
            // 设置技能信息
            if (certificate.getSkillId() != null) {
                UserSkill skill = userSkillService.getById(certificate.getSkillId());
                if (skill != null) {
                    dto.setSkillName(skill.getSkillName());
                    dto.setSkillLevel(getSkillLevelText(skill.getSkillLevel()));
                    dto.setSkillDescription(skill.getDescription());
                    
                    // 设置技能分类信息
                    if (skill.getSkillCategoryId() != null) {
                        SkillCategory category = skillCategoryService.getById(skill.getSkillCategoryId());
                        if (category != null) {
                            dto.setSkillCategoryId(category.getId());
                            dto.setSkillCategoryName(category.getCategoryName());
                        }
                    }
                }
            }
            
            // 设置状态文本
            dto.setStatusText(getStatusText(certificate.getStatus()));
            
            // 设置创建时间
            if (certificate.getCreatedTime() != null) {
                dto.setCreatedTime(certificate.getCreatedTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
            
            // 设置审核人姓名
            if (certificate.getAuditorId() != null) {
                VolunteerUser auditor = volunteerUserService.getById(certificate.getAuditorId());
                if (auditor != null) {
                    dto.setAuditorName(auditor.getRealName());
                }
            }
            
            return dto;
        }).collect(Collectors.toList());
        
        // 应用过滤条件
        List<UserCertificateDTO> filteredList = dtoList.stream()
            .filter(dto -> {
                // 技能分类过滤
                if (skillCategoryId != null && !skillCategoryId.equals(dto.getSkillCategoryId())) {
                    return false;
                }
                // 用户名过滤
                if (userName != null && !userName.trim().isEmpty()) {
                    return dto.getUserName() != null && dto.getUserName().contains(userName.trim());
                }
                return true;
            })
            .collect(Collectors.toList());
        
        return PageResult.fromPage(new Page<UserCertificateDTO>(pageNo, pageSize, certificatePage.getTotal()) {{
            setRecords(filteredList);
        }});
    }
    
    @Override
    public UserCertificateDTO getCertificateDetail(Long id) {
        UserCertificate certificate = getById(id);
        if (certificate == null) {
            return null;
        }
        
        UserCertificateDTO dto = new UserCertificateDTO();
        BeanUtils.copyProperties(certificate, dto);
        
        // 设置用户信息
        if (certificate.getUserId() != null) {
            VolunteerUser user = volunteerUserService.getById(certificate.getUserId());
            if (user != null) {
                dto.setUserName(user.getRealName());
                dto.setUserPhone(user.getPhone());
                dto.setUserEmail(user.getEmail());
                dto.setUserIdCard(user.getIdCard());
            }
        }
        
        // 设置技能信息
        if (certificate.getSkillId() != null) {
            UserSkill skill = userSkillService.getById(certificate.getSkillId());
            if (skill != null) {
                dto.setSkillName(skill.getSkillName());
                dto.setSkillLevel(getSkillLevelText(skill.getSkillLevel()));
                dto.setSkillDescription(skill.getDescription());
                
                // 设置技能分类信息
                if (skill.getSkillCategoryId() != null) {
                    SkillCategory category = skillCategoryService.getById(skill.getSkillCategoryId());
                    if (category != null) {
                        dto.setSkillCategoryId(category.getId());
                        dto.setSkillCategoryName(category.getCategoryName());
                    }
                }
            }
        }
        
        // 设置状态文本
        dto.setStatusText(getStatusText(certificate.getStatus()));
        
        // 设置创建时间
        if (certificate.getCreatedTime() != null) {
            dto.setCreatedTime(certificate.getCreatedTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        
        // 设置审核时间
        if (certificate.getAuditTime() != null) {
            dto.setAuditTime(certificate.getAuditTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        
        // 设置审核人姓名
        if (certificate.getAuditorId() != null) {
            VolunteerUser auditor = volunteerUserService.getById(certificate.getAuditorId());
            if (auditor != null) {
                dto.setAuditorName(auditor.getRealName());
            }
        }
        
        return dto;
    }
    
    @Override
    public List<UserCertificate> getExpiredCertificates() {
        LambdaQueryWrapper<UserCertificate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCertificate::getStatus, 2) // 已通过状态
               .le(UserCertificate::getExpiryDate, LocalDate.now())
               .isNotNull(UserCertificate::getExpiryDate);
        
        return list(wrapper);
    }
    
    /**
     * 获取状态文本
     */
    private String getStatusText(Integer status) {
        switch (status) {
            case 1: return "待审核";
            case 2: return "已通过";
            case 3: return "已拒绝";
            default: return "未知";
        }
    }
    
    /**
     * 获取技能等级文本
     */
    private String getSkillLevelText(Integer level) {
        if (level == null) return "未知";
        
        switch (level) {
            case 1: return "初级";
            case 2: return "中级";
            case 3: return "高级";
            case 4: return "专家级";
            default: return "未知";
        }
    }
}
