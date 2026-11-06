package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.volunteer.dto.UserSkillDTO;
import com.volunteer.entity.SkillCategory;
import com.volunteer.entity.UserCertificate;
import com.volunteer.entity.UserSkill;
import com.volunteer.mapper.UserSkillMapper;
import com.volunteer.service.SkillCategoryService;
import com.volunteer.service.UserCertificateService;
import com.volunteer.service.UserSkillService;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户技能Service实现类
 * 
 * @author volunteer-team
 * @since 2025-01-20
 */
@Service
public class UserSkillServiceImpl extends ServiceImpl<UserSkillMapper, UserSkill> implements UserSkillService {
    
    @Resource
    private SkillCategoryService skillCategoryService;
    
    @Resource
    @Lazy
    private UserCertificateService userCertificateService;
    
    @Override
    public List<UserSkillDTO> getUserSkills(Long userId) {
        LambdaQueryWrapper<UserSkill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSkill::getUserId, userId)
               .eq(UserSkill::getStatus, 1)
               .orderByDesc(UserSkill::getCreatedTime);
        
        List<UserSkill> skills = list(wrapper);
        
        return skills.stream().map(skill -> {
            UserSkillDTO dto = new UserSkillDTO();
            BeanUtils.copyProperties(skill, dto);
            
            // 设置技能分类名称
            if (skill.getSkillCategoryId() != null) {
                SkillCategory category = skillCategoryService.getById(skill.getSkillCategoryId());
                if (category != null) {
                    dto.setSkillCategoryName(category.getCategoryName());
                }
            }
            
            // 设置证书信息
            if (skill.getCertificateId() != null) {
                UserCertificate certificate = userCertificateService.getById(skill.getCertificateId());
                if (certificate != null) {
                    dto.setCertificateName(certificate.getCertificateName());
                    dto.setCertificateStatus(certificate.getStatus());
                }
            }
            
            return dto;
        }).collect(Collectors.toList());
    }
    
    @Override
    public List<UserSkillDTO> getUserSkillsByCategory(Long userId, Long categoryId) {
        LambdaQueryWrapper<UserSkill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSkill::getUserId, userId);
        
        // 如果categoryId不为null，则按分类查询；否则查询所有分类
        if (categoryId != null) {
            wrapper.eq(UserSkill::getSkillCategoryId, categoryId);
        }
        
        wrapper.eq(UserSkill::getStatus, 1)
               .orderByDesc(UserSkill::getCreatedTime);
        
        List<UserSkill> skills = list(wrapper);
        
        return skills.stream().map(skill -> {
            UserSkillDTO dto = new UserSkillDTO();
            BeanUtils.copyProperties(skill, dto);
            
            // 设置技能分类名称
            SkillCategory category = skillCategoryService.getById(skill.getSkillCategoryId());
            if (category != null) {
                dto.setSkillCategoryName(category.getCategoryName());
            }
            
            return dto;
        }).collect(Collectors.toList());
    }
    
    @Override
    public boolean updateSkillVerified(Long skillId, Integer verified) {
        UserSkill skill = getById(skillId);
        if (skill == null) {
            return false;
        }
        skill.setVerified(verified);
        return updateById(skill);
    }
}