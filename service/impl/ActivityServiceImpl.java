package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.Activity;
import com.volunteer.entity.Registration;
import com.volunteer.mapper.ActivityMapper;
import com.volunteer.mapper.RegistrationMapper;
import com.volunteer.service.ActivityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class ActivityServiceImpl implements ActivityService {

    @Resource
    private ActivityMapper activityMapper;

    @Resource
    private RegistrationMapper registrationMapper;

    @Override
    public Page<Activity> pageList(int pageNo, int pageSize, String keyword, Long categoryId, Integer status,
                                   String location, String startTimeBegin, String startTimeEnd) {
        Page<Activity> page = new Page<>(pageNo, pageSize);
        
        // C端默认显示已发布、进行中、已结束状态的活动
        // 如果指定了状态，则按指定状态查询；否则查询状态为2,3,4的活动
        log.info("ActivityService.pageList 参数: keyword={}, categoryId={}, status={}, location={}, startTimeBegin={}, startTimeEnd={}", 
            keyword, categoryId, status, location, startTimeBegin, startTimeEnd);
        
        return activityMapper.selectActivityPageWithParticipants(page, status, categoryId, keyword,
            location, startTimeBegin, startTimeEnd);
    }

    @Override
    public Page<Activity> adminPageList(int pageNo, int pageSize, String keyword, Long categoryId, Integer status, Long organizerId) {
        Page<Activity> page = new Page<>(pageNo, pageSize);
        
        // 管理后台显示所有状态的活动，使用新的查询方法获取实际报名人数
        return activityMapper.selectAdminActivityPageWithParticipants(page, status, categoryId, organizerId, keyword);
    }

    @Override
    public Activity getById(Long id) {
        return activityMapper.selectActivityDetailWithParticipantsById(id);
    }

    @Override
    @Transactional
    public boolean create(Activity activity) {
        // 设置默认值
        if (activity.getStatus() == null) {
            activity.setStatus(1); // 默认草稿状态
        }
        if (activity.getCurrentParticipants() == null) {
            activity.setCurrentParticipants(0);
        }
        activity.setCreatedTime(LocalDateTime.now());
        activity.setUpdatedTime(LocalDateTime.now());
        
        return activityMapper.insert(activity) > 0;
    }

    @Override
    @Transactional
    public boolean update(Activity activity) {
        activity.setUpdatedTime(LocalDateTime.now());
        return activityMapper.updateById(activity) > 0;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        return activityMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional
    public String deleteWithCheck(Long id) {
        // 检查是否存在报名记录
        Long registrationCount = registrationMapper.selectCount(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getActivityId, id));
        
        if (registrationCount > 0) {
            return "该活动存在报名记录，不支持删除！";
        }
        
        // 没有报名记录，可以删除
        boolean success = activityMapper.deleteById(id) > 0;
        return success ? "删除成功" : "删除失败";
    }

    @Override
    @Transactional
    public boolean batchDelete(List<Long> ids) {
        return activityMapper.deleteBatchIds(ids) > 0;
    }

    @Override
    @Transactional
    public boolean updateStatus(Long id, Integer status) {
        Activity activity = new Activity();
        activity.setId(id);
        activity.setStatus(status);
        activity.setUpdatedTime(LocalDateTime.now());
        return activityMapper.updateById(activity) > 0;
    }

    @Override
    @Transactional
    public boolean batchUpdateStatus(List<Long> ids, Integer status) {
        for (Long id : ids) {
            updateStatus(id, status);
        }
        return true;
    }

    @Override
    public String exportActivities(String keyword, Long categoryId, Integer status) {
        // 这里可以实现导出功能，返回文件下载链接
        // 暂时返回一个占位符
        return "/api/v1/admin/activities/export/download?timestamp=" + System.currentTimeMillis();
    }

    @Override
    public List<Activity> getUserParticipatedActivities(Long userId) {
        return activityMapper.getUserParticipatedActivities(userId);
    }
}


