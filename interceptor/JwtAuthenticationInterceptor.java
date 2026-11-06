package com.volunteer.interceptor;

import com.volunteer.exception.BusinessException;
import com.volunteer.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT认证拦截器
 * 
 * @author volunteer-team
 * @since 2025-01-17
 */
@Slf4j
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    private JwtUtils jwtUtils;

    public void setJwtUtils(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        log.debug("JWT拦截器处理请求：{}", requestURI);

        // 跳过不需要认证的路径
        if (requestURI.startsWith("/auth/") || 
            requestURI.startsWith("/public/") || 
            requestURI.startsWith("/error") ||
            requestURI.startsWith("/uploads/") ||
            requestURI.startsWith("/api/uploads/") ||
            requestURI.equals("/api/v1/activities") ||
            requestURI.equals("/api/v1/activity-categories") ||
            requestURI.matches("/api/v1/activities/\\d+/qr-code") ||
            requestURI.matches("/api/v1/activities/\\d+") ||
            requestURI.startsWith("/api/v1/recruitment") && !requestURI.contains("/apply") ||
            requestURI.startsWith("/api/v1/star-volunteers") ||
            requestURI.startsWith("/api/v1/goods/list") ||  // 商品列表不需要认证
            requestURI.equals("/api/v1/insurance/products") ||  // 保险产品列表不需要认证
            requestURI.startsWith("/api/v1/community/list") ||  // 社群列表不需要认证
            requestURI.matches("/api/v1/community/\\d+") ||  // 社群详情不需要认证
            // requestURI.matches("/api/v1/community/\\d+/posts") ||  // 社群帖子列表不需要认证 - 已屏蔽帖子功能
            requestURI.matches("/api/v1/community/\\d+/summary") ||  // 社群活动总结不需要认证
            requestURI.startsWith("/api/v1/activity-summaries")) {  // 活动总结列表不需要认证
            return true;
        }

        // 获取请求头中的Token
        String authHeader = request.getHeader(jwtUtils.getHeader());
        if (authHeader == null || !authHeader.startsWith(jwtUtils.getPrefix())) {
            log.warn("请求头中未找到有效的JWT Token：{}", requestURI);
            throw new BusinessException(401, "未授权访问");
        }

        // 提取Token
        String token = authHeader.substring(jwtUtils.getPrefix().length()).trim();
        if (token.isEmpty()) {
            log.warn("Token为空：{}", requestURI);
            throw new BusinessException(401, "Token不能为空");
        }

        // 验证Token
        try {
            Claims claims = jwtUtils.getClaimsFromToken(token);
            if (claims == null) {
                log.warn("Token解析失败：{}", requestURI);
                throw new BusinessException(401, "Token无效");
            }

            // 检查Token是否过期
            if (jwtUtils.isTokenExpired(token)) {
                log.warn("Token已过期：{}", requestURI);
                throw new BusinessException(401, "Token已过期");
            }

            // 将用户信息存储到请求属性中
            Long userId = jwtUtils.getUserIdFromToken(token);
            String username = jwtUtils.getUsernameFromToken(token);
            
            request.setAttribute("userId", userId);
            request.setAttribute("username", username);
            
            log.debug("JWT认证成功，用户ID：{}，用户名：{}", userId, username);
            return true;

        } catch (Exception e) {
            log.error("JWT认证失败：{}", e.getMessage());
            throw new BusinessException(401, "认证失败");
        }
    }
}
