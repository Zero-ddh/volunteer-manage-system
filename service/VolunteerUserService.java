package com.volunteer.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.volunteer.dto.VolunteerUserBatchDTO;
import com.volunteer.dto.VolunteerUserQueryDTO;
import com.volunteer.dto.VolunteerUserUpdateDTO;
import com.volunteer.entity.VolunteerUser;
import com.volunteer.vo.VolunteerStatisticsVO;
import com.volunteer.vo.VolunteerUserDetailVO;
import com.volunteer.vo.VolunteerUserListVO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 志愿者用户Service接口
 * 
 * @author volunteer-team
 * @since 2025-01-17
 */
public interface VolunteerUserService extends IService<VolunteerUser> {

    /**
     * 根据用户名查找用户
     */
    VolunteerUser findByUsername(String username);

    /**
     * 根据手机号查找用户
     */
    VolunteerUser findByPhone(String phone);

    /**
     * 用户注册
     */
    boolean register(VolunteerUser user);

    /**
     * 用户登录
     */
    String login(String username, String password);
    
    /**
     * 修改密码
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);
    
    /**
     * 分页查询志愿者用户列表
     */
    Page<VolunteerUserListVO> getVolunteerList(VolunteerUserQueryDTO query);
    
    /**
     * 获取志愿者用户详情
     */
    VolunteerUserDetailVO getVolunteerDetail(Long userId);
    
    /**
     * 更新志愿者用户信息
     */
    boolean updateVolunteer(VolunteerUserUpdateDTO updateDTO);
    
    /**
     * 更新志愿者用户状态
     */
    boolean updateVolunteerStatus(Long userId, Integer status);
    
    /**
     * 批量操作志愿者用户
     */
    boolean batchOperateVolunteers(VolunteerUserBatchDTO batchDTO);
    
    /**
     * 删除志愿者用户
     */
    boolean deleteVolunteer(Long userId);
    
    /**
     * 获取志愿者统计信息
     */
    VolunteerStatisticsVO getVolunteerStatistics();
    
    /**
     * 获取星级志愿者列表
     */
    Map<String, Object> getStarVolunteers(String name, Integer starLevel, Integer page, Integer size);
    
    /**
     * 获取积分排行榜
     */
    List<Map<String, Object>> getPointsRanking();
    
    /**
     * 手动评级志愿者
     */
    boolean rateVolunteer(Long userId, Integer starLevel, Long ratingBy);
    
    /**
     * 更新志愿者评级
     */
    boolean updateVolunteerRating(Long userId, Integer starLevel, String ratingRemark, Long ratingBy);
    
    /**
     * 更新用户总服务时长
     */
    boolean updateTotalServiceHours(Long userId, BigDecimal serviceHours);
    
    /**
     * 累加用户服务时长
     */
    boolean addServiceHours(Long userId, BigDecimal additionalHours);
    
    /**
     * 全量重新计算用户服务时长（从签到表统计）
     */
    boolean recalculateTotalServiceHours(Long userId);
    
    /**
     * 在新事务中全量重新计算用户服务时长（确保读取到已提交的数据）
     */
    boolean recalculateTotalServiceHoursInNewTransaction(Long userId);
}
