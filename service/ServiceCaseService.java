package com.volunteer.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.volunteer.entity.ServiceCase;

import java.util.List;
import java.util.Map;

/**
 * 服务典型案例Service接口
 * 
 * @author volunteer-team
 * @since 2025-01-22
 */
public interface ServiceCaseService extends IService<ServiceCase> {

    /**
     * 分页查询用户典型案例
     */
    Page<ServiceCase> getUserServiceCases(Long userId, int pageNo, int pageSize, Integer caseType);

    /**
     * 分页查询精选案例
     */
    Page<ServiceCase> getFeaturedCases(int pageNo, int pageSize, Integer caseType);

    /**
     * 分页查询所有案例（管理后台）
     */
    Page<ServiceCase> getAllCases(int pageNo, int pageSize, String keyword, Integer caseType, Integer status);

    /**
     * 创建典型案例
     */
    boolean createServiceCase(ServiceCase serviceCase);

    /**
     * 更新典型案例
     */
    boolean updateServiceCase(ServiceCase serviceCase);

    /**
     * 获取案例详情
     */
    ServiceCase getCaseDetail(Long id);

    /**
     * 增加浏览次数
     */
    boolean incrementViewCount(Long id);

    /**
     * 增加点赞次数
     */
    boolean incrementLikeCount(Long id);

    /**
     * 设置精选状态
     */
    boolean setFeatured(Long id, boolean featured);

    /**
     * 审核案例
     */
    boolean auditCase(Long id, Integer status, String remark);

    /**
     * 获取案例统计信息
     */
    Map<String, Object> getCaseStats(Long userId);
}
