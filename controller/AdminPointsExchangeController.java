package com.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.PointsExchange;
import com.volunteer.entity.VolunteerUser;
import com.volunteer.mapper.PointsExchangeMapper;
import com.volunteer.service.VolunteerUserService;
import com.volunteer.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理后台积分兑换记录控制器
 * 
 * @author volunteer-team
 * @since 2025-01-24
 */
@Slf4j
@RestController
@RequestMapping("/admin/points-exchange")
public class AdminPointsExchangeController {

    @Autowired
    private PointsExchangeMapper pointsExchangeMapper;
    
    @Autowired
    private VolunteerUserService volunteerUserService;

    /**
     * 分页查询兑换记录
     */
    @GetMapping("/list")
    public Result<Page<Map<String, Object>>> getExchangeRecords(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String goodsName,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String userPhone) {
        try {
            Page<PointsExchange> page = new Page<>(pageNo, pageSize);
            QueryWrapper<PointsExchange> queryWrapper = new QueryWrapper<>();
            
            // 状态筛选
            if (status != null) {
                queryWrapper.eq("status", status);
            }
            
            // 商品名称搜索
            if (goodsName != null && !goodsName.trim().isEmpty()) {
                queryWrapper.like("goods_name", goodsName.trim());
            }
            
            // 用户姓名搜索（通过用户表关联）
            if (userName != null && !userName.trim().isEmpty()) {
                queryWrapper.exists("SELECT 1 FROM volunteer_user vu WHERE vu.id = points_exchange.user_id AND vu.username LIKE {0}", "%" + userName.trim() + "%");
            }
            
            // 用户电话搜索（通过用户表关联）
            if (userPhone != null && !userPhone.trim().isEmpty()) {
                queryWrapper.exists("SELECT 1 FROM volunteer_user vu WHERE vu.id = points_exchange.user_id AND vu.phone LIKE {0}", "%" + userPhone.trim() + "%");
            }
            
            // 按兑换时间倒序排列
            queryWrapper.orderByDesc("exchange_time");
            
            Page<PointsExchange> exchangePage = pointsExchangeMapper.selectPage(page, queryWrapper);
            
            // 转换为包含用户信息的Map
            Page<Map<String, Object>> resultPage = new Page<>(pageNo, pageSize, exchangePage.getTotal());
            resultPage.setRecords(exchangePage.getRecords().stream().map(exchange -> {
                Map<String, Object> record = new HashMap<>();
                record.put("id", exchange.getId());
                record.put("userId", exchange.getUserId());
                record.put("goodsId", exchange.getGoodsId());
                record.put("goodsName", exchange.getGoodsName());
                record.put("pointsUsed", exchange.getPointsUsed());
                record.put("exchangeTime", exchange.getExchangeTime());
                record.put("status", exchange.getStatus());
                record.put("receiverName", exchange.getReceiverName());
                record.put("receiverPhone", exchange.getReceiverPhone());
                record.put("receiverAddress", exchange.getReceiverAddress());
                record.put("shippingCompany", exchange.getShippingCompany());
                record.put("trackingNumber", exchange.getTrackingNumber());
                record.put("shippingTime", exchange.getShippingTime());
                record.put("deliveryTime", exchange.getDeliveryTime());
                record.put("remark", exchange.getRemark());
                record.put("adminRemark", exchange.getAdminRemark());
                record.put("createdTime", exchange.getCreatedTime());
                
                // 获取用户信息
                VolunteerUser user = volunteerUserService.getById(exchange.getUserId());
                if (user != null) {
                    record.put("userName", user.getUsername());
                    record.put("userPhone", user.getPhone());
                }
                
                return record;
            }).collect(Collectors.toList()));
            
            return Result.success(resultPage);
        } catch (Exception e) {
            log.error("获取兑换记录失败", e);
            return Result.error("获取兑换记录失败");
        }
    }

    /**
     * 获取兑换记录详情
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getExchangeDetail(@PathVariable Long id) {
        try {
            PointsExchange exchange = pointsExchangeMapper.selectById(id);
            if (exchange == null) {
                return Result.error("兑换记录不存在");
            }
            
            Map<String, Object> detail = new HashMap<>();
            detail.put("id", exchange.getId());
            detail.put("userId", exchange.getUserId());
            detail.put("goodsId", exchange.getGoodsId());
            detail.put("goodsName", exchange.getGoodsName());
            detail.put("pointsUsed", exchange.getPointsUsed());
            detail.put("exchangeTime", exchange.getExchangeTime());
            detail.put("status", exchange.getStatus());
            detail.put("receiverName", exchange.getReceiverName());
            detail.put("receiverPhone", exchange.getReceiverPhone());
            detail.put("receiverAddress", exchange.getReceiverAddress());
            detail.put("shippingCompany", exchange.getShippingCompany());
            detail.put("trackingNumber", exchange.getTrackingNumber());
            detail.put("shippingTime", exchange.getShippingTime());
            detail.put("deliveryTime", exchange.getDeliveryTime());
            detail.put("remark", exchange.getRemark());
            detail.put("adminRemark", exchange.getAdminRemark());
            detail.put("createdTime", exchange.getCreatedTime());
            
            // 获取用户信息
            VolunteerUser user = volunteerUserService.getById(exchange.getUserId());
            if (user != null) {
                detail.put("userName", user.getUsername());
                detail.put("userPhone", user.getPhone());
            }
            
            return Result.success(detail);
        } catch (Exception e) {
            log.error("获取兑换记录详情失败", e);
            return Result.error("获取兑换记录详情失败");
        }
    }

    /**
     * 更新发货信息
     */
    @PutMapping("/{id}/shipping")
    public Result<String> updateShippingInfo(
            @PathVariable Long id,
            @RequestBody Map<String, Object> shippingInfo) {
        try {
            PointsExchange exchange = pointsExchangeMapper.selectById(id);
            if (exchange == null) {
                return Result.error("兑换记录不存在");
            }
            
            // 更新发货信息
            exchange.setShippingCompany((String) shippingInfo.get("shippingCompany"));
            exchange.setTrackingNumber((String) shippingInfo.get("trackingNumber"));
            exchange.setShippingTime(LocalDateTime.now());
            exchange.setStatus(2); // 已发货
            
            // 更新管理员备注
            if (shippingInfo.get("adminRemark") != null) {
                exchange.setAdminRemark((String) shippingInfo.get("adminRemark"));
            }
            
            boolean updated = pointsExchangeMapper.updateById(exchange) > 0;
            if (updated) {
                log.info("更新发货信息成功: exchangeId={}, trackingNumber={}", id, exchange.getTrackingNumber());
                return Result.success("发货信息更新成功");
            } else {
                return Result.error("发货信息更新失败");
            }
        } catch (Exception e) {
            log.error("更新发货信息失败: exchangeId={}", id, e);
            return Result.error("更新发货信息失败");
        }
    }

    /**
     * 更新订单状态
     */
    @PutMapping("/{id}/status")
    public Result<String> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam Integer status,
            @RequestParam(required = false) String adminRemark) {
        try {
            PointsExchange exchange = pointsExchangeMapper.selectById(id);
            if (exchange == null) {
                return Result.error("兑换记录不存在");
            }
            
            // 状态验证
            if (status < 1 || status > 5) {
                return Result.error("无效的状态值");
            }
            
            // 更新状态
            exchange.setStatus(status);
            
            // 根据状态设置相应时间
            if (status == 4) { // 已签收
                exchange.setDeliveryTime(LocalDateTime.now());
            }
            
            // 更新管理员备注
            if (adminRemark != null && !adminRemark.trim().isEmpty()) {
                exchange.setAdminRemark(adminRemark);
            }
            
            boolean updated = pointsExchangeMapper.updateById(exchange) > 0;
            if (updated) {
                log.info("更新订单状态成功: exchangeId={}, status={}", id, status);
                return Result.success("订单状态更新成功");
            } else {
                return Result.error("订单状态更新失败");
            }
        } catch (Exception e) {
            log.error("更新订单状态失败: exchangeId={}, status={}", id, status, e);
            return Result.error("更新订单状态失败");
        }
    }

    /**
     * 获取状态统计
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStatusStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 统计各状态数量
            for (int status = 1; status <= 5; status++) {
                QueryWrapper<PointsExchange> wrapper = new QueryWrapper<>();
                wrapper.eq("status", status);
                long count = pointsExchangeMapper.selectCount(wrapper);
                stats.put("status" + status, count);
            }
            
            // 总数量
            long total = pointsExchangeMapper.selectCount(null);
            stats.put("total", total);
            
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取状态统计失败", e);
            return Result.error("获取状态统计失败");
        }
    }
}
