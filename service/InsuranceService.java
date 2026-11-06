package com.volunteer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.volunteer.entity.InsuranceOrder;
import com.volunteer.entity.InsuranceProduct;

import java.util.List;
import java.util.Map;

/**
 * 保险服务接口
 * 
 * @author volunteer-team
 * @since 2025-01-22
 */
public interface InsuranceService extends IService<InsuranceProduct> {
    
    /**
     * 获取可用的保险产品列表
     * 
     * @return 保险产品列表
     */
    List<InsuranceProduct> getAvailableProducts();
    
    /**
     * 创建保险订单
     * 
     * @param userId 用户ID
     * @param productId 保险产品ID
     * @param paymentMethod 支付方式
     * @return 保险订单
     */
    InsuranceOrder createOrder(Long userId, Long productId, String paymentMethod);
    
    /**
     * 处理支付（模拟支付100%成功）
     * 
     * @param orderNo 订单号
     * @param paymentMethod 支付方式
     * @return 支付是否成功
     */
    boolean processPayment(String orderNo, String paymentMethod);
    
    /**
     * 获取用户保险订单列表
     * 
     * @param userId 用户ID
     * @return 保险订单列表
     */
    List<InsuranceOrder> getUserOrders(Long userId);
    
    /**
     * 根据订单号获取订单详情
     * 
     * @param orderNo 订单号
     * @return 保险订单
     */
    InsuranceOrder getOrderByOrderNo(String orderNo);
    
    /**
     * 获取用户保险订单列表（支持分页和搜索）
     * 
     * @param userId 用户ID
     * @param pageNo 页码
     * @param pageSize 每页大小
     * @param orderNo 订单号搜索
     * @param productName 产品名称搜索
     * @return 分页结果
     */
    Map<String, Object> getUserOrdersWithPagination(Long userId, int pageNo, int pageSize, String orderNo, String productName);
}

