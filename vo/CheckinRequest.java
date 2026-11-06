package com.volunteer.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 签到请求VO
 * 
 * @author volunteer-team
 * @since 2025-01-21
 */
@Data
public class CheckinRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 签到位置（GPS坐标）
     */
    private String location;

    /**
     * 二维码内容
     */
    private String qrCode;

    /**
     * 备注
     */
    private String remark;
}
