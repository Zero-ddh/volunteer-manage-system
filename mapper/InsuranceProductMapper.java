package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volunteer.entity.InsuranceProduct;
import org.apache.ibatis.annotations.Mapper;

/**
 * 保险产品Mapper接口
 * 
 * @author volunteer-team
 * @since 2025-01-22
 */
@Mapper
public interface InsuranceProductMapper extends BaseMapper<InsuranceProduct> {
}

