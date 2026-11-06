package com.volunteer.controller;

import com.volunteer.entity.VolunteerGoods;
import com.volunteer.entity.VolunteerUser;
import com.volunteer.service.VolunteerGoodsService;
import com.volunteer.service.VolunteerUserService;
import com.volunteer.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 志愿者商品控制器
 * 
 * @author volunteer-team
 * @since 2025-01-22
 */
@Slf4j
@RestController
@RequestMapping("/v1/goods")
public class VolunteerGoodsController {
    
    @Autowired
    private VolunteerGoodsService volunteerGoodsService;
    
    @Autowired
    private VolunteerUserService volunteerUserService;
    
    /**
     * 获取商品列表
     */
    @GetMapping("/list")
    public Result<List<VolunteerGoods>> getGoodsList() {
        try {
            List<VolunteerGoods> goodsList = volunteerGoodsService.getAvailableGoods();
            log.info("获取到商品列表，数量：{}", goodsList.size());
            
            // 处理列表中的图片数据
            for (VolunteerGoods goods : goodsList) {
                processGoodsImagesForList(goods);
            }
            
            return Result.success(goodsList);
        } catch (Exception e) {
            log.error("获取商品列表失败", e);
            return Result.error("获取商品列表失败");
        }
    }
    
    /**
     * 处理商品列表中的图片数据
     */
    private void processGoodsImagesForList(VolunteerGoods goods) {
        if (goods.getImageUrl() != null && !goods.getImageUrl().isEmpty()) {
            try {
                // 尝试解析JSON格式的图片数据
                com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                java.util.List<String> images = objectMapper.readValue(goods.getImageUrl(), 
                    objectMapper.getTypeFactory().constructCollectionType(java.util.List.class, String.class));
                goods.setImages(images);
            } catch (Exception e) {
                // 如果JSON解析失败，说明是单个图片URL，转换为数组
                goods.setImages(java.util.Arrays.asList(goods.getImageUrl()));
            }
        }
    }
    
    /**
     * 兑换商品（带收货信息）
     */
    @PostMapping("/exchange")
    public Result<String> exchangeGoods(@RequestBody Map<String, Object> exchangeData, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            // 获取商品ID
            Long goodsId = Long.valueOf(exchangeData.get("goodsId").toString());
            
            // 获取收货信息
            String receiverName = (String) exchangeData.get("receiverName");
            String receiverPhone = (String) exchangeData.get("receiverPhone");
            String receiverAddress = (String) exchangeData.get("receiverAddress");
            
            // 验证收货信息
            if (receiverName == null || receiverName.trim().isEmpty()) {
                return Result.error("收货人姓名不能为空");
            }
            if (receiverPhone == null || receiverPhone.trim().isEmpty()) {
                return Result.error("收货人电话不能为空");
            }
            if (receiverAddress == null || receiverAddress.trim().isEmpty()) {
                return Result.error("收货地址不能为空");
            }
            
            // 验证手机号格式
            if (!receiverPhone.matches("^1[3-9]\\d{9}$")) {
                return Result.error("请输入正确的手机号码");
            }
            
            boolean success = volunteerGoodsService.exchangeGoodsWithReceiverInfo(
                userId, goodsId, receiverName, receiverPhone, receiverAddress);
            
            if (success) {
                return Result.success("兑换成功");
            } else {
                return Result.error("兑换失败，请检查积分是否充足或商品是否可兑换");
            }
        } catch (Exception e) {
            log.error("兑换商品失败", e);
            return Result.error("兑换失败");
        }
    }
    
    /**
     * 获取用户兑换记录
     */
    @GetMapping("/user/exchange-records")
    public Result<List<Object>> getUserExchangeRecords(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            List<Object> records = volunteerGoodsService.getUserExchangeRecords(userId);
            return Result.success(records);
        } catch (Exception e) {
            log.error("获取兑换记录失败", e);
            return Result.error("获取兑换记录失败");
        }
    }
    
    /**
     * 获取用户总积分
     */
    @GetMapping("/user/total-points")
    public Result<Integer> getUserTotalPoints(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            // 直接从志愿者表获取总积分
            VolunteerUser volunteer = volunteerUserService.getById(userId);
            if (volunteer != null && volunteer.getTotalPoints() != null) {
                return Result.success(volunteer.getTotalPoints());
            }
            return Result.success(0);
        } catch (Exception e) {
            log.error("获取用户总积分失败", e);
            return Result.error("获取用户总积分失败");
        }
    }
    
    /**
     * 用户确认收货
     */
    @PutMapping("/user/exchange-records/{id}/confirm-receipt")
    public Result<String> confirmReceipt(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            log.info("用户确认收货请求: exchangeId={}, userId={}", id, userId);
            
            boolean success = volunteerGoodsService.confirmReceipt(id, userId);
            if (success) {
                return Result.success("确认收货成功");
            } else {
                return Result.error("确认收货失败，请检查订单状态或权限");
            }
        } catch (Exception e) {
            log.error("确认收货失败: exchangeId={}", id, e);
            return Result.error("确认收货失败: " + e.getMessage());
        }
    }
    
    /**
     * 调试接口：获取兑换记录详情
     */
    @GetMapping("/user/exchange-records/{id}/debug")
    public Result<Map<String, Object>> debugExchangeRecord(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            Map<String, Object> debugInfo = volunteerGoodsService.getExchangeRecordDebugInfo(id, userId);
            return Result.success(debugInfo);
        } catch (Exception e) {
            log.error("获取兑换记录调试信息失败: exchangeId={}", id, e);
            return Result.error("获取调试信息失败: " + e.getMessage());
        }
    }
}
