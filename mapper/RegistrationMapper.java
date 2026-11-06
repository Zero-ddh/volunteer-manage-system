package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volunteer.entity.Registration;
import org.apache.ibatis.annotations.Mapper;

/**
 * 活动报名Mapper接口
 * 
 * @author volunteer-team
 * @since 2025-01-21
 */
@Mapper
public interface RegistrationMapper extends BaseMapper<Registration> {
}