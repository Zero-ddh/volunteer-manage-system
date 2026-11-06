package com.volunteer.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.volunteer.entity.VolunteerGoods;
import com.volunteer.service.VolunteerGoodsService;
import com.volunteer.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理后台商品管理控制器
 * 
 * @author volunteer-team
 * @since 2025-01-23
 */
@Slf4j
@RestController
@RequestMapping("/v1/admin/goods")
public class AdminGoodsController {
    
    @Autowired
    private VolunteerGoodsService volunteerGoodsService;
    
    /**
     * 分页查询商品列表
     */
    @GetMapping
    public Result<IPage<VolunteerGoods>> getGoodsList(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status) {
        try {
            IPage<VolunteerGoods> result = volunteerGoodsService.adminPageList(pageNo, pageSize, keyword, category, status);
            
            // 处理列表中的图片数据
            for (VolunteerGoods goods : result.getRecords()) {
                processGoodsImagesForList(goods);
            }
            
            return Result.success(result);
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
     * 获取商品详情
     */
    @GetMapping("/{id}")
    public Result<VolunteerGoods> getGoodsDetail(@PathVariable Long id) {
        try {
            VolunteerGoods goods = volunteerGoodsService.getById(id);
            if (goods == null) {
                return Result.error("商品不存在");
            }
            
            // 处理图片数据，将imageUrl转换为images数组
            if (goods.getImageUrl() != null && !goods.getImageUrl().isEmpty()) {
                try {
                    // 尝试解析JSON格式的图片数据
                    com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    java.util.List<String> images = objectMapper.readValue(goods.getImageUrl(), 
                        objectMapper.getTypeFactory().constructCollectionType(java.util.List.class, String.class));
                    goods.setImages(images);
                    log.info("解析商品图片JSON: goodsId={}, images={}", goods.getId(), images);
                } catch (Exception e) {
                    // 如果JSON解析失败，说明是单个图片URL，转换为数组
                    log.info("图片数据不是JSON格式，使用单个URL: goodsId={}, imageUrl={}", goods.getId(), goods.getImageUrl());
                    goods.setImages(java.util.Arrays.asList(goods.getImageUrl()));
                }
            }
            
            return Result.success(goods);
        } catch (Exception e) {
            log.error("获取商品详情失败", e);
            return Result.error("获取商品详情失败");
        }
    }
    
    /**
     * 创建商品
     */
    @PostMapping
    public Result<Boolean> createGoods(@RequestBody VolunteerGoods goods) {
        try {
            boolean result = volunteerGoodsService.save(goods);
            if (result) {
                log.info("创建商品成功: {}", goods.getName());
            }
            return Result.success(result);
        } catch (Exception e) {
            log.error("创建商品失败", e);
            return Result.error("创建商品失败");
        }
    }
    
    /**
     * 更新商品
     */
    @PutMapping("/{id}")
    public Result<Boolean> updateGoods(@PathVariable Long id, @RequestBody VolunteerGoods goods) {
        try {
            goods.setId(id);
            boolean result = volunteerGoodsService.updateById(goods);
            if (result) {
                log.info("更新商品成功: {}", goods.getName());
            }
            return Result.success(result);
        } catch (Exception e) {
            log.error("更新商品失败", e);
            return Result.error("更新商品失败");
        }
    }
    
    /**
     * 删除商品
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteGoods(@PathVariable Long id) {
        try {
            // 检查商品是否存在
            VolunteerGoods goods = volunteerGoodsService.getById(id);
            if (goods == null) {
                return Result.error("商品不存在");
            }
            
            // 检查商品是否存在兑换记录
            boolean hasExchangeRecords = volunteerGoodsService.hasExchangeRecords(id);
            if (hasExchangeRecords) {
                log.warn("删除商品失败，存在兑换记录: goodsId={}, goodsName={}", id, goods.getName());
                return Result.error("该商品存在兑换记录，无法删除");
            }
            
            boolean result = volunteerGoodsService.removeById(id);
            if (result) {
                log.info("删除商品成功: id={}, name={}", id, goods.getName());
            }
            return Result.success(result);
        } catch (Exception e) {
            log.error("删除商品失败", e);
            return Result.error("删除商品失败");
        }
    }
    
    /**
     * 更新商品状态
     */
    @PutMapping("/{id}/status")
    public Result<Boolean> updateGoodsStatus(@PathVariable Long id, @RequestParam Integer status) {
        try {
            VolunteerGoods goods = new VolunteerGoods();
            goods.setId(id);
            goods.setStatus(status);
            boolean result = volunteerGoodsService.updateById(goods);
            if (result) {
                log.info("更新商品状态成功: id={}, status={}", id, status);
            }
            return Result.success(result);
        } catch (Exception e) {
            log.error("更新商品状态失败", e);
            return Result.error("更新商品状态失败");
        }
    }
    
    /**
     * 获取商品分类列表
     */
    @GetMapping("/categories")
    public Result<List<String>> getGoodsCategories() {
        try {
            List<String> categories = volunteerGoodsService.getGoodsCategories();
            return Result.success(categories);
        } catch (Exception e) {
            log.error("获取商品分类失败", e);
            return Result.error("获取商品分类失败");
        }
    }
}
