package com.volunteer.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.volunteer.entity.RecruitmentAdmission;
import com.volunteer.entity.RecruitmentInfo;
import com.volunteer.vo.RecruitmentAdmissionVO;
import com.volunteer.vo.RecruitmentApplicantVO;

public interface RecruitmentService extends IService<RecruitmentInfo> {
    RecruitmentInfo create(RecruitmentInfo info);
    boolean updateByIdSafe(Long id, RecruitmentInfo info);
    Page<RecruitmentInfo> pageList(String keyword, Integer status, int pageNo, int pageSize);
    RecruitmentInfo findDetail(Long id);
    boolean apply(Long recruitmentId, Long userId, String intro, String skills, String experience);
    Page<RecruitmentAdmission> listApplicants(Long recruitmentId, int pageNo, int pageSize);
    Page<RecruitmentApplicantVO> listApplicantsWithUserInfo(Long recruitmentId, String name, String phone, Integer status, int pageNo, int pageSize);
    Page<RecruitmentAdmission> getUserApplications(Long userId, int pageNo, int pageSize);
    Page<RecruitmentAdmissionVO> getUserApplicationsWithDetails(Long userId, int pageNo, int pageSize);
    boolean reviewApplication(Long admissionId, Integer status, String remark, Long reviewerId);
}


