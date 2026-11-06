package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.RecruitmentAdmission;
import com.volunteer.vo.RecruitmentApplicantVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RecruitmentAdmissionMapper extends BaseMapper<RecruitmentAdmission> {
    
    /**
     * 分页查询招募报名者（包含用户信息）
     */
    Page<RecruitmentApplicantVO> selectApplicantsWithUserInfo(Page<RecruitmentApplicantVO> page,
                                                              @Param("recruitmentId") Long recruitmentId,
                                                              @Param("name") String name,
                                                              @Param("phone") String phone,
                                                              @Param("status") Integer status);
}


