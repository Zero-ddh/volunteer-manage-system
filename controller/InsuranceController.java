package com.volunteer.controller;

import com.volunteer.entity.InsuranceOrder;
import com.volunteer.entity.InsuranceProduct;
import com.volunteer.service.InsuranceService;
import com.volunteer.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 保险控制器
 * 
 * @author volunteer-team
 * @since 2025-01-22
 */
@Slf4j
@RestController
@RequestMapping("/v1/insurance")
public class InsuranceController {
    
    @Resource
    private InsuranceService insuranceService;
    
    /**
     * 获取保险产品列表
     */
    @GetMapping("/products")
    public Result<List<InsuranceProduct>> getProducts() {
        try {
            List<InsuranceProduct> products = insuranceService.getAvailableProducts();
            return Result.success(products);
        } catch (Exception e) {
            log.error("获取保险产品列表失败", e);
            return Result.error("获取保险产品列表失败");
        }
    }
    
    /**
     * 创建保险订单
     */
    @PostMapping("/order")
    public Result<InsuranceOrder> createOrder(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            Long productId = Long.valueOf(params.get("productId").toString());
            String paymentMethod = params.getOrDefault("paymentMethod", "mock").toString();
            
            InsuranceOrder order = insuranceService.createOrder(userId, productId, paymentMethod);
            return Result.success(order);
        } catch (Exception e) {
            log.error("创建保险订单失败", e);
            return Result.error("创建保险订单失败: " + e.getMessage());
        }
    }
    
    /**
     * 模拟支付处理（100%成功）
     */
    @PostMapping("/payment/mock")
    public Result<Map<String, Object>> mockPayment(@RequestBody Map<String, String> params) {
        try {
            String orderNo = params.get("orderNo");
            String paymentMethod = params.getOrDefault("paymentMethod", "mock");
            
            boolean success = insuranceService.processPayment(orderNo, paymentMethod);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            result.put("orderNo", orderNo);
            result.put("message", success ? "支付成功" : "支付失败");
            
            if (success) {
                // 获取订单详情
                InsuranceOrder order = insuranceService.getOrderByOrderNo(orderNo);
                result.put("order", order);
            }
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("模拟支付处理失败", e);
            return Result.error("支付处理失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户保险订单（支持分页和搜索）
     */
    @GetMapping("/orders")
    public Result<Map<String, Object>> getUserOrders(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String productName,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            Map<String, Object> result = insuranceService.getUserOrdersWithPagination(userId, pageNo, pageSize, orderNo, productName);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取用户保险订单失败", e);
            return Result.error("获取用户保险订单失败");
        }
    }
    
    /**
     * 根据订单号获取订单详情
     */
    @GetMapping("/order/{orderNo}")
    public Result<InsuranceOrder> getOrderDetail(@PathVariable String orderNo, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            InsuranceOrder order = insuranceService.getOrderByOrderNo(orderNo);
            if (order == null) {
                return Result.error("订单不存在");
            }
            
            // 检查订单是否属于当前用户
            if (!order.getUserId().equals(userId)) {
                return Result.error("无权限查看此订单");
            }
            
            return Result.success(order);
        } catch (Exception e) {
            log.error("获取订单详情失败", e);
            return Result.error("获取订单详情失败");
        }
    }
}

