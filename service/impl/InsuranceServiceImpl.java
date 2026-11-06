package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.volunteer.entity.InsuranceOrder;
import com.volunteer.entity.InsuranceProduct;
import com.volunteer.mapper.InsuranceOrderMapper;
import com.volunteer.mapper.InsuranceProductMapper;
import com.volunteer.service.InsuranceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 保险服务实现类
 * 
 * @author volunteer-team
 * @since 2025-01-22
 */
@Slf4j
@Service
public class InsuranceServiceImpl extends ServiceImpl<InsuranceProductMapper, InsuranceProduct> implements InsuranceService {
    
    @Resource
    private InsuranceProductMapper insuranceProductMapper;
    
    @Resource
    private InsuranceOrderMapper insuranceOrderMapper;
    
    @Override
    public List<InsuranceProduct> getAvailableProducts() {
        return insuranceProductMapper.selectList(
            new LambdaQueryWrapper<InsuranceProduct>()
                .eq(InsuranceProduct::getStatus, 1)
                .eq(InsuranceProduct::getDeleted, 0)
                .orderByDesc(InsuranceProduct::getCreatedTime)
        );
    }
    
    @Override
    @Transactional
    public InsuranceOrder createOrder(Long userId, Long productId, String paymentMethod) {
        // 1. 获取保险产品信息
        InsuranceProduct product = insuranceProductMapper.selectById(productId);
        if (product == null || product.getStatus() != 1) {
            throw new RuntimeException("保险产品不存在或已下架");
        }
        
        // 2. 生成订单号
        String orderNo = generateOrderNo();
        
        // 3. 创建订单
        InsuranceOrder order = new InsuranceOrder();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setProductId(productId);
        order.setProductName(product.getName());
        order.setPrice(product.getPrice());
        order.setPaymentMethod(paymentMethod);
        order.setPaymentStatus(1); // 待支付
        order.setOrderStatus(1); // 待支付
        
        insuranceOrderMapper.insert(order);
        
        log.info("创建保险订单成功: userId={}, productId={}, orderNo={}", userId, productId, orderNo);
        return order;
    }
    
    @Override
    @Transactional
    public boolean processPayment(String orderNo, String paymentMethod) {
        // 获取订单信息
        InsuranceOrder order = insuranceOrderMapper.selectOne(
            new LambdaQueryWrapper<InsuranceOrder>()
                .eq(InsuranceOrder::getOrderNo, orderNo)
        );
        
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        if (order.getPaymentStatus() != 1) {
            throw new RuntimeException("订单状态异常，无法支付");
        }
        
        // 模拟支付成功（100%成功率）
        log.info("开始处理支付: orderNo={}, paymentMethod={}", orderNo, paymentMethod);
        
        // 支付成功
        order.setPaymentStatus(2); // 已支付
        order.setOrderStatus(2); // 已生效
        order.setEffectiveTime(LocalDateTime.now());
        
        // 设置过期时间（根据保险期限）
        InsuranceProduct product = insuranceProductMapper.selectById(order.getProductId());
        if (product.getCoveragePeriod() != null) {
            order.setExpireTime(LocalDateTime.now().plusDays(product.getCoveragePeriod()));
        }
        
        insuranceOrderMapper.updateById(order);
        
        log.info("支付处理成功: orderNo={}, effectiveTime={}, expireTime={}", 
            orderNo, order.getEffectiveTime(), order.getExpireTime());
        
        return true;
    }
    
    @Override
    public List<InsuranceOrder> getUserOrders(Long userId) {
        return insuranceOrderMapper.selectList(
            new LambdaQueryWrapper<InsuranceOrder>()
                .eq(InsuranceOrder::getUserId, userId)
                .orderByDesc(InsuranceOrder::getCreatedTime)
        );
    }
    
    @Override
    public InsuranceOrder getOrderByOrderNo(String orderNo) {
        return insuranceOrderMapper.selectOne(
            new LambdaQueryWrapper<InsuranceOrder>()
                .eq(InsuranceOrder::getOrderNo, orderNo)
        );
    }
    
    @Override
    public Map<String, Object> getUserOrdersWithPagination(Long userId, int pageNo, int pageSize, String orderNo, String productName) {
        Page<InsuranceOrder> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<InsuranceOrder> queryWrapper = new LambdaQueryWrapper<InsuranceOrder>()
            .eq(InsuranceOrder::getUserId, userId);
        
        // 订单号搜索
        if (orderNo != null && !orderNo.trim().isEmpty()) {
            queryWrapper.like(InsuranceOrder::getOrderNo, orderNo.trim());
        }
        
        // 产品名称搜索
        if (productName != null && !productName.trim().isEmpty()) {
            queryWrapper.like(InsuranceOrder::getProductName, productName.trim());
        }
        
        // 按创建时间倒序排列
        queryWrapper.orderByDesc(InsuranceOrder::getCreatedTime);
        
        Page<InsuranceOrder> orderPage = insuranceOrderMapper.selectPage(page, queryWrapper);
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", orderPage.getRecords());
        result.put("total", orderPage.getTotal());
        result.put("current", orderPage.getCurrent());
        result.put("size", orderPage.getSize());
        result.put("pages", orderPage.getPages());
        
        return result;
    }
    
    /**
     * 生成订单号
     * 
     * @return 订单号
     */
    private String generateOrderNo() {
        return "INS" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }
}

