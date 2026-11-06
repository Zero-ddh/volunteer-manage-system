package com.volunteer.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class RecruitmentApplicantVO implements Serializable {
    private Long id;
    private Long recruitmentId;
    private Long userId;
    private String userName; // 用户姓名
    private String phone; // 手机号
    private String email; // 邮箱
    private Integer status; // 1-待审核，2-已通过，3-已拒绝
    private String remark; // 申请备注
    private String skills; // 技能
    private String experience; // 相关经验
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime; // 申请时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime; // 更新时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewTime; // 审核时间
    private String reviewRemark; // 审核备注
    private Integer deleted;
}

