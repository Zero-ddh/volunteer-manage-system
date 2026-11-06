package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.ServiceCase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 服务典型案例Mapper接口
 * 
 * @author volunteer-team
 * @since 2025-01-22
 */
@Mapper
public interface ServiceCaseMapper extends BaseMapper<ServiceCase> {

    /**
     * 分页查询用户典型案例
     */
    Page<ServiceCase> selectUserServiceCases(Page<ServiceCase> page, @Param("userId") Long userId, @Param("caseType") Integer caseType);

    /**
     * 分页查询精选案例
     */
    Page<ServiceCase> selectFeaturedCases(Page<ServiceCase> page, @Param("caseType") Integer caseType);

    /**
     * 分页查询所有案例（管理后台）
     */
    Page<ServiceCase> selectAllCases(Page<ServiceCase> page, @Param("keyword") String keyword, @Param("caseType") Integer caseType, @Param("status") Integer status);

    /**
     * 增加浏览次数
     */
    int incrementViewCount(@Param("id") Long id);

    /**
     * 增加点赞次数
     */
    int incrementLikeCount(@Param("id") Long id);

    /**
     * 获取案例统计信息
     */
    List<ServiceCase> selectCaseStats(@Param("userId") Long userId);
}
