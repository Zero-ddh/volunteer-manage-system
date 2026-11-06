package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volunteer.entity.CommunityPost;
import org.apache.ibatis.annotations.Mapper;

/**
 * 社群帖子Mapper接口
 * 
 * @author volunteer-team
 * @since 2025-01-25
 */
@Mapper
public interface CommunityPostMapper extends BaseMapper<CommunityPost> {
}
