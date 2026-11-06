package com.volunteer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.RecruitmentInfo;
import com.volunteer.vo.Result;
import com.volunteer.service.RecruitmentService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/v1/recruitment")
public class RecruitmentController {

    @Resource
    private RecruitmentService recruitmentService;

    @GetMapping
    public Result<Page<RecruitmentInfo>> list(@RequestParam(required = false) String keyword,
                                              @RequestParam(required = false) Integer status,
                                              @RequestParam(defaultValue = "1") int pageNo,
                                              @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(recruitmentService.pageList(keyword, status, pageNo, pageSize));
    }

    @GetMapping("/{id}")
    public Result<RecruitmentInfo> detail(@PathVariable Long id) {
        return Result.success(recruitmentService.findDetail(id));
    }

    @PostMapping("/{id}/apply")
    public Result<Boolean> apply(@PathVariable("id") Long recruitmentId,
                                 @RequestBody(required = false) java.util.Map<String, String> requestBody,
                                 javax.servlet.http.HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("未授权");
        }
        
        // 从请求体中提取参数
        String intro = requestBody != null ? requestBody.get("intro") : null;
        String skills = requestBody != null ? requestBody.get("skills") : null;
        String experience = requestBody != null ? requestBody.get("experience") : null;
        
        return Result.success(recruitmentService.apply(recruitmentId, userId, intro, skills, experience));
    }
}


