package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volunteer.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 后台管理员用户Mapper接口
 * 
 * @author volunteer-team
 * @since 2025-01-17
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
