package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.volunteer.entity.Activity;
import com.volunteer.entity.Checkin;
import com.volunteer.entity.ServiceRecord;
import com.volunteer.entity.VolunteerUser;
import com.volunteer.mapper.CheckinMapper;
import com.volunteer.mapper.ServiceRecordMapper;
import com.volunteer.service.ActivityService;
import com.volunteer.service.CheckinService;
import com.volunteer.service.RegistrationService;
import com.volunteer.service.ServiceRecordService;
import com.volunteer.service.VolunteerUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import com.volunteer.vo.CheckinDetailVO;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CheckinServiceImpl extends ServiceImpl<CheckinMapper, Checkin> implements CheckinService {

    @Resource
    private CheckinMapper checkinMapper;
    
    @Resource
    private ServiceRecordMapper serviceRecordMapper;
    
    @Resource
    private ActivityService activityService;
    
    // 移除循环依赖，不再直接注入ServiceRecordService
    // @Resource
    // @Lazy
    // private ServiceRecordService serviceRecordService;
    
    @Resource
    private RegistrationService registrationService;

    @Resource
    private VolunteerUserService volunteerUserService;

    @Override
    public boolean checkin(Long activityId, Long userId, String location, String remark) {
        // 检查今天是否已经签到过此活动
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        
        LambdaQueryWrapper<Checkin> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(Checkin::getActivityId, activityId)
                   .eq(Checkin::getUserId, userId)
                   .eq(Checkin::getDeleted, 0)
                   .apply("DATE(checkin_time) = {0}", today);
        Checkin existingCheckin = checkinMapper.selectOne(checkWrapper);
        
        if (existingCheckin != null) {
            throw new RuntimeException("您今天已经签到过此活动，请明天再进行签到");
        }
        
        Checkin c = new Checkin();
        c.setActivityId(activityId);
        c.setUserId(userId);
        c.setCheckinTime(LocalDateTime.now());
        c.setLocation(location);
        c.setRemark(remark);
        c.setStatus(1); // 默认正常状态
        
        return checkinMapper.insert(c) > 0;
    }

    @Override
    @Transactional
    public boolean checkout(Long activityId, Long userId, String location, String remark) {
        try {
            // 1. 检查今天是否已经签退过此活动
            LocalDateTime now = LocalDateTime.now();
            LocalDate today = now.toLocalDate();
            
            LambdaQueryWrapper<Checkin> checkoutCheckWrapper = new LambdaQueryWrapper<>();
            checkoutCheckWrapper.eq(Checkin::getActivityId, activityId)
                               .eq(Checkin::getUserId, userId)
                               .eq(Checkin::getDeleted, 0)
                               .isNotNull(Checkin::getCheckoutTime)
                               .apply("DATE(checkout_time) = {0}", today);
            Checkin existingCheckout = checkinMapper.selectOne(checkoutCheckWrapper);
            
            if (existingCheckout != null) {
                throw new RuntimeException("您今天已经签退过此活动，请明天再进行签退");
            }
            
            // 2. 查找用户今天的签到记录（未签退的）
            LambdaQueryWrapper<Checkin> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Checkin::getActivityId, activityId)
                   .eq(Checkin::getUserId, userId)
                   .eq(Checkin::getDeleted, 0)
                   .isNull(Checkin::getCheckoutTime)
                   .apply("DATE(checkin_time) = {0}", today);
            
            Checkin checkin = getOne(wrapper);
            if (checkin == null) {
                throw new RuntimeException("未找到今天的签到记录，请先签到");
            }
            
            // 3. 设置签退信息
            checkin.setCheckoutTime(LocalDateTime.now());
            checkin.setCheckoutLocation(location);
            if (remark != null && !remark.isEmpty()) {
                String existingRemark = checkin.getRemark() != null ? checkin.getRemark() : "";
                checkin.setRemark(existingRemark + "\n签退备注：" + remark);
            }
            
            // 4. 计算实际服务时长（保留1位小数，四舍五入）
            LocalDateTime checkinTime = checkin.getCheckinTime();
            LocalDateTime checkoutTime = checkin.getCheckoutTime();
            
            long durationMinutes = Duration.between(checkinTime, checkoutTime).toMinutes();
            double actualHours = durationMinutes / 60.0;
            // 保留1位小数，四舍五入
            BigDecimal roundedHours = BigDecimal.valueOf(actualHours).setScale(1, BigDecimal.ROUND_HALF_UP);
            checkin.setActualServiceHours(roundedHours);
            
            // 5. 更新签到记录
            boolean result = updateById(checkin);
            
            if (result) {
                // 6. 更新用户总服务时长
                volunteerUserService.addServiceHours(userId, roundedHours);
                
                // 7. 自动更新服务记录 - service_record表服务时长字段已废弃，不再更新
                // autoUpdateServiceRecordFromCheckin(checkin);
                log.info("用户签退成功: userId={}, activityId={}, actualHours={}", userId, activityId, actualHours);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("签退失败: activityId={}, userId={}", activityId, userId, e);
            throw e;
        }
    }

    @Override
    public Page<Checkin> myCheckins(Long userId, int pageNo, int pageSize) {
        Page<Checkin> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<Checkin> qw = new LambdaQueryWrapper<>();
        qw.eq(Checkin::getUserId, userId)
          .orderByDesc(Checkin::getCheckinTime);
        return checkinMapper.selectPage(page, qw);
    }

    @Override
    public Page<CheckinDetailVO> getUserCheckinsWithActivity(Long userId, int pageNo, int pageSize, String activityTitle) {
        Page<CheckinDetailVO> page = new Page<>(pageNo, pageSize);
        return checkinMapper.selectUserCheckinsWithActivity(page, userId, activityTitle);
    }

    @Override
    public boolean validateLocation(Long activityId, String location) {
        if (!StringUtils.hasText(location)) {
            return false;
        }
        
        try {
            // 获取活动信息
            Activity activity = activityService.getById(activityId);
            if (activity == null) {
                return false;
            }
            
            // 解析GPS坐标
            String[] coords = location.split(",");
            if (coords.length != 2) {
                return false;
            }
            
            double userLat = Double.parseDouble(coords[0].trim());
            double userLng = Double.parseDouble(coords[1].trim());
            
            // 这里需要根据活动地点解析出经纬度
            // 由于活动地点可能是地址字符串，这里简化处理
            // 实际项目中可以使用地理编码服务将地址转换为经纬度
            
            // 计算距离（使用Haversine公式）
            // 这里假设活动地点在某个固定位置，实际应该从数据库获取
            double activityLat = 39.9042; // 示例：北京天安门
            double activityLng = 116.4074;
            
            double distance = calculateDistance(userLat, userLng, activityLat, activityLng);
            
            // 允许误差范围（500米）
            return distance <= 0.5;
            
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean validateQRCode(Long activityId, String qrCode) {
        if (!StringUtils.hasText(qrCode)) {
            return false;
        }
        
        try {
            // 验证二维码格式：activity_{activityId}_{timestamp}
            if (!qrCode.startsWith("activity_")) {
                return false;
            }
            
            String[] parts = qrCode.split("_");
            if (parts.length != 3) {
                return false;
            }
            
            Long qrActivityId = Long.parseLong(parts[1]);
            long timestamp = Long.parseLong(parts[2]);
            
            // 验证活动ID是否匹配
            if (!qrActivityId.equals(activityId)) {
                return false;
            }
            
            // 验证二维码是否在有效期内（24小时）
            long currentTime = System.currentTimeMillis();
            long timeDiff = currentTime - timestamp;
            long validTime = 24 * 60 * 60 * 1000; // 24小时
            
            return timeDiff <= validTime;
            
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String generateQRCode(Long activityId) {
        // 生成包含活动信息的二维码
        long timestamp = System.currentTimeMillis();
        return String.format("activity_%d_%d", activityId, timestamp);
    }

    @Override
    public Page<Checkin> adminPageList(int pageNo, int pageSize, String keyword, Long activityId, Integer status) {
        Page<Checkin> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<Checkin> qw = new LambdaQueryWrapper<>();
        
        if (activityId != null) {
            qw.eq(Checkin::getActivityId, activityId);
        }
        
        if (status != null) {
            qw.eq(Checkin::getStatus, status);
        }
        
        qw.orderByDesc(Checkin::getCheckinTime);
        
        return checkinMapper.selectPage(page, qw);
    }

    @Override
    public Page<CheckinDetailVO> adminPageListWithUserInfo(int pageNo, int pageSize, String keyword, Long activityId, Integer status) {
        Page<CheckinDetailVO> page = new Page<>(pageNo, pageSize);
        return checkinMapper.selectCheckinDetailPage(page, keyword, activityId, status);
    }

    @Override
    public boolean updateServiceHours(Long checkinId, Double serviceHours) {
        Checkin checkin = checkinMapper.selectById(checkinId);
        if (checkin == null) {
            return false;
        }
        
        checkin.setActualServiceHours(BigDecimal.valueOf(serviceHours));
        return checkinMapper.updateById(checkin) > 0;
    }

    @Override
    public boolean updateCheckinStatus(Long checkinId, Integer status, String remark) {
        Checkin checkin = checkinMapper.selectById(checkinId);
        if (checkin == null) {
            return false;
        }
        
        checkin.setStatus(status);
        if (remark != null && !remark.trim().isEmpty()) {
            checkin.setRemark(remark);
        }
        return checkinMapper.updateById(checkin) > 0;
    }

    @Override
    public boolean batchUpdateServiceHours(List<Long> checkinIds, Double serviceHours, String remark) {
        if (checkinIds == null || checkinIds.isEmpty()) {
            return false;
        }
        
        for (Long checkinId : checkinIds) {
            Checkin checkin = checkinMapper.selectById(checkinId);
            if (checkin != null) {
                checkin.setActualServiceHours(BigDecimal.valueOf(serviceHours));
                if (StringUtils.hasText(remark)) {
                    checkin.setRemark(remark);
                }
                checkinMapper.updateById(checkin);
            }
        }
        
        return true;
    }

    /**
     * 计算两点之间的距离（公里）
     * 使用Haversine公式
     */
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371; // 地球半径（公里）
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lngDistance = Math.toRadians(lng2 - lng1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }

    @Override
    public void exportCheckins(String keyword, Long activityId, Integer status, HttpServletResponse response) throws IOException {
        // 设置响应头
        response.setContentType("text/csv;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        String fileName = String.format("签到数据_%s.csv", 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        // 获取签到数据
        Page<Checkin> page = adminPageList(1, 10000, keyword, activityId, status);
        List<Checkin> checkins = page.getRecords();

        // 写入CSV内容
        PrintWriter writer = response.getWriter();
        
        // 写入BOM头，解决Excel中文乱码问题
        writer.write('\ufeff');
        
        // 写入表头
        writer.println("ID,活动ID,用户ID,签到时间,签到地点,服务时长,签到状态,备注,创建时间");
        
        // 写入数据
        for (Checkin checkin : checkins) {
            writer.printf("%d,%d,%d,%s,%s,%s,%s,%s,%s%n",
                checkin.getId(),
                checkin.getActivityId(),
                checkin.getUserId(),
                checkin.getCheckinTime() != null ? checkin.getCheckinTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "",
                checkin.getLocation() != null ? checkin.getLocation() : "",
                checkin.getActualServiceHours() != null ? checkin.getActualServiceHours().toString() : "",
                getStatusText(checkin.getStatus()),
                checkin.getRemark() != null ? checkin.getRemark() : "",
                checkin.getCreatedTime() != null ? checkin.getCreatedTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : ""
            );
        }
        
        writer.flush();
        writer.close();
    }

    @Override
    @Transactional
    public boolean autoCalculateServiceHoursFromCheckin(Long checkinId) {
        try {
            Checkin checkin = getById(checkinId);
            if (checkin == null) {
                log.warn("自动计算服务时长失败: 签到记录不存在, checkinId={}", checkinId);
                return false;
            }
            
            log.info("开始自动计算服务时长: checkinId={}, checkinTime={}, checkoutTime={}", 
                checkinId, checkin.getCheckinTime(), checkin.getCheckoutTime());
            
            BigDecimal calculatedHours = null;
            
            // 如果有签退时间，使用实际签退时间计算
            if (checkin.getCheckoutTime() != null) {
                long durationMinutes = Duration.between(checkin.getCheckinTime(), checkin.getCheckoutTime()).toMinutes();
                double actualHours = durationMinutes / 60.0;
                // 保留1位小数，四舍五入
                calculatedHours = BigDecimal.valueOf(actualHours).setScale(1, BigDecimal.ROUND_HALF_UP);
                log.info("使用签退时间计算: durationMinutes={}, actualHours={}", durationMinutes, actualHours);
            } else {
                // 如果没有签退时间，使用活动结束时间
                Activity activity = activityService.getById(checkin.getActivityId());
                if (activity != null && activity.getEndTime() != null) {
                    // 构造虚拟签退时间：签到当天 + 活动结束时间的时分秒
                    LocalDate checkinDate = checkin.getCheckinTime().toLocalDate();
                    LocalTime activityEndTime = activity.getEndTime().toLocalTime();
                    LocalDateTime virtualCheckoutTime = LocalDateTime.of(checkinDate, activityEndTime);
                    
                    // 检查签到时间是否晚于虚拟签退时间
                    if (checkin.getCheckinTime().isAfter(virtualCheckoutTime)) {
                        log.warn("签到时间晚于活动结束时间: checkinTime={}, virtualCheckoutTime={}", 
                            checkin.getCheckinTime(), virtualCheckoutTime);
                        calculatedHours = BigDecimal.ZERO;
                    } else {
                        long durationMinutes = Duration.between(checkin.getCheckinTime(), virtualCheckoutTime).toMinutes();
                        double actualHours = durationMinutes / 60.0;
                        calculatedHours = BigDecimal.valueOf(actualHours).setScale(1, BigDecimal.ROUND_HALF_UP);
                        log.info("使用虚拟签退时间计算: checkinTime={}, virtualCheckoutTime={}, durationMinutes={}, actualHours={}", 
                            checkin.getCheckinTime(), virtualCheckoutTime, durationMinutes, actualHours);
                    }
                } else {
                    log.warn("活动不存在或活动结束时间为空: activityId={}", checkin.getActivityId());
                    calculatedHours = BigDecimal.ZERO;
                }
            }
            
            if (calculatedHours != null) {
                // 更新activity_checkin表的服务时长（志愿者表由数据库触发器自动更新）
                checkin.setActualServiceHours(calculatedHours);
                boolean result = updateById(checkin);
                
                if (result) {
                    // 自动更新服务记录 - service_record表服务时长字段已废弃，不再更新
                    // autoUpdateServiceRecordFromCheckin(checkin);
                    log.info("自动计算服务时长成功: checkinId={}, actualHours={}", checkinId, calculatedHours);
                } else {
                    log.error("更新签到记录失败: checkinId={}", checkinId);
                }
                return result;
            } else {
                log.error("无法计算服务时长: checkinId={}", checkinId);
                return false;
            }
            
        } catch (Exception e) {
            log.error("自动计算服务时长失败: checkinId={}", checkinId, e);
            return false;
        }
    }

    /**
     * 自动更新服务记录
     */
    private void autoUpdateServiceRecordFromCheckin(Checkin checkin) {
        try {
            // 查找或创建服务记录
            LambdaQueryWrapper<ServiceRecord> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ServiceRecord::getUserId, checkin.getUserId())
                   .eq(ServiceRecord::getActivityId, checkin.getActivityId())
                   .eq(ServiceRecord::getDeleted, 0);
            
            ServiceRecord serviceRecord = serviceRecordMapper.selectOne(wrapper);
            
            if (serviceRecord == null) {
                // 创建新的服务记录
                serviceRecord = new ServiceRecord();
                serviceRecord.setUserId(checkin.getUserId());
                serviceRecord.setActivityId(checkin.getActivityId());
                serviceRecord.setServiceDate(checkin.getCheckinTime().toLocalDate());
                serviceRecord.setServiceType(1); // 活动服务
                serviceRecord.setStatus(1); // 正常状态
                serviceRecord.setDescription("系统自动生成的服务记录");
            }
            
            // 更新服务时长（统一使用actual_service_hours）
            if (checkin.getActualServiceHours() != null) {
                serviceRecord.setServiceHours(checkin.getActualServiceHours());
            }
            
            // 保存或更新服务记录
            if (serviceRecord.getId() == null) {
                serviceRecordMapper.insert(serviceRecord);
            } else {
                serviceRecordMapper.updateById(serviceRecord);
            }
            
            log.info("自动更新服务记录成功: userId={}, activityId={}, serviceHours={}", 
                checkin.getUserId(), checkin.getActivityId(), serviceRecord.getServiceHours());
            
        } catch (Exception e) {
            log.error("自动更新服务记录失败: checkinId={}", checkin.getId(), e);
        }
    }

    /**
     * 获取签到状态文本
     */
    private String getStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 1:
                return "正常";
            case 2:
                return "迟到";
            case 3:
                return "早退";
            default:
                return "未知";
        }
    }

    @Override
    public boolean hasCheckedInToday(Long activityId, Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        
        LambdaQueryWrapper<Checkin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Checkin::getActivityId, activityId)
               .eq(Checkin::getUserId, userId)
               .eq(Checkin::getDeleted, 0)
               .apply("DATE(checkin_time) = {0}", today);
        
        Checkin checkin = getOne(wrapper);
        return checkin != null;
    }

    @Override
    public Checkin getTodayCheckin(Long activityId, Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        
        LambdaQueryWrapper<Checkin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Checkin::getActivityId, activityId)
               .eq(Checkin::getUserId, userId)
               .eq(Checkin::getDeleted, 0)
               .apply("DATE(checkin_time) = {0}", today)
               .orderByDesc(Checkin::getCheckinTime)
               .last("LIMIT 1");
        
        return getOne(wrapper);
    }

    @Override
    public boolean validateUserRegistration(Long activityId, Long userId) {
        return registrationService.isUserRegisteredAndApproved(activityId, userId);
    }

    @Override
    public boolean validateCheckoutSameDay(Long activityId, Long userId) {
        // 检查今天是否有未签退的签到记录
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        
        LambdaQueryWrapper<Checkin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Checkin::getActivityId, activityId)
               .eq(Checkin::getUserId, userId)
               .eq(Checkin::getDeleted, 0)
               .isNull(Checkin::getCheckoutTime)
               .apply("DATE(checkin_time) = {0}", today);
        
        Checkin checkin = getOne(wrapper);
        return checkin != null;
    }

    @Override
    @Transactional
    public Map<String, Object> updateUsersTotalServiceHours(List<Long> userIds) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;
        List<String> failMessages = new ArrayList<>();
        
        try {
            // 如果传入空数组或null，则获取所有用户ID
            if (userIds == null || userIds.isEmpty()) {
                List<VolunteerUser> allUsers = volunteerUserService.list();
                userIds = allUsers.stream()
                    .map(VolunteerUser::getId)
                    .collect(Collectors.toList());
                log.info("批量更新所有用户总服务时长，用户总数: {}", userIds.size());
            }
            
            for (Long userId : userIds) {
                try {
                    // 从签到表统计用户的总服务时长
                    BigDecimal totalCalculatedHours = checkinMapper.selectTotalServiceHoursByUserId(userId);
                    if (totalCalculatedHours == null) {
                        totalCalculatedHours = BigDecimal.ZERO;
                    }
                    
                    // 更新志愿者表的总服务时长
                    LambdaQueryWrapper<Checkin> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(Checkin::getUserId, userId)
                           .eq(Checkin::getDeleted, 0)
                           .isNotNull(Checkin::getActualServiceHours)
                           .gt(Checkin::getActualServiceHours, 0);
                    
                    // 使用原生SQL更新志愿者表
                    String updateSql = "UPDATE volunteer_user SET total_service_hours = ? WHERE id = ?";
                    // 这里需要使用JdbcTemplate或者MyBatis的@Update注解
                    // 为了简化，我们使用现有的VolunteerUserService
                    
                    // 获取用户信息并更新
                    VolunteerUser user = volunteerUserService.getById(userId);
                    if (user != null) {
                        user.setTotalServiceHours(totalCalculatedHours);
                        boolean updateResult = volunteerUserService.updateById(user);
                        
                        if (updateResult) {
                            successCount++;
                            log.info("更新用户总服务时长成功: userId={}, totalServiceHours={}", userId, totalCalculatedHours);
                        } else {
                            failCount++;
                            failMessages.add("用户ID " + userId + " 更新失败");
                        }
                    } else {
                        failCount++;
                        failMessages.add("用户ID " + userId + " 不存在");
                    }
                    
                } catch (Exception e) {
                    failCount++;
                    failMessages.add("用户ID " + userId + " 更新异常: " + e.getMessage());
                    log.error("更新用户总服务时长失败: userId={}", userId, e);
                }
            }
            
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("failMessages", failMessages);
            result.put("totalCount", userIds.size());
            result.put("message", String.format("更新完成：成功 %d 个，失败 %d 个", successCount, failCount));
            
            log.info("批量更新用户总服务时长完成: 总数={}, 成功={}, 失败={}", userIds.size(), successCount, failCount);
            
        } catch (Exception e) {
            log.error("批量更新用户总服务时长失败", e);
            result.put("successCount", 0);
            result.put("failCount", userIds.size());
            result.put("failMessages", new ArrayList<>(Arrays.asList("批量更新失败: " + e.getMessage())));
            result.put("totalCount", userIds.size());
            result.put("message", "批量更新失败: " + e.getMessage());
        }
        
        return result;
    }
}
