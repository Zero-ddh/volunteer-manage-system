package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.volunteer.entity.Activity;
import com.volunteer.entity.PointsRuleConfig;
import com.volunteer.entity.ServiceRecord;
import com.volunteer.entity.UserPoints;
import com.volunteer.entity.VolunteerUser;
import com.volunteer.mapper.ServiceRecordMapper;
import com.volunteer.service.ActivityService;
import com.volunteer.service.CheckinService;
import com.volunteer.service.PointsRuleConfigService;
import com.volunteer.service.UserPointsService;
import com.volunteer.service.VolunteerUserService;
import com.volunteer.service.ServiceRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 服务记录Service实现类
 * 
 * @author volunteer-team
 * @since 2025-01-22
 */
@Slf4j
@Service
public class ServiceRecordServiceImpl extends ServiceImpl<ServiceRecordMapper, ServiceRecord> implements ServiceRecordService {

    @Resource
    private ServiceRecordMapper serviceRecordMapper;

    @Resource
    private ActivityService activityService;

    @Resource
    private CheckinService checkinService;

    @Resource
    private PointsRuleConfigService pointsRuleConfigService;

    @Resource
    private UserPointsService userPointsService;
    
    @Resource
    private VolunteerUserService volunteerUserService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Page<ServiceRecord> getUserServiceRecords(Long userId, int pageNo, int pageSize) {
        Page<ServiceRecord> page = new Page<>(pageNo, pageSize);
        return serviceRecordMapper.selectUserServiceRecordsWithActivity(page, userId);
    }

    @Override
    public ServiceRecord getServiceRecordDetail(Long id) {
        return getById(id);
    }

    @Override
    @Transactional
    public boolean createServiceRecord(ServiceRecord serviceRecord) {
        try {
            log.info("开始创建服务记录: userId={}, activityId={}, serviceDate={}, serviceHours={}", 
                serviceRecord.getUserId(), serviceRecord.getActivityId(), 
                serviceRecord.getServiceDate(), serviceRecord.getServiceHours());
            
            // 设置默认值
            if (serviceRecord.getStatus() == null) {
                serviceRecord.setStatus(1);
            }
            if (serviceRecord.getServiceQuality() == null) {
                serviceRecord.setServiceQuality(5);
            }
            if (serviceRecord.getServiceType() == null) {
                serviceRecord.setServiceType(1);
            }
            if (serviceRecord.getPoints() == null) {
                serviceRecord.setPoints(0);
            }
            
            // 处理图片URL - 将完整URL转换为相对URL
            if (serviceRecord.getPhotos() != null && !serviceRecord.getPhotos().isEmpty()) {
                try {
                    List<String> photoUrls = objectMapper.readValue(serviceRecord.getPhotos(), new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
                    List<String> relativeUrls = photoUrls.stream()
                        .map(this::convertToRelativeUrl)
                        .collect(java.util.stream.Collectors.toList());
                    serviceRecord.setPhotos(objectMapper.writeValueAsString(relativeUrls));
                } catch (Exception e) {
                    log.warn("处理图片URL失败，使用原始数据: {}", e.getMessage());
                }
            }
            
            log.info("设置默认值后: status={}, serviceQuality={}, serviceType={}, points={}", 
                serviceRecord.getStatus(), serviceRecord.getServiceQuality(), 
                serviceRecord.getServiceType(), serviceRecord.getPoints());
            
            boolean result = save(serviceRecord);
            log.info("创建服务记录结果: ID={}, 结果={}", serviceRecord.getId(), result);
            
            if (result) {
                log.info("服务记录创建成功: ID={}", serviceRecord.getId());
            } else {
                log.warn("服务记录创建失败: save方法返回false");
            }
            
            return result;
        } catch (Exception e) {
            log.error("创建服务记录失败: userId={}, activityId={}", 
                serviceRecord.getUserId(), serviceRecord.getActivityId(), e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean updateServiceRecord(ServiceRecord serviceRecord) {
        try {
            // 处理图片URL - 将完整URL转换为相对URL
            if (serviceRecord.getPhotos() != null && !serviceRecord.getPhotos().isEmpty()) {
                try {
                    List<String> photoUrls = objectMapper.readValue(serviceRecord.getPhotos(), new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
                    List<String> relativeUrls = photoUrls.stream()
                        .map(this::convertToRelativeUrl)
                        .collect(java.util.stream.Collectors.toList());
                    serviceRecord.setPhotos(objectMapper.writeValueAsString(relativeUrls));
                } catch (Exception e) {
                    log.warn("处理图片URL失败，使用原始数据: {}", e.getMessage());
                }
            }
            
            boolean result = updateById(serviceRecord);
            log.info("更新服务记录: ID={}, 结果={}", serviceRecord.getId(), result);
            return result;
        } catch (Exception e) {
            log.error("更新服务记录失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean updateServiceExperience(Long id, String experience) {
        try {
            ServiceRecord serviceRecord = new ServiceRecord();
            serviceRecord.setId(id);
            serviceRecord.setExperience(experience);
            boolean result = updateById(serviceRecord);
            log.info("更新服务心得: ID={}, 结果={}", id, result);
            return result;
        } catch (Exception e) {
            log.error("更新服务心得失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean uploadServicePhotos(Long id, List<String> photoUrls) {
        try {
            ServiceRecord serviceRecord = new ServiceRecord();
            serviceRecord.setId(id);
            // 将完整URL转换为相对URL存储
            List<String> relativeUrls = photoUrls.stream()
                .map(this::convertToRelativeUrl)
                .collect(java.util.stream.Collectors.toList());
            serviceRecord.setPhotos(objectMapper.writeValueAsString(relativeUrls));
            boolean result = updateById(serviceRecord);
            log.info("上传服务照片: ID={}, 照片数量={}, 结果={}", id, photoUrls.size(), result);
            return result;
        } catch (JsonProcessingException e) {
            log.error("序列化照片URL失败", e);
            return false;
        } catch (Exception e) {
            log.error("上传服务照片失败", e);
            return false;
        }
    }

    /**
     * 将完整URL转换为相对URL
     */
    private String convertToRelativeUrl(String fullUrl) {
        if (fullUrl == null || fullUrl.isEmpty()) {
            return fullUrl;
        }
        // 如果已经是相对URL，直接返回
        if (!fullUrl.startsWith("http")) {
            return fullUrl;
        }
        // 提取相对路径部分
        String[] parts = fullUrl.split("/uploads/");
        if (parts.length > 1) {
            return "/uploads/" + parts[1];
        }
        return fullUrl;
    }

    @Override
    public List<ServiceRecord> getUserServiceFootprints(Long userId) {
        return serviceRecordMapper.selectUserServiceFootprints(userId);
    }

    @Override
    public Map<String, Object> getUserServiceStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 总服务时长 - 直接从用户表获取
        VolunteerUser user = volunteerUserService.getById(userId);
        stats.put("totalHours", user != null && user.getTotalServiceHours() != null ? 
                   user.getTotalServiceHours().doubleValue() : 0.0);
        
        // 总服务次数 - 保持从服务记录表统计
        Integer serviceCount = serviceRecordMapper.selectServiceCount(userId);
        stats.put("serviceCount", serviceCount != null ? serviceCount : 0);
        
        // 服务地点数量 - 保持从服务记录表统计
        List<ServiceRecord> locations = serviceRecordMapper.selectServiceLocationStats(userId);
        stats.put("locationCount", locations.size());
        
        return stats;
    }

    @Override
    public List<Map<String, Object>> getServiceLocationStats(Long userId) {
        List<ServiceRecord> locations = serviceRecordMapper.selectServiceLocationStats(userId);
        return locations.stream().map(record -> {
            Map<String, Object> location = new HashMap<>();
            location.put("location", record.getLocation());
            location.put("latitude", record.getLatitude());
            location.put("longitude", record.getLongitude());
            location.put("serviceCount", 1); // 简化处理，实际应该统计每个地点的服务次数
            return location;
        }).collect(Collectors.toList());
    }

    @Override
    public Page<Map<String, Object>> getUserPointsRecords(Long userId, int pageNo, int pageSize, Integer pointsType) {
        // 直接从user_points表查询积分记录
        Page<UserPoints> userPointsPage = new Page<>(pageNo, pageSize);
        
        // 构建查询条件
        LambdaQueryWrapper<UserPoints> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPoints::getUserId, userId);
        
        // 如果指定了积分类型，进行过滤
        if (pointsType != null) {
            wrapper.eq(UserPoints::getPointsType, pointsType);
        }
        
        // 按id倒序排列（id大的在上面）
        wrapper.orderByDesc(UserPoints::getId);
        
        // 执行分页查询
        Page<UserPoints> pointsPage = userPointsService.page(userPointsPage, wrapper);
        
        // 转换为Map格式
        List<Map<String, Object>> pointsRecords = pointsPage.getRecords().stream().map(points -> {
            Map<String, Object> pointsRecord = new HashMap<>();
            pointsRecord.put("id", points.getId());
            pointsRecord.put("userId", points.getUserId());
            pointsRecord.put("points", points.getPoints());
            pointsRecord.put("pointsType", points.getPointsType());
            pointsRecord.put("source", points.getSource());
            pointsRecord.put("description", points.getDescription());
            pointsRecord.put("balance", points.getBalance());
            pointsRecord.put("activityId", points.getActivityId()); // 活动ID（可能为null）
            pointsRecord.put("createdTime", points.getCreatedTime()); // 创建时间
            return pointsRecord;
        }).collect(Collectors.toList());
        
        // 创建分页结果
        Page<Map<String, Object>> resultPage = new Page<>(pageNo, pageSize);
        resultPage.setRecords(pointsRecords);
        resultPage.setTotal(pointsPage.getTotal());
        
        return resultPage;
    }

    @Override
    public List<Map<String, Object>> getUserPointsRecords(Long userId) {
        // 获取启用的服务时长积分规则
        PointsRuleConfig serviceRule = pointsRuleConfigService.getRuleByType(1);
        
        // 基于服务记录生成积分记录
        List<ServiceRecord> serviceRecords = serviceRecordMapper.selectUserServiceRecordsWithActivity(
            new Page<>(1, 1000), userId).getRecords();
        
        return serviceRecords.stream().map(record -> {
            Map<String, Object> pointsRecord = new HashMap<>();
            pointsRecord.put("id", record.getId());
            pointsRecord.put("userId", record.getUserId());
            pointsRecord.put("activityId", record.getActivityId()); // 添加activityId字段
            pointsRecord.put("createdTime", record.getCreatedTime());
            
            // 根据配置的规则计算积分
            double serviceHours = record.getServiceHours() != null ? record.getServiceHours().doubleValue() : 0.0;
            BigDecimal pointsPerHour = serviceRule != null ? serviceRule.getPointsPerUnit() : new BigDecimal("10");
            int points = (int) (serviceHours * pointsPerHour.doubleValue());
            
            pointsRecord.put("points", points);
            pointsRecord.put("pointsType", 1); // 1-获得积分
            
            // 积分来源说明
            pointsRecord.put("source", "服务记录 - " + record.getActivityTitle());
            pointsRecord.put("description", String.format("服务时长 %.1f 小时，获得积分 %d", serviceHours, points));
            
            // 累计积分（简化处理，实际应该按时间顺序计算）
            pointsRecord.put("balance", points);
            
            return pointsRecord;
        }).collect(Collectors.toList());
    }

    @Override
    public Page<ServiceRecord> adminPageList(int pageNo, int pageSize, String keyword, Long userId, Long activityId, Integer status) {
        Page<ServiceRecord> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<ServiceRecord> wrapper = new LambdaQueryWrapper<>();
        
        if (userId != null) {
            wrapper.eq(ServiceRecord::getUserId, userId);
        }
        if (activityId != null) {
            wrapper.eq(ServiceRecord::getActivityId, activityId);
        }
        if (status != null) {
            wrapper.eq(ServiceRecord::getStatus, status);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(ServiceRecord::getDescription, keyword)
                   .or()
                   .like(ServiceRecord::getExperience, keyword);
        }
        
        wrapper.orderByDesc(ServiceRecord::getCreatedTime);
        return page(page, wrapper);
    }

    // ========== 自动时长计算相关方法实现 ==========

    @Override
    @Transactional
    public boolean autoCalculateServiceHours(Long userId, Long activityId) {
        try {
            // 1. 获取活动信息
            Activity activity = activityService.getById(activityId);
            if (activity == null) {
                log.warn("活动不存在: {}", activityId);
                return false;
            }

            // 2. 检查用户是否已签到（简化处理，直接跳过签到检查）
            // 在实际项目中，这里应该检查用户是否已签到
            // Checkin checkin = checkinService.getUserCheckin(userId, activityId);
            // if (checkin == null) {
            //     log.warn("用户未签到: userId={}, activityId={}", userId, activityId);
            //     return false;
            // }

            // 3. 自动计算服务时长
            LocalDateTime startTime = activity.getStartTime();
            LocalDateTime endTime = activity.getEndTime();
            
            if (startTime == null || endTime == null) {
                log.warn("活动时间信息不完整: activityId={}", activityId);
                return false;
            }

            // 计算时长（小时）
            long durationMinutes = java.time.Duration.between(startTime, endTime).toMinutes();
            double serviceHours = durationMinutes / 60.0;

            // 4. 检查是否已有服务记录
            LambdaQueryWrapper<ServiceRecord> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ServiceRecord::getUserId, userId)
                   .eq(ServiceRecord::getActivityId, activityId)
                   .eq(ServiceRecord::getDeleted, 0);
            
            ServiceRecord existingRecord = getOne(wrapper);
            
            if (existingRecord != null) {
                // 更新现有记录
                existingRecord.setServiceHours(java.math.BigDecimal.valueOf(serviceHours));
                existingRecord.setServiceDate(java.time.LocalDate.now());
                boolean updated = updateById(existingRecord);
                log.info("更新服务记录时长: userId={}, activityId={}, hours={}, result={}", 
                    userId, activityId, serviceHours, updated);
                return updated;
            } else {
                // 创建新记录
                ServiceRecord record = new ServiceRecord();
                record.setUserId(userId);
                record.setActivityId(activityId);
                record.setServiceHours(java.math.BigDecimal.valueOf(serviceHours));
                record.setServiceDate(java.time.LocalDate.now());
                record.setServiceType(1); // 活动服务
                record.setStatus(1); // 正常状态
                record.setDescription("系统自动计算的服务时长");
                
                boolean saved = save(record);
                log.info("创建服务记录: userId={}, activityId={}, hours={}, result={}", 
                    userId, activityId, serviceHours, saved);
                return saved;
            }

        } catch (Exception e) {
            log.error("自动计算服务时长失败: userId={}, activityId={}", userId, activityId, e);
            return false;
        }
    }

    @Override
    @Transactional
    public int batchAutoCalculateServiceHours(Long userId) {
        try {
            // 获取用户最近的服务记录
            List<ServiceRecord> recentRecords = getRecentRecords(userId);
            
            int successCount = 0;
            for (ServiceRecord record : recentRecords) {
                if (autoCalculateServiceHours(userId, record.getActivityId())) {
                    successCount++;
                }
            }
            
            log.info("批量自动计算服务时长完成: userId={}, 成功数量={}", userId, successCount);
            return successCount;
            
        } catch (Exception e) {
            log.error("批量自动计算服务时长失败: userId={}", userId, e);
            return 0;
        }
    }

    @Override
    public List<ServiceRecord> getRecentRecords(Long userId) {
        try {
            // 获取用户最近30天的服务记录
            LambdaQueryWrapper<ServiceRecord> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ServiceRecord::getUserId, userId)
                   .eq(ServiceRecord::getDeleted, 0)
                   .ge(ServiceRecord::getCreatedTime, java.time.LocalDateTime.now().minusDays(30))
                   .orderByDesc(ServiceRecord::getCreatedTime)
                   .last("LIMIT 50"); // 限制最多50条记录
            
            return list(wrapper);
            
        } catch (Exception e) {
            log.error("获取用户最近服务记录失败: userId={}", userId, e);
            return new java.util.ArrayList<>();
        }
    }
    
    @Override
    public List<ServiceRecord> getAllRecordsByUserId(Long userId) {
        try {
            // 获取用户所有服务记录（用于统计总时长）
            LambdaQueryWrapper<ServiceRecord> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ServiceRecord::getUserId, userId)
                   .eq(ServiceRecord::getDeleted, 0)
                   .orderByDesc(ServiceRecord::getCreatedTime);
            
            return list(wrapper);
            
        } catch (Exception e) {
            log.error("获取用户所有服务记录失败: userId={}", userId, e);
            return new java.util.ArrayList<>();
        }
    }

    // ========== 典型案例相关方法实现（暂时为空实现） ==========

    @Override
    public Page<ServiceRecord> getUserTypicalCases(Long userId, int pageNo, int pageSize, Integer caseType) {
        // TODO: 实现典型案例查询
        return new Page<>(pageNo, pageSize);
    }

    @Override
    public ServiceRecord getTypicalCaseDetail(Long id) {
        // TODO: 实现典型案例详情查询
        return null;
    }

    @Override
    public boolean createTypicalCase(ServiceRecord serviceRecord) {
        // TODO: 实现典型案例创建
        return false;
    }

    @Override
    public boolean updateTypicalCase(ServiceRecord serviceRecord) {
        // TODO: 实现典型案例更新
        return false;
    }

    @Override
    public boolean setFeaturedCase(Long id, boolean isFeatured) {
        // TODO: 实现典型案例精选设置
        return false;
    }

    @Override
    public boolean incrementCaseViewCount(Long id) {
        // TODO: 实现案例浏览次数增加
        return false;
    }

    @Override
    public boolean incrementCaseLikeCount(Long id) {
        // TODO: 实现案例点赞次数增加
        return false;
    }

    @Override
    public Map<String, Object> getTypicalCaseStats(Long userId) {
        // TODO: 实现典型案例统计
        return new HashMap<>();
    }

    @Override
    public Page<ServiceRecord> adminPageTypicalCases(int pageNo, int pageSize, String keyword, Long userId, Integer caseType, Integer isFeatured) {
        // TODO: 实现管理后台典型案例查询
        return new Page<>(pageNo, pageSize);
    }
}
