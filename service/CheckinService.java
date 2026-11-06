package com.volunteer.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.volunteer.entity.Checkin;
import com.volunteer.vo.CheckinDetailVO;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface CheckinService extends IService<Checkin> {

    /**
     * 活动签到
     */
    boolean checkin(Long activityId, Long userId, String location, String remark);

    /**
     * 活动签退
     */
    boolean checkout(Long activityId, Long userId, String location, String remark);

    /**
     * 获取用户签到记录
     */
    Page<Checkin> myCheckins(Long userId, int pageNo, int pageSize);

    /**
     * 获取用户签到记录（包含活动信息）
     */
    Page<CheckinDetailVO> getUserCheckinsWithActivity(Long userId, int pageNo, int pageSize, String activityTitle);

    /**
     * 验证GPS位置
     */
    boolean validateLocation(Long activityId, String location);

    /**
     * 验证二维码
     */
    boolean validateQRCode(Long activityId, String qrCode);

    /**
     * 生成活动二维码
     */
    String generateQRCode(Long activityId);

    /**
     * 管理后台分页查询签到列表
     */
    Page<Checkin> adminPageList(int pageNo, int pageSize, String keyword, Long activityId, Integer status);

    /**
     * 管理后台分页查询签到详情列表（包含用户信息）
     */
    Page<CheckinDetailVO> adminPageListWithUserInfo(int pageNo, int pageSize, String keyword, Long activityId, Integer status);

    /**
     * 更新服务时长
     */
    boolean updateServiceHours(Long checkinId, Double serviceHours);

    /**
     * 基于签到签退时间自动计算服务时长
     */
    boolean autoCalculateServiceHoursFromCheckin(Long checkinId);

    /**
     * 更新签到状态
     */
    boolean updateCheckinStatus(Long checkinId, Integer status, String remark);

    /**
     * 批量更新服务时长
     */
    boolean batchUpdateServiceHours(List<Long> checkinIds, Double serviceHours, String remark);

    /**
     * 检查用户今天是否已签到指定活动
     */
    boolean hasCheckedInToday(Long activityId, Long userId);

    /**
     * 获取用户今天的签到记录
     */
    Checkin getTodayCheckin(Long activityId, Long userId);

    /**
     * 导出签到数据
     */
    void exportCheckins(String keyword, Long activityId, Integer status, HttpServletResponse response) throws IOException;

    /**
     * 检查用户是否已报名并通过审核
     * @param activityId 活动ID
     * @param userId 用户ID
     * @return true-已报名并通过审核，false-未报名或未通过审核
     */
    boolean validateUserRegistration(Long activityId, Long userId);

    /**
     * 验证签退时间是否与签到时间为同一天
     * @param activityId 活动ID
     * @param userId 用户ID
     * @return true-同一天，false-不是同一天
     */
    boolean validateCheckoutSameDay(Long activityId, Long userId);

    /**
     * 更新用户总服务时长（从签到表重新统计）
     * @param userIds 用户ID列表
     * @return 更新结果统计
     */
    Map<String, Object> updateUsersTotalServiceHours(List<Long> userIds);
}


