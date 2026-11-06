package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.volunteer.entity.UserPoints;
import com.volunteer.entity.VolunteerUser;
import com.volunteer.mapper.UserPointsMapper;
import com.volunteer.service.UserPointsService;
import com.volunteer.service.VolunteerUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 用户积分记录Service实现类
 * 
 * @author volunteer-team
 * @since 2025-01-22
 */
@Slf4j
@Service
public class UserPointsServiceImpl extends ServiceImpl<UserPointsMapper, UserPoints> implements UserPointsService {

    @Autowired
    private VolunteerUserService volunteerUserService;

    @Override
    public Integer getUserTotalPoints(Long userId) {
        try {
            Integer totalPoints = baseMapper.getUserTotalPoints(userId);
            return totalPoints != null ? totalPoints : 0;
        } catch (Exception e) {
            log.error("获取用户总积分失败: userId={}", userId, e);
            return 0;
        }
    }

    @Override
    public List<UserPoints> getUserPointsRecords(Long userId) {
        try {
            return baseMapper.getUserPointsRecords(userId);
        } catch (Exception e) {
            log.error("获取用户积分记录失败: userId={}", userId, e);
            return Collections.emptyList();
        }
    }

    @Override
    @Transactional
    public boolean addPointsRecord(Long userId, Integer points, Integer pointsType, String source, String description) {
        try {
            // 获取用户当前总积分
            Integer currentTotalPoints = getUserTotalPoints(userId);
            
            // 计算新的积分余额
            Integer newBalance;
            if (pointsType == 1) { // 获得积分
                newBalance = currentTotalPoints + points;
            } else { // 消费积分
                newBalance = currentTotalPoints - points;
            }
            
            // 创建积分记录
            UserPoints pointsRecord = new UserPoints();
            pointsRecord.setUserId(userId);
            pointsRecord.setPoints(points);
            pointsRecord.setPointsType(pointsType);
            pointsRecord.setSource(source);
            pointsRecord.setDescription(description);
            pointsRecord.setBalance(newBalance);
            pointsRecord.setCreatedTime(LocalDateTime.now());
            
            boolean result = save(pointsRecord);
            
            // 同步更新志愿者表中的总积分
            if (result) {
                VolunteerUser volunteer = volunteerUserService.getById(userId);
                if (volunteer != null) {
                    volunteer.setTotalPoints(newBalance);
                    volunteerUserService.updateById(volunteer);
                    log.info("同步更新志愿者总积分成功: userId={}, totalPoints={}", userId, newBalance);
                }
                log.info("添加积分记录成功: userId={}, points={}, pointsType={}, source={}, balance={}", 
                    userId, points, pointsType, source, newBalance);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("添加积分记录失败: userId={}, points={}, pointsType={}, source={}", 
                userId, points, pointsType, source, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean batchAddPointsRecords(List<UserPoints> pointsRecords) {
        try {
            if (pointsRecords == null || pointsRecords.isEmpty()) {
                return true;
            }
            
            boolean result = saveBatch(pointsRecords);
            
            // 批量更新志愿者表中的总积分
            if (result) {
                for (UserPoints pointsRecord : pointsRecords) {
                    VolunteerUser volunteer = volunteerUserService.getById(pointsRecord.getUserId());
                    if (volunteer != null) {
                        // 重新计算该用户的总积分
                        Integer totalPoints = getUserTotalPoints(pointsRecord.getUserId());
                        volunteer.setTotalPoints(totalPoints);
                        volunteerUserService.updateById(volunteer);
                    }
                }
                log.info("批量添加积分记录成功: 记录数量={}", pointsRecords.size());
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("批量添加积分记录失败: 记录数量={}", 
                pointsRecords != null ? pointsRecords.size() : 0, e);
            return false;
        }
    }

    @Override
    public IPage<UserPoints> getPointsRecordsPage(Integer current, Integer size, String username, String realName, Integer pointsType, String source) {
        try {
            Page<UserPoints> page = new Page<>(current, size);
            return baseMapper.getPointsRecordsPage(page, username, realName, pointsType, source);
        } catch (Exception e) {
            log.error("分页查询积分记录失败: current={}, size={}, username={}, realName={}, pointsType={}, source={}", 
                current, size, username, realName, pointsType, source, e);
            return new Page<>(current, size);
        }
    }
}
