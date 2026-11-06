package com.volunteer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.dto.VolunteerUserBatchDTO;
import com.volunteer.dto.VolunteerUserQueryDTO;
import com.volunteer.dto.VolunteerUserUpdateDTO;
import com.volunteer.entity.EmergencyContact;
import com.volunteer.mapper.EmergencyContactMapper;
import com.volunteer.service.VolunteerUserService;
import com.volunteer.vo.Result;
import com.volunteer.vo.VolunteerStatisticsVO;
import com.volunteer.vo.VolunteerUserDetailVO;
import com.volunteer.vo.VolunteerUserListVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 志愿者管理控制器
 * 
 * @author volunteer-team
 * @since 2025-01-20
 */
@Slf4j
@RestController
@RequestMapping("/admin/volunteers")
public class VolunteerManagementController {

    @Autowired
    private VolunteerUserService volunteerUserService;

    @Autowired
    private EmergencyContactMapper emergencyContactMapper;

    /**
     * 分页查询志愿者列表
     */
    @PostMapping("/list")
    public Result<Page<VolunteerUserListVO>> list(@RequestBody VolunteerUserQueryDTO query) {
        try {
            Page<VolunteerUserListVO> result = volunteerUserService.getVolunteerList(query);
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询志愿者列表失败：{}", e.getMessage());
            return Result.error("查询志愿者列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取志愿者详情
     */
    @GetMapping("/detail/{userId}")
    public Result<VolunteerUserDetailVO> detail(@PathVariable Long userId) {
        try {
            VolunteerUserDetailVO result = volunteerUserService.getVolunteerDetail(userId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取志愿者详情失败：{}", e.getMessage());
            return Result.error("获取志愿者详情失败：" + e.getMessage());
        }
    }

    /**
     * 更新志愿者信息
     */
    @PutMapping("/update")
    public Result<Boolean> update(@RequestBody VolunteerUserUpdateDTO updateDTO) {
        try {
            boolean result = volunteerUserService.updateVolunteer(updateDTO);
            return Result.success(result);
        } catch (Exception e) {
            log.error("更新志愿者信息失败：{}", e.getMessage());
            return Result.error("更新志愿者信息失败：" + e.getMessage());
        }
    }

    /**
     * 更新志愿者状态
     */
    @PutMapping("/status/{userId}")
    public Result<Boolean> updateStatus(@PathVariable Long userId, @RequestParam Integer status) {
        try {
            boolean result = volunteerUserService.updateVolunteerStatus(userId, status);
            return Result.success(result);
        } catch (Exception e) {
            log.error("更新志愿者状态失败：{}", e.getMessage());
            return Result.error("更新志愿者状态失败：" + e.getMessage());
        }
    }

    /**
     * 批量操作志愿者
     */
    @PostMapping("/batch")
    public Result<Boolean> batchOperate(@RequestBody VolunteerUserBatchDTO batchDTO) {
        try {
            boolean result = volunteerUserService.batchOperateVolunteers(batchDTO);
            return Result.success(result);
        } catch (Exception e) {
            log.error("批量操作志愿者失败：{}", e.getMessage());
            return Result.error("批量操作志愿者失败：" + e.getMessage());
        }
    }

    /**
     * 删除志愿者
     */
    @DeleteMapping("/delete/{userId}")
    public Result<Boolean> delete(@PathVariable Long userId) {
        try {
            boolean result = volunteerUserService.deleteVolunteer(userId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("删除志愿者失败：{}", e.getMessage());
            return Result.error("删除志愿者失败：" + e.getMessage());
        }
    }

    /**
     * 手动评级志愿者
     */
    @PutMapping("/rate/{userId}")
    public Result<Boolean> rateVolunteer(@PathVariable Long userId, @RequestParam Integer starLevel) {
        try {
            // 获取当前登录管理员ID（这里简化处理，实际应该从token中获取）
            Long ratingBy = 1L; // TODO: 从认证信息中获取当前管理员ID
            
            boolean result = volunteerUserService.rateVolunteer(userId, starLevel, ratingBy);
            return Result.success(result);
        } catch (Exception e) {
            log.error("评级志愿者失败：{}", e.getMessage());
            return Result.error("评级志愿者失败：" + e.getMessage());
        }
    }

    /**
     * 更新志愿者评级
     */
    @PutMapping("/rating")
    public Result<Boolean> updateRating(@RequestBody Map<String, Object> ratingData) {
        try {
            Long userId = Long.valueOf(ratingData.get("id").toString());
            Integer starLevel = Integer.valueOf(ratingData.get("starLevel").toString());
            String ratingRemark = ratingData.get("ratingRemark") != null ? ratingData.get("ratingRemark").toString() : "";
            
            // 获取当前登录管理员ID（这里简化处理，实际应该从token中获取）
            Long ratingBy = 1L; // TODO: 从认证信息中获取当前管理员ID
            
            boolean result = volunteerUserService.updateVolunteerRating(userId, starLevel, ratingRemark, ratingBy);
            return Result.success(result);
        } catch (Exception e) {
            log.error("更新志愿者评级失败：{}", e.getMessage());
            return Result.error("更新志愿者评级失败：" + e.getMessage());
        }
    }

    /**
     * 获取志愿者统计信息
     */
    @GetMapping("/statistics")
    public Result<VolunteerStatisticsVO> statistics() {
        try {
            VolunteerStatisticsVO result = volunteerUserService.getVolunteerStatistics();
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取志愿者统计信息失败：{}", e.getMessage());
            return Result.error("获取志愿者统计信息失败：" + e.getMessage());
        }
    }

    /**
     * 获取志愿者的紧急联系人信息
     */
    @GetMapping("/{userId}/emergency-contacts")
    public Result<List<VolunteerUserDetailVO.EmergencyContactInfo>> getEmergencyContacts(@PathVariable Long userId) {
        try {
            List<EmergencyContact> contacts = emergencyContactMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<EmergencyContact>()
                    .eq(EmergencyContact::getUserId, userId)
                    .orderByDesc(EmergencyContact::getIsPrimary)
                    .orderByDesc(EmergencyContact::getUpdatedTime)
            );
            
            List<VolunteerUserDetailVO.EmergencyContactInfo> contactInfos = contacts.stream().map(contact -> {
                VolunteerUserDetailVO.EmergencyContactInfo info = new VolunteerUserDetailVO.EmergencyContactInfo();
                info.setId(contact.getId());
                info.setName(contact.getName());
                info.setPhone(contact.getPhone());
                info.setRelation(contact.getRelation());
                info.setIsPrimary(contact.getIsPrimary());
                info.setIsPrimaryText(contact.getIsPrimary() != null && contact.getIsPrimary() == 1 ? "是" : "否");
                info.setCreatedTime(contact.getCreatedTime());
                info.setUpdatedTime(contact.getUpdatedTime());
                return info;
            }).collect(Collectors.toList());
            
            log.info("获取志愿者紧急联系人成功: userId={}, count={}", userId, contactInfos.size());
            return Result.success(contactInfos);
        } catch (Exception e) {
            log.error("获取志愿者紧急联系人失败：userId={}, error={}", userId, e.getMessage());
            return Result.error("获取志愿者紧急联系人失败：" + e.getMessage());
        }
    }
}
