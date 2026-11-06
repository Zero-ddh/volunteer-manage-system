package com.volunteer.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.Activity;

import java.util.List;

/**
 * 活动服务接口
 */
public interface ActivityService {

    /**
     * C端分页查询活动列表
     */
    Page<Activity> pageList(int pageNo, int pageSize, String keyword, Long categoryId, Integer status,
                           String location, String startTimeBegin, String startTimeEnd);

    /**
     * 管理后台分页查询活动列表
     */
    Page<Activity> adminPageList(int pageNo, int pageSize, String keyword, Long categoryId, Integer status, Long organizerId);

    /**
     * 根据ID获取活动详情
     */
    Activity getById(Long id);

    /**
     * 创建活动
     */
    boolean create(Activity activity);

    /**
     * 更新活动
     */
    boolean update(Activity activity);

    /**
     * 删除活动
     */
    boolean delete(Long id);

    /**
     * 删除活动（带报名记录检查）
     */
    String deleteWithCheck(Long id);

    /**
     * 批量删除活动
     */
    boolean batchDelete(List<Long> ids);

    /**
     * 更新活动状态
     */
    boolean updateStatus(Long id, Integer status);

    /**
     * 批量更新活动状态
     */
    boolean batchUpdateStatus(List<Long> ids, Integer status);

    /**
     * 导出活动数据
     */
    String exportActivities(String keyword, Long categoryId, Integer status);

    /**
     * 获取用户参与的活动列表
     */
    List<Activity> getUserParticipatedActivities(Long userId);
}


