package com.volunteer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.Registration;
import com.volunteer.service.RegistrationService;
import com.volunteer.service.RecruitmentService;
import com.volunteer.vo.RegistrationVO;
import com.volunteer.vo.RecruitmentAdmissionVO;
import com.volunteer.vo.RegistrationResult;
import com.volunteer.vo.Result;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/v1")
public class RegistrationController {

    @Resource
    private RegistrationService registrationService;

    @Resource
    private RecruitmentService recruitmentService;

    @PostMapping("/activities/{id}/register")
    public Result<RegistrationResult> register(@PathVariable("id") Long activityId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        RegistrationResult result = registrationService.registerWithStatus(activityId, userId);
        if (result.isSuccess()) {
            return Result.success(result.getMessage(), result);
        } else {
            return Result.result(500, result.getMessage(), result);
        }
    }

    @DeleteMapping("/activities/{id}/register")
    public Result<Boolean> cancel(@PathVariable("id") Long activityId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        boolean result = registrationService.cancel(activityId, userId);
        return Result.success(result);
    }

    @GetMapping("/user/registrations")
    public Result<Page<Registration>> my(HttpServletRequest request,
                                 @RequestParam(defaultValue = "1") int pageNo,
                                 @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = (Long) request.getAttribute("userId");
        Page<Registration> result = registrationService.myRegistrations(userId, pageNo, pageSize);
        return Result.success(result);
    }

    @GetMapping("/user/registrations/details")
    public Result<Page<RegistrationVO>> myWithDetails(HttpServletRequest request,
                                 @RequestParam(defaultValue = "1") int pageNo,
                                 @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = (Long) request.getAttribute("userId");
        Page<RegistrationVO> result = registrationService.myRegistrationsWithDetails(userId, pageNo, pageSize);
        return Result.success(result);
    }

    @GetMapping("/user/recruitment-applications")
    public Result<Page<RecruitmentAdmissionVO>> getMyRecruitmentApplications(HttpServletRequest request,
                                 @RequestParam(defaultValue = "1") int pageNo,
                                 @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = (Long) request.getAttribute("userId");
        Page<RecruitmentAdmissionVO> result = recruitmentService.getUserApplicationsWithDetails(userId, pageNo, pageSize);
        return Result.success(result);
    }

    @GetMapping("/activities/{id}/registrations")
    public Result<Page<Registration>> list(@PathVariable("id") Long activityId,
                                   @RequestParam(defaultValue = "1") int pageNo,
                                   @RequestParam(defaultValue = "10") int pageSize) {
        Page<Registration> result = registrationService.activityRegistrations(activityId, pageNo, pageSize);
        return Result.success(result);
    }

    /**
     * 管理后台分页查询报名列表
     */
    @GetMapping("/admin/registrations")
    public Result<Page<RegistrationVO>> adminList(@RequestParam(defaultValue = "1") int pageNo,
                                               @RequestParam(defaultValue = "10") int pageSize,
                                               @RequestParam(required = false) String keyword,
                                               @RequestParam(required = false) Long activityId,
                                               @RequestParam(required = false) Integer status) {
        Page<RegistrationVO> result = registrationService.adminPageListWithDetails(pageNo, pageSize, keyword, activityId, status);
        return Result.success(result);
    }

    /**
     * 获取报名详情
     */
    @GetMapping("/admin/registrations/{id}")
    public Result<RegistrationVO> getRegistrationDetail(@PathVariable Long id) {
        RegistrationVO result = registrationService.getRegistrationDetail(id);
        if (result == null) {
            return Result.error("报名信息不存在");
        }
        return Result.success(result);
    }

    /**
     * 管理后台审核报名
     */
    @PostMapping("/admin/registrations/{id}/review")
    public Result<Boolean> reviewRegistration(@PathVariable Long id,
                                            @RequestBody java.util.Map<String, Object> requestBody) {
        Integer status = (Integer) requestBody.get("status");
        String remark = (String) requestBody.get("remark");
        boolean result = registrationService.auditRegistration(id, status, remark);
        return Result.success(result);
    }

    /**
     * 管理后台批量审核报名
     */
    @PostMapping("/admin/registrations/batch-review")
    @SuppressWarnings("unchecked")
    public Result<Boolean> batchReviewRegistrations(@RequestBody java.util.Map<String, Object> requestBody) {
        java.util.List<Long> ids = (java.util.List<Long>) requestBody.get("ids");
        Integer status = (Integer) requestBody.get("status");
        String remark = (String) requestBody.get("remark");
        boolean result = registrationService.batchAuditRegistrations(ids, status, remark);
        return Result.success(result);
    }

    /**
     * 生成电子报名表（返回JSON数据供前端生成PDF）
     */
    @GetMapping("/registrations/{id}/form-data")
    public Result<RegistrationVO> getRegistrationFormData(@PathVariable Long id) {
        RegistrationVO registrationVO = registrationService.getRegistrationDetail(id);
        if (registrationVO == null) {
            return Result.error("报名信息不存在");
        }
        return Result.success(registrationVO);
    }
    
    /**
     * 生成电子报名表（保留旧接口以兼容）
     */
    @GetMapping("/registrations/{id}/form")
    public void generateRegistrationForm(@PathVariable Long id, 
                                        HttpServletResponse response) throws IOException {
        try {
            registrationService.generateRegistrationForm(id, response);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("生成报名表失败：" + e.getMessage());
        }
    }
}


