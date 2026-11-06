package com.volunteer.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 签到详情VO
 * 
 * @author volunteer-team
 * @since 2025-01-22
 */
@Data
public class CheckinDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 签到ID
     */
    private Long id;

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户姓名
     */
    private String userName;

    /**
     * 用户手机号
     */
    private String userPhone;

    /**
     * 签到时间
     */
    private LocalDateTime checkinTime;

    /**
     * 签退时间
     */
    private LocalDateTime checkoutTime;

    /**
     * 签到地点
     */
    private String checkinLocation;

    /**
     * 签退地点
     */
    private String checkoutLocation;

    /**
     * 服务时长（统一使用actual_service_hours）
     */
    private BigDecimal serviceHours;

    /**
     * 签到状态
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 活动开始时间
     */
    private LocalDateTime activityStartTime;

    /**
     * 活动结束时间
     */
    private LocalDateTime activityEndTime;

    /**
     * 活动标题
     */
    private String activityTitle;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}
