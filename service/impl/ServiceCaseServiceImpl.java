package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.volunteer.entity.ServiceCase;
import com.volunteer.mapper.ServiceCaseMapper;
import com.volunteer.service.ServiceCaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务典型案例Service实现类
 * 
 * @author volunteer-team
 * @since 2025-01-22
 */
@Slf4j
@Service
public class ServiceCaseServiceImpl extends ServiceImpl<ServiceCaseMapper, ServiceCase> implements ServiceCaseService {

    @Resource
    private ServiceCaseMapper serviceCaseMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Page<ServiceCase> getUserServiceCases(Long userId, int pageNo, int pageSize, Integer caseType) {
        Page<ServiceCase> page = new Page<>(pageNo, pageSize);
        return serviceCaseMapper.selectUserServiceCases(page, userId, caseType);
    }

    @Override
    public Page<ServiceCase> getFeaturedCases(int pageNo, int pageSize, Integer caseType) {
        Page<ServiceCase> page = new Page<>(pageNo, pageSize);
        return serviceCaseMapper.selectFeaturedCases(page, caseType);
    }

    @Override
    public Page<ServiceCase> getAllCases(int pageNo, int pageSize, String keyword, Integer caseType, Integer status) {
        Page<ServiceCase> page = new Page<>(pageNo, pageSize);
        return serviceCaseMapper.selectAllCases(page, keyword, caseType, status);
    }

    @Override
    @Transactional
    public boolean createServiceCase(ServiceCase serviceCase) {
        try {
            // 设置默认值
            if (serviceCase.getStatus() == null) {
                serviceCase.setStatus(1);
            }
            if (serviceCase.getIsFeatured() == null) {
                serviceCase.setIsFeatured(0);
            }
            if (serviceCase.getViewCount() == null) {
                serviceCase.setViewCount(0);
            }
            if (serviceCase.getLikeCount() == null) {
                serviceCase.setLikeCount(0);
            }
            
            boolean result = save(serviceCase);
            log.info("创建典型案例: ID={}, 结果={}", serviceCase.getId(), result);
            return result;
        } catch (Exception e) {
            log.error("创建典型案例失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean updateServiceCase(ServiceCase serviceCase) {
        try {
            boolean result = updateById(serviceCase);
            log.info("更新典型案例: ID={}, 结果={}", serviceCase.getId(), result);
            return result;
        } catch (Exception e) {
            log.error("更新典型案例失败", e);
            return false;
        }
    }

    @Override
    public ServiceCase getCaseDetail(Long id) {
        ServiceCase serviceCase = getById(id);
        if (serviceCase != null) {
            // 增加浏览次数
            incrementViewCount(id);
        }
        return serviceCase;
    }

    @Override
    @Transactional
    public boolean incrementViewCount(Long id) {
        try {
            int result = serviceCaseMapper.incrementViewCount(id);
            log.info("增加浏览次数: ID={}, 结果={}", id, result);
            return result > 0;
        } catch (Exception e) {
            log.error("增加浏览次数失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean incrementLikeCount(Long id) {
        try {
            int result = serviceCaseMapper.incrementLikeCount(id);
            log.info("增加点赞次数: ID={}, 结果={}", id, result);
            return result > 0;
        } catch (Exception e) {
            log.error("增加点赞次数失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean setFeatured(Long id, boolean featured) {
        try {
            ServiceCase serviceCase = new ServiceCase();
            serviceCase.setId(id);
            serviceCase.setIsFeatured(featured ? 1 : 0);
            boolean result = updateById(serviceCase);
            log.info("设置精选状态: ID={}, 精选={}, 结果={}", id, featured, result);
            return result;
        } catch (Exception e) {
            log.error("设置精选状态失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean auditCase(Long id, Integer status, String remark) {
        try {
            ServiceCase serviceCase = new ServiceCase();
            serviceCase.setId(id);
            serviceCase.setStatus(status);
            // 如果有备注字段，可以在这里设置
            boolean result = updateById(serviceCase);
            log.info("审核案例: ID={}, 状态={}, 结果={}", id, status, result);
            return result;
        } catch (Exception e) {
            log.error("审核案例失败", e);
            return false;
        }
    }

    @Override
    public Map<String, Object> getCaseStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 获取用户案例统计
        List<ServiceCase> caseStats = serviceCaseMapper.selectCaseStats(userId);
        
        int totalCases = caseStats.size();
        int featuredCases = (int) caseStats.stream().filter(c -> c.getIsFeatured() == 1).count();
        int totalViews = caseStats.stream().mapToInt(ServiceCase::getViewCount).sum();
        int totalLikes = caseStats.stream().mapToInt(ServiceCase::getLikeCount).sum();
        
        stats.put("totalCases", totalCases);
        stats.put("featuredCases", featuredCases);
        stats.put("totalViews", totalViews);
        stats.put("totalLikes", totalLikes);
        
        return stats;
    }
}
