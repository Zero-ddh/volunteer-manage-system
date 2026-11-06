package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.Checkin;
import com.volunteer.vo.CheckinDetailVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

@Mapper
public interface CheckinMapper extends BaseMapper<Checkin> {
    
    /**
     * 分页查询签到详情列表（包含用户信息）
     */
    Page<CheckinDetailVO> selectCheckinDetailPage(Page<CheckinDetailVO> page, 
                                                  @Param("keyword") String keyword,
                                                  @Param("activityId") Long activityId, 
                                                  @Param("status") Integer status);

    /**
     * 获取用户签到记录（包含活动信息）
     */
    Page<CheckinDetailVO> selectUserCheckinsWithActivity(Page<CheckinDetailVO> page, 
                                                          @Param("userId") Long userId,
                                                          @Param("activityTitle") String activityTitle);
    
    /**
     * 统计用户总服务时长（从签到表）
     */
    BigDecimal selectTotalServiceHoursByUserId(@Param("userId") Long userId);
}


