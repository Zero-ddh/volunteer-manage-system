package com.volunteer.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.Registration;
import com.volunteer.vo.RegistrationVO;
import com.volunteer.vo.RegistrationResult;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 活动报名服务接口
 * 
 * @author volunteer-team
 * @since 2025-01-21
 */
public interface RegistrationService {

    /**
     * 报名活动（带状态检查）
     */
    RegistrationResult registerWithStatus(Long activityId, Long userId);

    /**
     * 报名活动
     */
    boolean register(Long activityId, Long userId);

    /**
     * 取消报名
     */
    boolean cancel(Long activityId, Long userId);

    /**
     * 获取我的报名列表
     */
    Page<Registration> myRegistrations(Long userId, int pageNo, int pageSize);

    /**
     * 获取我的报名列表（包含活动信息）
     */
    Page<RegistrationVO> myRegistrationsWithDetails(Long userId, int pageNo, int pageSize);

    /**
     * 获取活动的报名列表
     */
    Page<Registration> activityRegistrations(Long activityId, int pageNo, int pageSize);

    /**
     * 管理后台分页查询报名列表
     */
    Page<Registration> adminPageList(int pageNo, int pageSize, String keyword, Long activityId, Integer status);

    /**
     * 管理后台分页查询报名列表（包含用户和活动信息）
     */
    Page<RegistrationVO> adminPageListWithDetails(int pageNo, int pageSize, String keyword, Long activityId, Integer status);

    /**
     * 管理后台审核报名
     */
    boolean auditRegistration(Long id, Integer status, String remark);

    /**
     * 管理后台批量审核报名
     */
    boolean batchAuditRegistrations(java.util.List<Long> ids, Integer status, String remark);

    /**
     * 获取报名详情（包含用户和活动信息）
     */
    RegistrationVO getRegistrationDetail(Long id);

    /**
     * 生成电子报名表
     */
    void generateRegistrationForm(Long registrationId, HttpServletResponse response) throws IOException;

    /**
     * 检查用户是否已报名并通过审核
     * @param activityId 活动ID
     * @param userId 用户ID
     * @return true-已报名并通过审核，false-未报名或未通过审核
     */
    boolean isUserRegisteredAndApproved(Long activityId, Long userId);
}