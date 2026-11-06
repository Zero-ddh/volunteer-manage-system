package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volunteer.entity.Community;
import org.apache.ibatis.annotations.Mapper;

/**
 * 社群Mapper接口
 * 
 * @author volunteer-team
 * @since 2025-01-25
 */
@Mapper
public interface CommunityMapper extends BaseMapper<Community> {
}
