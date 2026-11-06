package com.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.InsuranceOrder;
import com.volunteer.entity.VolunteerUser;
import com.volunteer.mapper.InsuranceOrderMapper;
import com.volunteer.service.VolunteerUserService;
import com.volunteer.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理后台保险订单控制器
 * 
 * @author volunteer-team
 * @since 2025-01-24
 */
@Slf4j
@RestController
@RequestMapping("/admin/insurance-orders")
public class AdminInsuranceOrderController {
    
    @Autowired
    private InsuranceOrderMapper insuranceOrderMapper;
    
    @Autowired
    private VolunteerUserService volunteerUserService;

    /**
     * 分页查询保险订单列表
     */
    @GetMapping("/list")
    public Result<Page<Map<String, Object>>> getOrderList(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer paymentStatus,
            @RequestParam(required = false) Integer orderStatus,
            @RequestParam(required = false) Long productId) {
        try {
            Page<InsuranceOrder> page = new Page<>(pageNo, pageSize);
            QueryWrapper<InsuranceOrder> queryWrapper = new QueryWrapper<>();
            
            // 关键词搜索（订单号、产品名称）
            if (keyword != null && !keyword.trim().isEmpty()) {
                queryWrapper.and(wrapper -> wrapper
                    .like("order_no", keyword)
                    .or().like("product_name", keyword)
                );
            }
            
            // 支付状态筛选
            if (paymentStatus != null) {
                queryWrapper.eq("payment_status", paymentStatus);
            }
            
            // 订单状态筛选
            if (orderStatus != null) {
                queryWrapper.eq("order_status", orderStatus);
            }
            
            // 产品ID筛选
            if (productId != null) {
                queryWrapper.eq("product_id", productId);
            }
            
            // 按创建时间倒序排列
            queryWrapper.orderByDesc("created_time");
            
            Page<InsuranceOrder> orderPage = insuranceOrderMapper.selectPage(page, queryWrapper);
            
            // 转换为包含用户信息的Map
            Page<Map<String, Object>> resultPage = new Page<>(pageNo, pageSize);
            resultPage.setTotal(orderPage.getTotal());
            
            List<Map<String, Object>> orderList = orderPage.getRecords().stream().map(order -> {
                Map<String, Object> orderMap = new HashMap<>();
                orderMap.put("id", order.getId());
                orderMap.put("orderNo", order.getOrderNo());
                orderMap.put("userId", order.getUserId());
                orderMap.put("productId", order.getProductId());
                orderMap.put("productName", order.getProductName());
                orderMap.put("price", order.getPrice());
                orderMap.put("paymentMethod", order.getPaymentMethod());
                orderMap.put("paymentStatus", order.getPaymentStatus());
                orderMap.put("orderStatus", order.getOrderStatus());
                orderMap.put("effectiveTime", order.getEffectiveTime());
                orderMap.put("expireTime", order.getExpireTime());
                orderMap.put("createdTime", order.getCreatedTime());
                orderMap.put("updatedTime", order.getUpdatedTime());
                
                // 获取用户信息
                try {
                    VolunteerUser user = volunteerUserService.getById(order.getUserId());
                    if (user != null) {
                        orderMap.put("userName", user.getRealName());
                        orderMap.put("userPhone", user.getPhone());
                        orderMap.put("userEmail", user.getEmail());
                    } else {
                        orderMap.put("userName", "未知用户");
                        orderMap.put("userPhone", "");
                        orderMap.put("userEmail", "");
                    }
                } catch (Exception e) {
                    orderMap.put("userName", "未知用户");
                    orderMap.put("userPhone", "");
                    orderMap.put("userEmail", "");
                }
                
                return orderMap;
            }).collect(Collectors.toList());
            
            resultPage.setRecords(orderList);
            
            log.info("查询保险订单列表成功: pageNo={}, pageSize={}, keyword={}, paymentStatus={}, orderStatus={}, productId={}", 
                pageNo, pageSize, keyword, paymentStatus, orderStatus, productId);
            
            return Result.success(resultPage);
        } catch (Exception e) {
            log.error("查询保险订单列表失败", e);
            return Result.error("查询保险订单列表失败");
        }
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getOrderDetail(@PathVariable Long id) {
        try {
            InsuranceOrder order = insuranceOrderMapper.selectById(id);
            if (order == null) {
                return Result.error("保险订单不存在");
            }
            
            Map<String, Object> detail = new HashMap<>();
            detail.put("id", order.getId());
            detail.put("orderNo", order.getOrderNo());
            detail.put("userId", order.getUserId());
            detail.put("productId", order.getProductId());
            detail.put("productName", order.getProductName());
            detail.put("price", order.getPrice());
            detail.put("paymentMethod", order.getPaymentMethod());
            detail.put("paymentStatus", order.getPaymentStatus());
            detail.put("orderStatus", order.getOrderStatus());
            detail.put("effectiveTime", order.getEffectiveTime());
            detail.put("expireTime", order.getExpireTime());
            detail.put("createdTime", order.getCreatedTime());
            detail.put("updatedTime", order.getUpdatedTime());
            
            // 获取用户详细信息
            try {
                VolunteerUser user = volunteerUserService.getById(order.getUserId());
                if (user != null) {
                    detail.put("userName", user.getRealName());
                    detail.put("userPhone", user.getPhone());
                    detail.put("userEmail", user.getEmail());
                    detail.put("userIdCard", user.getIdCard());
                } else {
                    detail.put("userName", "未知用户");
                    detail.put("userPhone", "");
                    detail.put("userEmail", "");
                    detail.put("userIdCard", "");
                }
            } catch (Exception e) {
                detail.put("userName", "未知用户");
                detail.put("userPhone", "");
                detail.put("userEmail", "");
                detail.put("userIdCard", "");
            }
            
            log.info("获取保险订单详情成功: orderId={}", id);
            return Result.success(detail);
        } catch (Exception e) {
            log.error("获取保险订单详情失败: orderId={}", id, e);
            return Result.error("获取保险订单详情失败");
        }
    }

    /**
     * 更新订单状态
     */
    @PutMapping("/{id}/status")
    public Result<String> updateOrderStatus(@PathVariable Long id, @RequestParam Integer status) {
        try {
            InsuranceOrder order = insuranceOrderMapper.selectById(id);
            if (order == null) {
                return Result.error("保险订单不存在");
            }
            
            // 状态验证
            if (status < 1 || status > 4) {
                return Result.error("无效的状态值");
            }
            
            // 更新状态
            order.setOrderStatus(status);
            order.setUpdatedTime(LocalDateTime.now());
            
            int result = insuranceOrderMapper.updateById(order);
            if (result > 0) {
                String statusText = getOrderStatusText(status);
                log.info("更新保险订单状态成功: orderId={}, status={}", id, statusText);
                return Result.success("订单状态更新成功");
            } else {
                return Result.error("更新订单状态失败");
            }
        } catch (Exception e) {
            log.error("更新保险订单状态失败: orderId={}, status={}", id, status, e);
            return Result.error("更新订单状态失败");
        }
    }

    /**
     * 获取订单统计
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getOrderStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            // 总订单数
            QueryWrapper<InsuranceOrder> totalQuery = new QueryWrapper<>();
            long totalOrders = insuranceOrderMapper.selectCount(totalQuery);
            statistics.put("totalOrders", totalOrders);
            
            // 待支付订单数
            QueryWrapper<InsuranceOrder> pendingQuery = new QueryWrapper<>();
            pendingQuery.eq("payment_status", 1);
            long pendingOrders = insuranceOrderMapper.selectCount(pendingQuery);
            statistics.put("pendingOrders", pendingOrders);
            
            // 已支付订单数
            QueryWrapper<InsuranceOrder> paidQuery = new QueryWrapper<>();
            paidQuery.eq("payment_status", 2);
            long paidOrders = insuranceOrderMapper.selectCount(paidQuery);
            statistics.put("paidOrders", paidOrders);
            
            // 已生效订单数
            QueryWrapper<InsuranceOrder> effectiveQuery = new QueryWrapper<>();
            effectiveQuery.eq("order_status", 2);
            long effectiveOrders = insuranceOrderMapper.selectCount(effectiveQuery);
            statistics.put("effectiveOrders", effectiveOrders);
            
            // 已过期订单数
            QueryWrapper<InsuranceOrder> expiredQuery = new QueryWrapper<>();
            expiredQuery.eq("order_status", 3);
            long expiredOrders = insuranceOrderMapper.selectCount(expiredQuery);
            statistics.put("expiredOrders", expiredOrders);
            
            // 今日订单数
            QueryWrapper<InsuranceOrder> todayQuery = new QueryWrapper<>();
            todayQuery.ge("created_time", LocalDateTime.now().toLocalDate().atStartOfDay());
            long todayOrders = insuranceOrderMapper.selectCount(todayQuery);
            statistics.put("todayOrders", todayOrders);
            
            log.info("获取保险订单统计成功");
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取保险订单统计失败", e);
            return Result.error("获取保险订单统计失败");
        }
    }


    /**
     * 获取订单状态文本
     */
    private String getOrderStatusText(Integer status) {
        switch (status) {
            case 1: return "待支付";
            case 2: return "已生效";
            case 3: return "已过期";
            case 4: return "已取消";
            default: return "未知";
        }
    }
}
