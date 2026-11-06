package com.volunteer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.Checkin;
import com.volunteer.entity.VolunteerUser;
import com.volunteer.service.CheckinService;
import com.volunteer.service.QRCodeService;
import com.volunteer.service.RegistrationService;
import com.volunteer.service.VolunteerUserService;
import com.volunteer.vo.CheckinRequest;
import com.volunteer.vo.CheckinDetailVO;
import com.volunteer.vo.Result;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/v1")
public class CheckinController {

    @Resource
    private CheckinService checkinService;
    
    @Resource
    private QRCodeService qrCodeService;
    
    @Resource
    private VolunteerUserService volunteerUserService;
    
    @Resource
    private RegistrationService registrationService;

    /**
     * 活动签到
     */
    @PostMapping("/activities/{id}/checkin")
    public Result<Boolean> checkin(@PathVariable("id") Long activityId,
                                   @RequestBody CheckinRequest request,
                                   HttpServletRequest httpRequest) {
        try {
            // 从JWT上下文中解析userId
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return Result.error("用户未登录");
            }

            // 检查用户是否已报名并通过审核
            if (!registrationService.isUserRegisteredAndApproved(activityId, userId)) {
                return Result.error("签到失败，您还没报名参加活动，请先报名！");
            }

            // 验证GPS位置 - 暂时屏蔽位置校验
            // if (!checkinService.validateLocation(activityId, request.getLocation())) {
            //     return Result.error("签到位置不符合要求，请确保您在活动现场");
            // }

            // 验证二维码 - 暂时屏蔽二维码校验
            // if (!checkinService.validateQRCode(activityId, request.getQrCode())) {
            //     return Result.error("二维码验证失败，请重新扫描");
            // }

            // 执行签到
            boolean result = checkinService.checkin(activityId, userId, 
                request.getLocation(), request.getRemark());
            
            if (result) {
                return Result.success("签到成功", true);
            } else {
                return Result.error("签到失败，请重试");
            }
        } catch (Exception e) {
            return Result.error("签到过程中发生错误：" + e.getMessage());
        }
    }

    /**
     * 活动签退
     */
    @PostMapping("/activities/{id}/checkout")
    public Result<Boolean> checkout(@PathVariable("id") Long activityId,
                                   @RequestBody CheckinRequest request,
                                   HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return Result.error("用户未登录");
            }

            // 检查用户是否已报名并通过审核
            if (!registrationService.isUserRegisteredAndApproved(activityId, userId)) {
                return Result.error("签退失败，您还没报名参加活动，请先报名！");
            }

            // 检查签退时间是否与签到时间为同一天
            if (!checkinService.validateCheckoutSameDay(activityId, userId)) {
                return Result.error("您今天还没有签到或已经签退过此活动，无法进行签退操作！");
            }

            boolean result = checkinService.checkout(activityId, userId, 
                request.getLocation(), request.getRemark());
            
            if (result) {
                return Result.success("签退成功", true);
            } else {
                return Result.error("签退失败，请重试");
            }
        } catch (Exception e) {
            return Result.error("签退过程中发生错误：" + e.getMessage());
        }
    }

    /**
     * 生成活动二维码（无需认证）
     */
    @GetMapping("/activities/{id}/qr-code")
    @PreAuthorize("permitAll()")
    public Result<Map<String, Object>> generateQRCode(@PathVariable("id") Long activityId) {
        try {
            // 生成演示用的URL（本地开发环境）
            String qrCode = "http://localhost:3000/checkin/activity_" + activityId + "_demo";
            
            // 生成二维码图片（Base64编码）
            String qrImageBase64 = qrCodeService.generateQRCodeImage(qrCode);
            
            Map<String, Object> result = new HashMap<>();
            result.put("qrCode", qrCode);
            result.put("qrImage", qrImageBase64);
            result.put("expireTime", System.currentTimeMillis() + 48 * 60 * 60 * 1000); // 48小时后过期
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("生成二维码失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户签到记录
     */
    @GetMapping("/user/checkins")
    public Result<Page<Checkin>> my(HttpServletRequest request,
                                   @RequestParam(defaultValue = "1") int pageNo,
                                   @RequestParam(defaultValue = "10") int pageSize) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            Page<Checkin> result = checkinService.myCheckins(userId, pageNo, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取签到记录失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户签到记录（包含活动信息）
     */
    @GetMapping("/user/checkins-with-activity")
    public Result<Page<CheckinDetailVO>> getUserCheckinsWithActivity(HttpServletRequest request,
                                                               @RequestParam(defaultValue = "1") int pageNo,
                                                               @RequestParam(defaultValue = "10") int pageSize,
                                                               @RequestParam(required = false) String activityTitle) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            Page<CheckinDetailVO> result = checkinService.getUserCheckinsWithActivity(userId, pageNo, pageSize, activityTitle);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取签到记录失败：" + e.getMessage());
        }
    }

    /**
     * 自动计算服务时长
     */
    @PostMapping("/checkins/{id}/auto-calculate-hours")
    public Result<Boolean> autoCalculateServiceHours(@PathVariable Long id) {
        try {
            boolean result = checkinService.autoCalculateServiceHoursFromCheckin(id);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("自动计算服务时长失败：" + e.getMessage());
        }
    }

    /**
     * 更新用户总服务时长（从签到表重新统计）
     */
    @PostMapping("/admin/checkins/update-total-service-hours")
    public Result<Map<String, Object>> updateTotalServiceHours(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> userIds = (List<Long>) request.get("userIds");
            
            // 如果传入空数组或null，则批量更新所有用户
            Map<String, Object> result = checkinService.updateUsersTotalServiceHours(userIds);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("更新用户总服务时长失败：" + e.getMessage());
        }
    }

    /**
     * 管理后台获取签到列表
     */
    @GetMapping("/admin/checkins")
    public Result<Page<CheckinDetailVO>> adminList(@RequestParam(defaultValue = "1") int pageNo,
                                          @RequestParam(defaultValue = "10") int pageSize,
                                          @RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) Long activityId,
                                          @RequestParam(required = false) Integer status) {
        try {
            Page<CheckinDetailVO> result = checkinService.adminPageListWithUserInfo(pageNo, pageSize, keyword, activityId, status);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取签到列表失败：" + e.getMessage());
        }
    }

    /**
     * 更新签到状态
     */
    @PutMapping("/admin/checkins/{id}/status")
    public Result<Boolean> updateCheckinStatus(@PathVariable Long id,
                                               @RequestBody java.util.Map<String, Object> requestBody) {
        try {
            Integer status = Integer.valueOf(requestBody.get("status").toString());
            String remark = (String) requestBody.get("remark");
            boolean result = checkinService.updateCheckinStatus(id, status, remark);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("更新签到状态失败：" + e.getMessage());
        }
    }

    /**
     * 更新服务时长
     */
    @PutMapping("/admin/checkins/{id}/service-hours")
    public Result<Boolean> updateServiceHours(@PathVariable Long id,
                                              @RequestBody java.util.Map<String, Object> requestBody) {
        try {
            Double serviceHours = Double.valueOf(requestBody.get("serviceHours").toString());
            boolean result = checkinService.updateServiceHours(id, serviceHours);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("更新服务时长失败：" + e.getMessage());
        }
    }

    /**
     * 批量更新服务时长
     */
    @PutMapping("/admin/checkins/batch-service-hours")
    @SuppressWarnings("unchecked")
    public Result<Boolean> batchUpdateServiceHours(@RequestBody java.util.Map<String, Object> requestBody) {
        try {
            java.util.List<Long> ids = (java.util.List<Long>) requestBody.get("ids");
            Double serviceHours = Double.valueOf(requestBody.get("serviceHours").toString());
            String remark = (String) requestBody.get("remark");
            
            boolean result = checkinService.batchUpdateServiceHours(ids, serviceHours, remark);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("批量更新服务时长失败：" + e.getMessage());
        }
    }

    /**
     * 导出签到数据
     */
    @GetMapping("/admin/checkins/export")
    public void exportCheckins(@RequestParam(required = false) String keyword,
                               @RequestParam(required = false) Long activityId,
                               @RequestParam(required = false) Integer status,
                               HttpServletResponse response) {
        try {
            checkinService.exportCheckins(keyword, activityId, status, response);
        } catch (Exception e) {
            try {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("导出失败：" + e.getMessage());
            } catch (IOException ioException) {
                // 忽略IO异常
            }
        }
    }

    /**
     * 检查用户今天是否已签到指定活动
     */
    @GetMapping("/activities/{id}/checkin-status")
    public Result<Map<String, Object>> checkTodayCheckinStatus(@PathVariable("id") Long activityId,
                                                               HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            // 检查今天是否已签到
            boolean hasCheckedInToday = checkinService.hasCheckedInToday(activityId, userId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("hasCheckedInToday", hasCheckedInToday);
            
            if (hasCheckedInToday) {
                // 获取今天的签到记录
                Checkin todayCheckin = checkinService.getTodayCheckin(activityId, userId);
                if (todayCheckin != null) {
                    result.put("checkinTime", todayCheckin.getCheckinTime());
                    result.put("checkinLocation", todayCheckin.getLocation());
                }
            }
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("检查签到状态失败：" + e.getMessage());
        }
    }

    /**
     * 获取当前用户基本信息（用于签到页面预填充）
     */
    @GetMapping("/user/checkin-info")
    public Result<Map<String, Object>> getCurrentUserCheckinInfo(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("用户未登录");
            }

            // 获取用户基本信息
            VolunteerUser user = volunteerUserService.getById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("name", user.getRealName());
            userInfo.put("phone", user.getPhone());
            userInfo.put("idCard", user.getIdCard());

            return Result.success(userInfo);
        } catch (Exception e) {
            return Result.error("获取用户信息失败：" + e.getMessage());
        }
    }
}


