package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.volunteer.dto.ActivityRecommendationDTO;
import com.volunteer.entity.Activity;
import com.volunteer.entity.ActivityCategory;
import com.volunteer.entity.Registration;
import com.volunteer.entity.ServiceRecord;
import com.volunteer.entity.VolunteerUser;
import com.volunteer.mapper.ActivityMapper;
import com.volunteer.mapper.ActivityCategoryMapper;
import com.volunteer.mapper.RegistrationMapper;
import com.volunteer.mapper.ServiceRecordMapper;
import com.volunteer.mapper.VolunteerUserMapper;
import com.volunteer.service.ActivityRecommendationService;
import com.volunteer.service.ActivitySkillRequirementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 活动推荐服务实现类
 * 
 * @author volunteer-team
 * @since 2025-01-27
 */
@Slf4j
@Service
public class ActivityRecommendationServiceImpl implements ActivityRecommendationService {
    
    @Resource
    private ActivityMapper activityMapper;
    
    @Resource
    private VolunteerUserMapper volunteerUserMapper;
    
    @Resource
    private RegistrationMapper registrationMapper;
    
    @Resource
    private ServiceRecordMapper serviceRecordMapper;
    
    @Resource
    private ActivitySkillRequirementService activitySkillRequirementService;
    
    @Resource
    private ActivityCategoryMapper activityCategoryMapper;
    
    // 省份关键词
    private static final String[] PROVINCES = {
        "北京", "上海", "天津", "重庆", "河北", "山西", "辽宁", 
        "吉林", "黑龙江", "江苏", "浙江", "安徽", "福建", "江西", 
        "山东", "河南", "湖北", "湖南", "广东", "海南", "四川", 
        "贵州", "云南", "陕西", "甘肃", "青海", "台湾", "广西", 
        "宁夏", "新疆", "西藏", "香港", "澳门"
    };
    
    @Override
    public List<ActivityRecommendationDTO> recommendActivities(Long userId,
                                                              Integer pageNo,
                                                              Integer pageSize,
                                                              Long categoryId,
                                                              String location) {
        // 注意：location 参数保留用于接口兼容性，但已不再用于推荐算法计算
        
        // 1. 构建查询条件
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        
        // 查询已发布和进行中的活动
        wrapper.in(Activity::getStatus, Arrays.asList(2, 3));
        
        // 分类筛选
        if (categoryId != null) {
            wrapper.eq(Activity::getCategoryId, categoryId);
        }
        
        // 排除用户已报名的活动
        List<Registration> registrations = registrationMapper.selectList(
            new LambdaQueryWrapper<Registration>()
                .eq(Registration::getUserId, userId)
                .eq(Registration::getDeleted, 0)
        );
        List<Long> registeredActivityIds = registrations.stream()
            .map(Registration::getActivityId)
            .collect(Collectors.toList());
        
        if (!registeredActivityIds.isEmpty()) {
            wrapper.notIn(Activity::getId, registeredActivityIds);
        }
        
        // 查询活动
        List<Activity> activities = activityMapper.selectList(wrapper);
        
        // 3. 计算每个活动的匹配分数
        List<ActivityRecommendationDTO> recommendations = activities.stream()
            .map(activity -> {
                ActivityRecommendationDTO dto = new ActivityRecommendationDTO();
                
                // 复制活动信息
                BeanUtils.copyProperties(activity, dto);
                
                // 计算各维度分数
                double skillScore = activitySkillRequirementService.calculateSkillMatchScore(userId, activity.getId());
                double categoryScore = calculateCategoryPreferenceScore(userId, activity.getCategoryId());
                
                // 设置各维度分数
                dto.setSkillMatchScore(skillScore);
                dto.setLocationMatchScore(0.0); // 地址匹配已移除
                dto.setCategoryMatchScore(categoryScore);
                dto.setHistoryMatchScore(0.0); // 历史匹配已被服务类型匹配替代
                
                // 获取技能匹配的关键词
                List<String> skillKeywords = activitySkillRequirementService.getSkillMatchKeywords(userId, activity.getId());
                dto.setSkillMatchKeywords(skillKeywords);
                
                // 获取历史活动类型匹配的关键词（活动分类名称）
                List<String> categoryKeywords = getHistoryCategoryMatchKeywords(userId, activity.getCategoryId());
                dto.setCategoryMatchKeywords(categoryKeywords);
                
                // 计算综合匹配分数（权重分配：技能70% + 历史活动类型30%）
                double matchScore = skillScore * 0.7 + categoryScore * 0.3;
                dto.setMatchScore(matchScore);
                
                // 设置推荐理由
                dto.setRecommendationReason(generateRecommendationReason(dto));
                
                // 设置当前参与人数
                dto.setTotalParticipants(activity.getCurrentParticipants());
                
                return dto;
            })
            .sorted((a, b) -> Double.compare(b.getMatchScore(), a.getMatchScore()))
            .collect(Collectors.toList());
        
        return recommendations;
    }
    
    @Override
    public double calculateComprehensiveMatchScore(Long userId, Long activityId) {
        VolunteerUser user = volunteerUserMapper.selectById(userId);
        Activity activity = activityMapper.selectById(activityId);
        
        if (user == null || activity == null) {
            return 0.0;
        }
        
        // 技能匹配度 (70%)
        double skillScore = activitySkillRequirementService.calculateSkillMatchScore(userId, activityId);
        
        // 历史活动类型匹配度 (30%)
        double categoryScore = calculateCategoryPreferenceScore(userId, activity.getCategoryId());
        
        // 加权平均：技能70% + 历史活动类型30%
        return skillScore * 0.7 + categoryScore * 0.3;
    }
    
    @Override
    public double calculateLocationMatchScore(String userAddress, String activityLocation) {
        if (userAddress == null || activityLocation == null) {
            return 50.0; // 如果地址为空，给中等分数
        }
        
        // 提取地址关键词
        Set<String> userKeywords = extractAddressKeywords(userAddress);
        Set<String> activityKeywords = extractAddressKeywords(activityLocation);
        
        if (userKeywords.isEmpty() && activityKeywords.isEmpty()) {
            return 50.0;
        }
        
        // 计算Jaccard相似度
        double similarity = calculateJaccardSimilarity(userKeywords, activityKeywords);
        
        // 转换为0-100分数
        return similarity * 100;
    }
    
    /**
     * 提取地址关键词
     */
    private Set<String> extractAddressKeywords(String address) {
        Set<String> keywords = new HashSet<>();
        
        // 一级关键词：省份/直辖市
        for (String province : PROVINCES) {
            if (address.contains(province)) {
                keywords.add(province);
            }
        }
        
        // 二级关键词：城市/区
        Pattern pattern = Pattern.compile("([\\u4e00-\\u9fa5]{2,}(?:市|区|县|镇|街道))");
        Matcher matcher = pattern.matcher(address);
        while (matcher.find()) {
            keywords.add(matcher.group(1));
        }
        
        // 常见街道社区
        pattern = Pattern.compile("([\\u4e00-\\u9fa5]+(?:社区|村|路|街))");
        matcher = pattern.matcher(address);
        while (matcher.find()) {
            keywords.add(matcher.group(1));
        }
        
        return keywords;
    }
    
    /**
     * 计算Jaccard相似度
     */
    private double calculateJaccardSimilarity(Set<String> set1, Set<String> set2) {
        if (set1.isEmpty() && set2.isEmpty()) {
            return 1.0;
        }
        if (set1.isEmpty() || set2.isEmpty()) {
            return 0.0;
        }
        
        // 交集
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        
        // 并集
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        
        return (double) intersection.size() / union.size();
    }
    
    /**
     * 计算服务类型偏好分数
     */
    @Override
    public double calculateCategoryPreferenceScore(Long userId, Long categoryId) {
        // 查询用户历史参与的活动
        List<ServiceRecord> records = serviceRecordMapper.selectList(
            new LambdaQueryWrapper<ServiceRecord>()
                .eq(ServiceRecord::getUserId, userId)
                .eq(ServiceRecord::getDeleted, 0)
        );
        
        if (records.isEmpty()) {
            return 100.0; // 无历史记录，默认返回100%
        }
        
        // 统计用户参与的各类活动数量
        List<Long> participatedActivityIds = records.stream()
            .map(ServiceRecord::getActivityId)
            .distinct()
            .collect(Collectors.toList());
        
        if (participatedActivityIds.isEmpty()) {
            return 100.0; // 无历史记录，默认返回100%
        }
        
        // 查询这些活动的分类
        List<Activity> activities = activityMapper.selectList(
            new LambdaQueryWrapper<Activity>()
                .in(Activity::getId, participatedActivityIds)
        );
        
        // 统计用户最常参与的分类
        Map<Long, Long> categoryCount = activities.stream()
            .collect(Collectors.groupingBy(Activity::getCategoryId, Collectors.counting()));
        
        Long count = categoryCount.get(categoryId);
        if (count == null || count == 0) {
            return 0.0; // 用户没有参与过该类型的活动，返回0%
        }
        
        // 根据参与次数计算分数
        long maxCount = categoryCount.values().stream().mapToLong(Long::longValue).max().orElse(1);
        // 分数范围：0%-100%，根据用户该分类参与次数占用户参与最多分类次数的比例计算
        double score = (double) count / maxCount * 100.0;
        return Math.min(100.0, Math.max(0.0, score));
    }
    
    /**
     * 获取历史活动类型匹配的关键词（活动分类名称）
     */
    @Override
    public List<String> getHistoryCategoryMatchKeywords(Long userId, Long categoryId) {
        try {
            // 查询用户历史参与的活动
            List<ServiceRecord> records = serviceRecordMapper.selectList(
                new LambdaQueryWrapper<ServiceRecord>()
                    .eq(ServiceRecord::getUserId, userId)
                    .eq(ServiceRecord::getDeleted, 0)
            );
            
            if (records.isEmpty()) {
                return new ArrayList<>();
            }
            
            // 获取用户参与过的活动ID列表
            List<Long> participatedActivityIds = records.stream()
                .map(ServiceRecord::getActivityId)
                .distinct()
                .collect(Collectors.toList());
            
            if (participatedActivityIds.isEmpty()) {
                return new ArrayList<>();
            }
            
            // 查询这些活动的分类
            List<Activity> activities = activityMapper.selectList(
                new LambdaQueryWrapper<Activity>()
                    .in(Activity::getId, participatedActivityIds)
            );
            
            // 检查用户历史是否有相同分类的活动
            boolean hasSameCategory = activities.stream()
                .anyMatch(a -> a.getCategoryId().equals(categoryId));
            
            if (hasSameCategory) {
                // 获取当前活动的分类名称
                ActivityCategory category = activityCategoryMapper.selectById(categoryId);
                if (category != null) {
                    List<String> keywords = new ArrayList<>();
                    keywords.add(category.getCategoryName());
                    return keywords;
                }
            }
            
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("获取历史活动类型匹配关键词失败 - userId: {}, categoryId: {}", userId, categoryId, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public double calculateHistorySimilarityScore(Long userId, Long activityId) {
        // 此方法已被 getHistoryCategoryMatchKeywords 和 calculateCategoryPreferenceScore 替代
        // 保留此方法以保持接口兼容性
        return calculateCategoryPreferenceScore(userId, activityId);
    }
    
    /**
     * 计算时间可用性
     */
    private double calculateTimeAvailabilityScore(Activity activity) {
        // 检查活动时间是否在未来
        if (activity.getStartTime() != null && 
            activity.getStartTime().isAfter(java.time.LocalDateTime.now())) {
            return 100.0;
        }
        return 30.0;
    }
    
    /**
     * 生成推荐理由
     */
    private String generateRecommendationReason(ActivityRecommendationDTO dto) {
        List<String> reasons = new ArrayList<>();
        
        if (dto.getSkillMatchScore() >= 80) {
            reasons.add("您的技能与活动要求高度匹配");
        } else if (dto.getSkillMatchScore() >= 60) {
            reasons.add("您的技能基本满足活动要求");
        }
        
        if (dto.getCategoryMatchScore() >= 70) {
            reasons.add("符合您常参与的活动类型");
        }
        
        if (dto.getHistoryMatchScore() >= 70) {
            reasons.add("类似您之前参与的活动");
        }
        
        if (reasons.isEmpty()) {
            return "根据您的个人资料推荐";
        }
        
        return String.join("，", reasons);
    }
}
