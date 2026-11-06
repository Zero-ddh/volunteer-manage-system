package com.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.volunteer.entity.SysUser;
import com.volunteer.entity.VolunteerUser;
import com.volunteer.mapper.SysUserMapper;
import com.volunteer.service.VolunteerUserService;
import com.volunteer.utils.JwtUtils;
import com.volunteer.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 
 * @author volunteer-team
 * @since 2025-01-17
 */
@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private VolunteerUserService volunteerUserService;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody VolunteerUser user) {
        try {
            log.info("用户注册请求：{}", user.getUsername());
            
            volunteerUserService.register(user);
            
            // 注册成功
            Map<String, Object> data = new HashMap<>();
            data.put("username", user.getUsername());
            data.put("message", "注册成功");
            return Result.success("注册成功", data);
        } catch (RuntimeException e) {
            // 捕获业务异常（用户名已存在、手机号已被注册等）
            log.error("用户注册失败：{}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            // 捕获其他异常
            log.error("用户注册异常：{}", e.getMessage());
            return Result.error("注册失败，请稍后重试");
        }
    }

    /**
     * 管理员登录
     */
    @PostMapping("/admin/login")
    public Result<Map<String, Object>> adminLogin(@RequestBody Map<String, String> loginData) {
        try {
            String username = loginData.get("username");
            String password = loginData.get("password");
            
            log.info("管理员登录请求：username={}", username);
            
            // 检查用户名和密码是否为空
            if (username == null || username.trim().isEmpty()) {
                return Result.error("用户名不能为空");
            }
            
            if (password == null || password.trim().isEmpty()) {
                return Result.error("密码不能为空");
            }
            
            // 查找管理员用户
            QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", username);
            queryWrapper.eq("status", 1); // 只查询正常状态的用户
            SysUser admin = sysUserMapper.selectOne(queryWrapper);
            
            if (admin == null) {
                return Result.error("用户名不存在");
            }
            
            if (!password.equals(admin.getPassword())) {
                return Result.error("密码错误");
            }
            
            // 生成JWT token
            String token = jwtUtils.generateToken(admin.getId(), admin.getUsername());
            
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("adminInfo", admin);
            data.put("username", admin.getUsername());
            data.put("message", "登录成功");
            
            return Result.success("登录成功", data);
        } catch (Exception e) {
            log.error("管理员登录异常：{}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
        try {
            String username = loginData.get("username");
            String phone = loginData.get("phone");
            String password = loginData.get("password");
            
            log.info("用户登录请求：username={}, phone={}", username, phone);
            
            // 检查用户名或手机号是否为空
            if ((username == null || username.trim().isEmpty()) && 
                (phone == null || phone.trim().isEmpty())) {
                return Result.error("用户名或手机号不能为空");
            }
            
            if (password == null || password.trim().isEmpty()) {
                return Result.error("密码不能为空");
            }
            
            String token;
            String loginIdentifier;
            
            // 优先使用手机号登录
            if (phone != null && !phone.trim().isEmpty()) {
                VolunteerUser user = volunteerUserService.findByPhone(phone);
                if (user == null) {
                    return Result.error("手机号不存在");
                }
                if (!password.equals(user.getPassword())) {
                    return Result.error("密码错误");
                }
                token = jwtUtils.generateToken(user.getId(), user.getUsername());
                loginIdentifier = user.getUsername();
            } else {
                // 使用用户名登录
                token = volunteerUserService.login(username, password);
                loginIdentifier = username;
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("username", loginIdentifier);
            data.put("message", "登录成功");
            
            return Result.success("登录成功", data);
        } catch (Exception e) {
            log.error("用户登录异常：{}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        // JWT是无状态的，登出只需要前端删除token即可
        return Result.success("登出成功", "登出成功");
    }

    /**
     * 忘记密码
     */
    @PostMapping("/forgot-password")
    public Result<String> forgotPassword(@RequestBody Map<String, String> data) {
        try {
            String phone = data.get("phone");
            String verifyCode = data.get("verifyCode");
            String newPassword = data.get("newPassword");
            
            log.info("忘记密码请求：phone={}", phone);
            
            // 检查参数
            if (phone == null || phone.trim().isEmpty()) {
                return Result.error("手机号不能为空");
            }
            if (verifyCode == null || verifyCode.trim().isEmpty()) {
                return Result.error("验证码不能为空");
            }
            if (newPassword == null || newPassword.trim().isEmpty()) {
                return Result.error("新密码不能为空");
            }
            if (newPassword.length() < 6) {
                return Result.error("密码长度至少6位");
            }
            
            // 验证码暂时不校验，随便输入数字都可以通过
            // TODO: 后续接入真实短信验证码
            
            // 查找用户
            VolunteerUser user = volunteerUserService.findByPhone(phone);
            if (user == null) {
                return Result.error("该手机号未注册");
            }
            
            // 重置密码（明文）
            user.setPassword(newPassword);
            boolean success = volunteerUserService.updateById(user);
            
            if (success) {
                return Result.success("密码重置成功");
            } else {
                return Result.error("密码重置失败");
            }
        } catch (Exception e) {
            log.error("忘记密码异常：{}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 测试接口
     */
    @GetMapping("/test")
    public Result<String> test() {
        return Result.success("认证服务正常");
    }
}
