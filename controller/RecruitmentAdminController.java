package com.volunteer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.RecruitmentInfo;
import com.volunteer.service.RecruitmentService;
import com.volunteer.vo.Result;
import com.volunteer.vo.RecruitmentApplicantVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/v1/admin/recruitment")
public class RecruitmentAdminController {

    @Resource
    private RecruitmentService recruitmentService;

    @PostMapping
    public Result<RecruitmentInfo> create(@RequestBody RecruitmentInfo info, HttpServletRequest request) {
        // 从JWT中获取当前用户ID作为组织者ID
        Long organizerId = (Long) request.getAttribute("userId");
        if (organizerId == null) {
            return Result.error("用户未登录");
        }
        info.setOrganizerId(organizerId);
        
        // 设置默认值
        if (info.getDeleted() == null) {
            info.setDeleted(0);
        }
        
        return Result.success(recruitmentService.create(info));
    }

    @GetMapping
    public Result<Page<RecruitmentInfo>> list(@RequestParam(required = false) String keyword,
                                              @RequestParam(required = false) Integer status,
                                              @RequestParam(defaultValue = "1") int pageNo,
                                              @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(recruitmentService.pageList(keyword, status, pageNo, pageSize));
    }

    @GetMapping("/{id}")
    public Result<RecruitmentInfo> getDetail(@PathVariable Long id) {
        RecruitmentInfo info = recruitmentService.findDetail(id);
        if (info == null) {
            return Result.error("招募信息不存在");
        }
        return Result.success(info);
    }

    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody RecruitmentInfo info) {
        return Result.success(recruitmentService.updateByIdSafe(id, info));
    }

    @GetMapping("/{id}/applicants")
    public Result<Page<RecruitmentApplicantVO>> applicants(@PathVariable("id") Long recruitmentId,
                                                         @RequestParam(required = false) String name,
                                                         @RequestParam(required = false) String phone,
                                                         @RequestParam(required = false) Integer status,
                                                         @RequestParam(defaultValue = "1") int pageNo,
                                                         @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(recruitmentService.listApplicantsWithUserInfo(recruitmentId, name, phone, status, pageNo, pageSize));
    }

    @PostMapping("/applications/{id}/review")
    public Result<Boolean> review(@PathVariable("id") Long admissionId,
                                @RequestBody ReviewRequest requestBody,
                                HttpServletRequest request) {
        // 获取当前用户ID
        Long reviewerId = (Long) request.getAttribute("userId");
        if (reviewerId == null) {
            return Result.error("用户未登录");
        }
        
        // 调用服务层审核方法
        return Result.success(recruitmentService.reviewApplication(admissionId, requestBody.getStatus(), requestBody.getRemark(), reviewerId));
    }
    
    // 审核请求DTO
    public static class ReviewRequest {
        private Integer status;
        private String remark;
        
        public Integer getStatus() {
            return status;
        }
        
        public void setStatus(Integer status) {
            this.status = status;
        }
        
        public String getRemark() {
            return remark;
        }
        
        public void setRemark(String remark) {
            this.remark = remark;
        }
    }
}


