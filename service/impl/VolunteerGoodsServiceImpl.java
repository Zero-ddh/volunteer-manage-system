package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.volunteer.entity.PointsExchange;
import com.volunteer.entity.VolunteerGoods;
import com.volunteer.entity.VolunteerUser;
import com.volunteer.mapper.PointsExchangeMapper;
import com.volunteer.mapper.VolunteerGoodsMapper;
import com.volunteer.service.VolunteerGoodsService;
import com.volunteer.service.VolunteerUserService;
import com.volunteer.service.UserPointsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 志愿者商品服务实现类
 * 
 * @author volunteer-team
 * @since 2025-01-22
 */
@Slf4j
@Service
public class VolunteerGoodsServiceImpl extends ServiceImpl<VolunteerGoodsMapper, VolunteerGoods> implements VolunteerGoodsService {
    
    @Autowired
    private PointsExchangeMapper pointsExchangeMapper;
    
    @Autowired
    private VolunteerUserService volunteerUserService;
    
    @Autowired
    private UserPointsService userPointsService;
    
    @Override
    public List<VolunteerGoods> getAvailableGoods() {
        LambdaQueryWrapper<VolunteerGoods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VolunteerGoods::getStatus, 1) // 上架状态
               .gt(VolunteerGoods::getStock, 0)  // 有库存
               .orderByAsc(VolunteerGoods::getPointsRequired);
        
        return list(wrapper);
    }
    
    @Override
    @Transactional
    public boolean exchangeGoods(Long userId, Long goodsId) {
        try {
            // 1. 获取商品信息
            VolunteerGoods goods = getById(goodsId);
            if (goods == null || goods.getStatus() != 1) {
                log.warn("商品不存在或已下架: goodsId={}", goodsId);
                return false;
            }
            
            if (goods.getStock() <= 0) {
                log.warn("商品库存不足: goodsId={}, stock={}", goodsId, goods.getStock());
                return false;
            }
            
            // 2. 获取用户当前积分
            int userPoints = getUserTotalPoints(userId);
            if (userPoints < goods.getPointsRequired()) {
                log.warn("用户积分不足: userId={}, userPoints={}, required={}", 
                    userId, userPoints, goods.getPointsRequired());
                return false;
            }
            
            // 3. 创建兑换记录
            PointsExchange exchange = new PointsExchange();
            exchange.setUserId(userId);
            exchange.setGoodsId(goodsId);
            exchange.setGoodsName(goods.getName());
            exchange.setPointsUsed(goods.getPointsRequired());
            exchange.setExchangeTime(LocalDateTime.now());
            exchange.setStatus(1); // 兑换成功
            exchange.setRemark("积分兑换商品");
            
            boolean saved = pointsExchangeMapper.insert(exchange) > 0;
            
            // 4. 减少商品库存并扣除用户积分
            if (saved) {
                goods.setStock(goods.getStock() - 1);
                updateById(goods);
                
                // 扣除用户积分 - 添加消费积分记录
                boolean pointsDeducted = userPointsService.addPointsRecord(
                    userId, 
                    goods.getPointsRequired(), 
                    2, // 消费积分
                    "商品兑换", 
                    "兑换商品：" + goods.getName()
                );
                
                if (!pointsDeducted) {
                    log.error("扣除用户积分失败: userId={}, points={}", userId, goods.getPointsRequired());
                    throw new RuntimeException("扣除用户积分失败");
                }
                
                log.info("商品兑换成功: userId={}, goodsId={}, pointsUsed={}", 
                    userId, goodsId, goods.getPointsRequired());
            }
            
            return saved;
            
        } catch (Exception e) {
            log.error("商品兑换失败: userId={}, goodsId={}", userId, goodsId, e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean exchangeGoodsWithReceiverInfo(Long userId, Long goodsId, String receiverName, String receiverPhone, String receiverAddress) {
        try {
            // 1. 获取商品信息
            VolunteerGoods goods = getById(goodsId);
            if (goods == null || goods.getStatus() != 1) {
                log.warn("商品不存在或已下架: goodsId={}", goodsId);
                return false;
            }
            
            if (goods.getStock() <= 0) {
                log.warn("商品库存不足: goodsId={}, stock={}", goodsId, goods.getStock());
                return false;
            }
            
            // 2. 获取用户当前积分
            int userPoints = getUserTotalPoints(userId);
            if (userPoints < goods.getPointsRequired()) {
                log.warn("用户积分不足: userId={}, userPoints={}, required={}", 
                    userId, userPoints, goods.getPointsRequired());
                return false;
            }
            
            // 3. 创建兑换记录（包含收货信息）
            PointsExchange exchange = new PointsExchange();
            exchange.setUserId(userId);
            exchange.setGoodsId(goodsId);
            exchange.setGoodsName(goods.getName());
            exchange.setPointsUsed(goods.getPointsRequired());
            exchange.setExchangeTime(LocalDateTime.now());
            exchange.setStatus(1); // 待发货
            exchange.setRemark("积分兑换商品");
            
            // 设置收货信息
            exchange.setReceiverName(receiverName);
            exchange.setReceiverPhone(receiverPhone);
            exchange.setReceiverAddress(receiverAddress);
            
            boolean saved = pointsExchangeMapper.insert(exchange) > 0;
            
            // 4. 减少商品库存并扣除用户积分
            if (saved) {
                goods.setStock(goods.getStock() - 1);
                updateById(goods);
                
                // 扣除用户积分 - 添加消费积分记录
                boolean pointsDeducted = userPointsService.addPointsRecord(
                    userId, 
                    goods.getPointsRequired(), 
                    2, // 消费积分
                    "商品兑换", 
                    "兑换商品：" + goods.getName()
                );
                
                if (!pointsDeducted) {
                    log.error("扣除用户积分失败: userId={}, points={}", userId, goods.getPointsRequired());
                    throw new RuntimeException("扣除用户积分失败");
                }
                
                log.info("商品兑换成功: userId={}, goodsId={}, pointsUsed={}, receiverName={}", 
                    userId, goodsId, goods.getPointsRequired(), receiverName);
            }
            
            return saved;
            
        } catch (Exception e) {
            log.error("商品兑换失败: userId={}, goodsId={}", userId, goodsId, e);
            return false;
        }
    }
    
    @Override
    public List<Object> getUserExchangeRecords(Long userId) {
        LambdaQueryWrapper<PointsExchange> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsExchange::getUserId, userId)
               .orderByDesc(PointsExchange::getExchangeTime);
        
        List<PointsExchange> exchanges = pointsExchangeMapper.selectList(wrapper);
        
        return exchanges.stream().map(exchange -> {
            Map<String, Object> record = new HashMap<>();
            record.put("id", exchange.getId());
            record.put("goodsName", exchange.getGoodsName());
            record.put("pointsUsed", exchange.getPointsUsed());
            record.put("exchangeTime", exchange.getExchangeTime());
            record.put("status", exchange.getStatus());
            record.put("remark", exchange.getRemark());
            record.put("receiverName", exchange.getReceiverName());
            record.put("receiverPhone", exchange.getReceiverPhone());
            record.put("receiverAddress", exchange.getReceiverAddress());
            record.put("shippingCompany", exchange.getShippingCompany());
            record.put("trackingNumber", exchange.getTrackingNumber());
            record.put("shippingTime", exchange.getShippingTime());
            record.put("deliveryTime", exchange.getDeliveryTime());
            return record;
        }).collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 获取用户总积分
     * @param userId 用户ID
     * @return 总积分
     */
    private int getUserTotalPoints(Long userId) {
        try {
            // 从志愿者表获取总积分
            VolunteerUser volunteer = volunteerUserService.getById(userId);
            if (volunteer != null && volunteer.getTotalPoints() != null) {
                return volunteer.getTotalPoints();
            }
            return 0;
        } catch (Exception e) {
            log.error("获取用户总积分失败: userId={}", userId, e);
            return 0;
        }
    }
    
    @Override
    public IPage<VolunteerGoods> adminPageList(int pageNo, int pageSize, String keyword, String category, Integer status) {
        Page<VolunteerGoods> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<VolunteerGoods> wrapper = new LambdaQueryWrapper<>();
        
        // 关键词搜索
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(VolunteerGoods::getName, keyword)
                             .or()
                             .like(VolunteerGoods::getDescription, keyword));
        }
        
        // 分类筛选
        if (StringUtils.hasText(category)) {
            wrapper.eq(VolunteerGoods::getCategory, category);
        }
        
        // 状态筛选
        if (status != null) {
            wrapper.eq(VolunteerGoods::getStatus, status);
        }
        
        wrapper.orderByDesc(VolunteerGoods::getId);
        
        return page(page, wrapper);
    }
    
    @Override
    public List<String> getGoodsCategories() {
        LambdaQueryWrapper<VolunteerGoods> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(VolunteerGoods::getCategory)
               .groupBy(VolunteerGoods::getCategory)
               .orderByAsc(VolunteerGoods::getCategory);
        
        List<VolunteerGoods> goodsList = list(wrapper);
        return goodsList.stream()
                       .map(VolunteerGoods::getCategory)
                       .filter(Objects::nonNull)
                       .distinct()
                       .collect(Collectors.toList());
    }
    
    /**
     * 处理商品图片数据
     * 将前端传递的images数组转换为JSON字符串存储到imageUrl字段
     * @param goods 商品对象
     */
    private void processGoodsImages(VolunteerGoods goods) {
        if (goods.getImages() != null && !goods.getImages().isEmpty()) {
            try {
                // 将图片数组转换为JSON字符串存储到imageUrl字段
                com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                String imageUrlJson = objectMapper.writeValueAsString(goods.getImages());
                goods.setImageUrl(imageUrlJson);
                log.info("处理商品图片: goodsId={}, images={}, imageUrl={}", goods.getId(), goods.getImages(), imageUrlJson);
            } catch (Exception e) {
                log.error("处理商品图片失败", e);
                // 如果JSON转换失败，使用第一张图片作为备用方案
                String imageUrl = goods.getImages().get(0);
                goods.setImageUrl(imageUrl);
            }
        }
    }
    
    /**
     * 重写save方法，处理图片数据
     */
    @Override
    public boolean save(VolunteerGoods goods) {
        processGoodsImages(goods);
        return super.save(goods);
    }
    
    /**
     * 重写updateById方法，处理图片数据
     */
    @Override
    public boolean updateById(VolunteerGoods goods) {
        processGoodsImages(goods);
        return super.updateById(goods);
    }
    
    @Override
    @Transactional
    public boolean confirmReceipt(Long exchangeId, Long userId) {
        try {
            log.info("开始确认收货: exchangeId={}, userId={}", exchangeId, userId);
            
            // 1. 查询兑换记录
            PointsExchange exchange = pointsExchangeMapper.selectById(exchangeId);
            if (exchange == null) {
                log.warn("兑换记录不存在: exchangeId={}", exchangeId);
                return false;
            }
            
            log.info("找到兑换记录: exchangeId={}, userId={}, status={}, exchangeUserId={}", 
                exchangeId, userId, exchange.getStatus(), exchange.getUserId());
            
            // 2. 验证用户权限
            if (!exchange.getUserId().equals(userId)) {
                log.warn("用户无权限操作此兑换记录: userId={}, exchangeUserId={}", userId, exchange.getUserId());
                return false;
            }
            
            // 3. 验证状态（已发货和运输中状态都可以确认收货）
            if (exchange.getStatus() != 2 && exchange.getStatus() != 3) {
                log.warn("订单状态不允许确认收货: exchangeId={}, status={}", exchangeId, exchange.getStatus());
                return false;
            }
            
            // 4. 更新状态为已签收
            exchange.setStatus(4); // 已签收
            exchange.setDeliveryTime(LocalDateTime.now());
            
            boolean updated = pointsExchangeMapper.updateById(exchange) > 0;
            if (updated) {
                log.info("用户确认收货成功: exchangeId={}, userId={}", exchangeId, userId);
                return true;
            } else {
                log.warn("更新兑换记录状态失败: exchangeId={}", exchangeId);
                return false;
            }
        } catch (Exception e) {
            log.error("确认收货失败: exchangeId={}, userId={}", exchangeId, userId, e);
            return false;
        }
    }
    
    @Override
    public Map<String, Object> getExchangeRecordDebugInfo(Long exchangeId, Long userId) {
        Map<String, Object> debugInfo = new HashMap<>();
        
        try {
            // 查询兑换记录
            PointsExchange exchange = pointsExchangeMapper.selectById(exchangeId);
            
            debugInfo.put("exchangeId", exchangeId);
            debugInfo.put("currentUserId", userId);
            debugInfo.put("exchangeExists", exchange != null);
            
            if (exchange != null) {
                debugInfo.put("exchangeUserId", exchange.getUserId());
                debugInfo.put("exchangeStatus", exchange.getStatus());
                debugInfo.put("exchangeGoodsName", exchange.getGoodsName());
                debugInfo.put("exchangeTime", exchange.getExchangeTime());
                debugInfo.put("receiverName", exchange.getReceiverName());
                debugInfo.put("trackingNumber", exchange.getTrackingNumber());
                
                // 权限检查
                boolean hasPermission = exchange.getUserId().equals(userId);
                debugInfo.put("hasPermission", hasPermission);
                
                // 状态检查
                boolean canConfirm = (exchange.getStatus() == 2 || exchange.getStatus() == 3);
                debugInfo.put("canConfirmReceipt", canConfirm);
                
                // 状态说明
                String statusText = getStatusText(exchange.getStatus());
                debugInfo.put("statusText", statusText);
            }
            
        } catch (Exception e) {
            debugInfo.put("error", e.getMessage());
            log.error("获取调试信息失败: exchangeId={}, userId={}", exchangeId, userId, e);
        }
        
        return debugInfo;
    }
    
    private String getStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 1: return "待发货";
            case 2: return "已发货";
            case 3: return "运输中";
            case 4: return "已签收";
            case 5: return "已完成";
            default: return "未知状态(" + status + ")";
        }
    }
    
    @Override
    public boolean hasExchangeRecords(Long goodsId) {
        try {
            LambdaQueryWrapper<PointsExchange> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(PointsExchange::getGoodsId, goodsId);
            
            Long count = pointsExchangeMapper.selectCount(wrapper);
            boolean hasRecords = count != null && count > 0;
            
            log.info("检查商品兑换记录: goodsId={}, hasRecords={}, count={}", goodsId, hasRecords, count);
            return hasRecords;
        } catch (Exception e) {
            log.error("检查商品兑换记录失败: goodsId={}", goodsId, e);
            return false;
        }
    }
}
