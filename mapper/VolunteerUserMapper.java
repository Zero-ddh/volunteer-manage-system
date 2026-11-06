package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.dto.VolunteerUserQueryDTO;
import com.volunteer.entity.VolunteerUser;
import com.volunteer.vo.VolunteerStatisticsVO;
import com.volunteer.vo.VolunteerUserListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 志愿者用户Mapper接口
 * 
 * @author volunteer-team
 * @since 2025-01-17
 */
@Mapper
public interface VolunteerUserMapper extends BaseMapper<VolunteerUser> {
    
    /**
     * 分页查询志愿者用户列表
     */
    Page<VolunteerUserListVO> selectVolunteerList(Page<VolunteerUserListVO> page, @Param("query") VolunteerUserQueryDTO query);
    
    /**
     * 获取志愿者统计信息
     */
    VolunteerStatisticsVO selectVolunteerStatistics();
    
    /**
     * 批量更新志愿者状态
     */
    int batchUpdateStatus(@Param("userIds") List<Long> userIds, @Param("status") Integer status);
}
