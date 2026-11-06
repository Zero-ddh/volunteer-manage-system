package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.volunteer.entity.Checkin;
import com.volunteer.entity.PointsRuleConfig;
import com.volunteer.entity.UserPoints;
import com.volunteer.mapper.PointsRuleConfigMapper;
import com.volunteer.service.CheckinService;
import com.volunteer.service.PointsRuleConfigService;
import com.volunteer.service.UserPointsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PointsRuleConfigServiceImpl extends ServiceImpl<PointsRuleConfigMapper, PointsRuleConfig> implements PointsRuleConfigService {
    
    @Resource
    @Lazy
    private UserPointsService userPointsService;
    
    @Resource
    @Lazy
    private CheckinService checkinService;
    
    @Override
    public List<PointsRuleConfig> getActiveRules() {
        LambdaQueryWrapper<PointsRuleConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsRuleConfig::getStatus, 1)
               .orderByAsc(PointsRuleConfig::getRuleType);
        return list(wrapper);
    }
    
    @Override
    public PointsRuleConfig getRuleByType(Integer ruleType) {
        LambdaQueryWrapper<PointsRuleConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsRuleConfig::getRuleType, ruleType)
               .eq(PointsRuleConfig::getStatus, 1);
        return getOne(wrapper);
    }
    
    @Override
    public boolean updateRule(PointsRuleConfig rule) {
        try {
            return updateById(rule);
        } catch (Exception e) {
            log.error("更新积分规则失败: ruleId=" + rule.getId(), e);
            return false;
        }
    }
    
    @Override
    public boolean toggleRuleStatus(Long id, Integer status) {
        try {
            PointsRuleConfig rule = new PointsRuleConfig();
            rule.setId(id);
            rule.setStatus(status);
            return updateById(rule);
        } catch (Exception e) {
            log.error("切换积分规则状态失败: ruleId=" + id + ", status=" + status, e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public Map<String, Object> generatePointsData() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 获取启用的服务时长积分规则
            PointsRuleConfig serviceRule = getRuleByType(1);
            if (serviceRule == null || serviceRule.getStatus() != 1) {
                result.put("success", false);
                result.put("message", "服务时长积分规则未启用");
                return result;
            }
            
            // 2. 获取所有有实际服务时长的签到记录
            List<Checkin> checkinRecords = checkinService.list(
                new LambdaQueryWrapper<Checkin>()
                    .eq(Checkin::getDeleted, 0)
                    .isNotNull(Checkin::getActualServiceHours)
                    .gt(Checkin::getActualServiceHours, 0)
            );
            
            if (checkinRecords.isEmpty()) {
                result.put("success", false);
                result.put("message", "没有找到有效的签到记录");
                return result;
            }
            
            // 3. 按用户分组统计积分
            Map<Long, BigDecimal> userPointsMap = new HashMap<>();
            Map<Long, BigDecimal> userServiceHoursMap = new HashMap<>();
            
            for (Checkin checkin : checkinRecords) {
                Long userId = checkin.getUserId();
                BigDecimal actualServiceHours = checkin.getActualServiceHours();
                
                // 计算积分
                BigDecimal points = actualServiceHours.multiply(serviceRule.getPointsPerUnit());
                
                // 累计积分和服务时长
                userPointsMap.merge(userId, points, BigDecimal::add);
                userServiceHoursMap.merge(userId, actualServiceHours, BigDecimal::add);
            }
            
            // 4. 处理每条签到记录，支持update和insert
            int successCount = 0;
            int failCount = 0;
            int updateCount = 0;
            int insertCount = 0;
            
            for (Checkin checkin : checkinRecords) {
                try {
                    Long userId = checkin.getUserId();
                    Long activityId = checkin.getActivityId();
                    BigDecimal actualServiceHours = checkin.getActualServiceHours();
                    
                    // 计算该记录的积分
                    BigDecimal points = actualServiceHours.multiply(serviceRule.getPointsPerUnit());
                    
                    // 检查是否已存在该活动的积分记录
                    UserPoints existingPoints = userPointsService.getOne(
                        new LambdaQueryWrapper<UserPoints>()
                            .eq(UserPoints::getActivityId, activityId)
                            .eq(UserPoints::getUserId, userId)
                            .eq(UserPoints::getSource, "服务时长")
                    );
                    
                    if (existingPoints != null) {
                        // 更新现有积分记录 - 先删除旧记录，再添加新记录
                        boolean deleted = userPointsService.removeById(existingPoints.getId());
                        if (deleted) {
                            // 使用addPointsRecord方法添加积分记录，会自动同步更新volunteer_user表
                            boolean added = userPointsService.addPointsRecord(
                                userId, 
                                points.intValue(), 
                                1, // 1-获得积分
                                "服务时长", 
                                String.format("服务时长 %.1f 小时，获得积分 %d", 
                                    actualServiceHours.doubleValue(), points.intValue())
                            );
                            
                            if (added) {
                                // 更新积分记录的activity_id
                                UserPoints newPointsRecord = userPointsService.getOne(
                                    new LambdaQueryWrapper<UserPoints>()
                                        .eq(UserPoints::getUserId, userId)
                                        .eq(UserPoints::getSource, "服务时长")
                                        .eq(UserPoints::getPoints, points.intValue())
                                        .orderByDesc(UserPoints::getCreatedTime)
                                        .last("LIMIT 1")
                                );
                                if (newPointsRecord != null) {
                                    newPointsRecord.setActivityId(activityId);
                                    userPointsService.updateById(newPointsRecord);
                                }
                                
                                updateCount++;
                                successCount++;
                                log.debug("更新积分记录成功: activityId=" + activityId + ", userId=" + userId + ", points=" + points.intValue());
                            } else {
                                failCount++;
                                log.error("更新积分记录失败: activityId=" + activityId);
                            }
                        } else {
                            failCount++;
                            log.error("删除旧积分记录失败: activityId=" + activityId);
                        }
                    } else {
                        // 使用addPointsRecord方法添加积分记录，会自动同步更新volunteer_user表
                        boolean added = userPointsService.addPointsRecord(
                            userId, 
                            points.intValue(), 
                            1, // 1-获得积分
                            "服务时长", 
                            String.format("服务时长 %.1f 小时，获得积分 %d", 
                                actualServiceHours.doubleValue(), points.intValue())
                        );
                        
                        if (added) {
                            // 更新积分记录的activity_id
                            UserPoints newPointsRecord = userPointsService.getOne(
                                new LambdaQueryWrapper<UserPoints>()
                                    .eq(UserPoints::getUserId, userId)
                                    .eq(UserPoints::getSource, "服务时长")
                                    .eq(UserPoints::getPoints, points.intValue())
                                    .orderByDesc(UserPoints::getCreatedTime)
                                    .last("LIMIT 1")
                            );
                            if (newPointsRecord != null) {
                                newPointsRecord.setActivityId(activityId);
                                userPointsService.updateById(newPointsRecord);
                            }
                            
                            insertCount++;
                            successCount++;
                            log.debug("创建积分记录成功: activityId=" + activityId + ", userId=" + userId + ", points=" + points.intValue());
                        } else {
                            failCount++;
                            log.error("创建积分记录失败: activityId=" + activityId);
                        }
                    }
                    
                } catch (Exception e) {
                    log.error("处理签到记录积分失败: checkinId=" + checkin.getId(), e);
                    failCount++;
                }
            }
            
            result.put("success", true);
            result.put("message", String.format("积分数据生成完成，成功: %d, 失败: %d, 新增: %d, 更新: %d", 
                successCount, failCount, insertCount, updateCount));
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("insertCount", insertCount);
            result.put("updateCount", updateCount);
            result.put("totalUsers", userPointsMap.size());
            result.put("totalRecords", checkinRecords.size());
            
        } catch (Exception e) {
            log.error("生成积分数据异常", e);
            result.put("success", false);
            result.put("message", "生成积分数据异常: " + e.getMessage());
        }
        
        return result;
    }
}
