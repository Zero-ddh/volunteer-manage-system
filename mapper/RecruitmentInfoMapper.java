package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.RecruitmentInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RecruitmentInfoMapper extends BaseMapper<RecruitmentInfo> {
    
    /**
     * 分页查询招募列表（包含报名者数量）
     */
    Page<RecruitmentInfo> selectRecruitmentPageWithApplicantCount(Page<RecruitmentInfo> page,
                                                                  @Param("keyword") String keyword,
                                                                  @Param("status") Integer status);
}


