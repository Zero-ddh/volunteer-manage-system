package com.volunteer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.volunteer.entity.PointsRuleConfig;

import java.util.List;
import java.util.Map;

/**
 * 积分规则配置Service接口
 * 
 * @author volunteer-team
 * @since 2025-01-22
 */
public interface PointsRuleConfigService extends IService<PointsRuleConfig> {
    
    /**
     * 获取启用的积分规则
     */
    List<PointsRuleConfig> getActiveRules();
    
    /**
     * 根据规则类型获取积分规则
     */
    PointsRuleConfig getRuleByType(Integer ruleType);
    
    /**
     * 更新积分规则
     */
    boolean updateRule(PointsRuleConfig rule);
    
    /**
     * 启用/禁用积分规则
     */
    boolean toggleRuleStatus(Long id, Integer status);
    
    /**
     * 手动生成积分数据
     * @return 生成结果统计
     */
    Map<String, Object> generatePointsData();
}
