package com.volunteer.controller;

import com.volunteer.dto.UserSkillDTO;
import com.volunteer.entity.UserSkill;
import com.volunteer.entity.VolunteerUser;
import com.volunteer.service.UserSkillService;
import com.volunteer.service.VolunteerUserService;
import com.volunteer.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private VolunteerUserService volunteerUserService;

    @Autowired
    private UserSkillService userSkillService;

    @GetMapping("/profile")
    public Result<VolunteerUser> profile(@RequestParam("username") String username) {
        VolunteerUser user = volunteerUserService.findByUsername(username);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(user);
    }

    @PutMapping("/profile")
    public Result<String> updateProfile(@RequestBody VolunteerUser update) {
        if (update.getId() == null) {
            return Result.error("用户ID不能为空");
        }
        boolean ok = volunteerUserService.updateById(update);
        return ok ? Result.success("更新成功", "更新成功") : Result.error("更新失败");
    }

    @PutMapping("/change-password")
    public Result<String> changePassword(@RequestBody Map<String, String> passwordData) {
        try {
            Long userId = Long.valueOf(passwordData.get("userId"));
            String oldPassword = passwordData.get("oldPassword");
            String newPassword = passwordData.get("newPassword");
            
            if (userId == null || oldPassword == null || newPassword == null) {
                return Result.error("参数不能为空");
            }
            
            boolean success = volunteerUserService.changePassword(userId, oldPassword, newPassword);
            return success ? Result.success("密码修改成功", "密码修改成功") : Result.error("密码修改失败");
        } catch (Exception e) {
            log.error("修改密码失败：{}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/avatar")
    public Result<Map<String, Object>> uploadAvatar(@RequestParam("userId") Long userId,
                                                    @RequestParam("file") MultipartFile file) {
        // 这里先做占位实现，实际中请保存文件并生成URL
        String url = "/static/avatar/" + userId + ".png";
        VolunteerUser user = new VolunteerUser();
        user.setId(userId);
        user.setAvatar(url);
        volunteerUserService.updateById(user);

        Map<String, Object> data = new HashMap<>();
        data.put("url", url);
        return Result.success("上传成功", data);
    }

    // =============== 技能管理 ===============
    @GetMapping("/skills")
    public Result<List<UserSkillDTO>> listSkills(@RequestParam("userId") Long userId) {
        return Result.success(userSkillService.getUserSkills(userId));
    }

    @PostMapping("/skills")
    public Result<String> addSkill(@RequestBody UserSkill skill) {
        boolean ok = userSkillService.save(skill);
        return ok ? Result.success("新增成功", "新增成功") : Result.error("新增失败");
    }

    @PutMapping("/skills")
    public Result<String> updateSkill(@RequestBody UserSkill skill) {
        boolean ok = userSkillService.updateById(skill);
        return ok ? Result.success("更新成功", "更新成功") : Result.error("更新失败");
    }

    @DeleteMapping("/skills/{id}")
    public Result<String> deleteSkill(@PathVariable("id") Long id) {
        boolean ok = userSkillService.removeById(id);
        return ok ? Result.success("删除成功", "删除成功") : Result.error("删除失败");
    }
}


