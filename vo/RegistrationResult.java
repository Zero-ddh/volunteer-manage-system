package com.volunteer.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 报名结果封装类
 * 
 * @author volunteer-team
 * @since 2025-01-21
 */
@Data
public class RegistrationResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 提示消息
     */
    private String message;

    /**
     * 当前报名状态：1-待审核，2-已通过，3-已拒绝，4-已取消
     */
    private Integer status;

    /**
     * 报名记录ID
     */
    private Long registrationId;

    public RegistrationResult() {
    }

    public RegistrationResult(boolean success, String message, Integer status, Long registrationId) {
        this.success = success;
        this.message = message;
        this.status = status;
        this.registrationId = registrationId;
    }

    /**
     * 成功结果
     */
    public static RegistrationResult success(String message, Integer status) {
        return new RegistrationResult(true, message, status, null);
    }

    /**
     * 成功结果（带报名ID）
     */
    public static RegistrationResult success(String message, Integer status, Long registrationId) {
        return new RegistrationResult(true, message, status, registrationId);
    }

    /**
     * 失败结果
     */
    public static RegistrationResult failure(String message, Integer status) {
        return new RegistrationResult(false, message, status, null);
    }
}
