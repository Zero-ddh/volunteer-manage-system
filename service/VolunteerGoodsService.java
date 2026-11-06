package com.volunteer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.volunteer.entity.VolunteerGoods;

import java.util.List;
import java.util.Map;

/**
 * 志愿者商品服务接口
 * 
 * @author volunteer-team
 * @since 2025-01-22
 */
public interface VolunteerGoodsService extends IService<VolunteerGoods> {
    
    /**
     * 获取上架商品列表
     * @return 商品列表
     */
    List<VolunteerGoods> getAvailableGoods();
    
    /**
     * 兑换商品
     * @param userId 用户ID
     * @param goodsId 商品ID
     * @return 兑换结果
     */
    boolean exchangeGoods(Long userId, Long goodsId);
    
    /**
     * 兑换商品（带收货信息）
     * @param userId 用户ID
     * @param goodsId 商品ID
     * @param receiverName 收货人姓名
     * @param receiverPhone 收货人电话
     * @param receiverAddress 收货地址
     * @return 兑换结果
     */
    boolean exchangeGoodsWithReceiverInfo(Long userId, Long goodsId, String receiverName, String receiverPhone, String receiverAddress);
    
    /**
     * 获取用户兑换记录
     * @param userId 用户ID
     * @return 兑换记录列表
     */
    List<Object> getUserExchangeRecords(Long userId);
    
    /**
     * 管理后台分页查询商品列表
     * @param pageNo 页码
     * @param pageSize 每页大小
     * @param keyword 关键词
     * @param category 分类
     * @param status 状态
     * @return 分页结果
     */
    IPage<VolunteerGoods> adminPageList(int pageNo, int pageSize, String keyword, String category, Integer status);
    
    /**
     * 获取商品分类列表
     * @return 分类列表
     */
    List<String> getGoodsCategories();
    
    /**
     * 用户确认收货
     * @param exchangeId 兑换记录ID
     * @param userId 用户ID
     * @return 确认结果
     */
    boolean confirmReceipt(Long exchangeId, Long userId);
    
    /**
     * 获取兑换记录调试信息
     * @param exchangeId 兑换记录ID
     * @param userId 用户ID
     * @return 调试信息
     */
    Map<String, Object> getExchangeRecordDebugInfo(Long exchangeId, Long userId);
    
    /**
     * 检查商品是否存在兑换记录
     * @param goodsId 商品ID
     * @return 是否存在兑换记录
     */
    boolean hasExchangeRecords(Long goodsId);
}
