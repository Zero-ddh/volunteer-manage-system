package com.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.InsuranceProduct;
import com.volunteer.mapper.InsuranceProductMapper;
import com.volunteer.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理后台保险产品控制器
 * 
 * @author volunteer-team
 * @since 2025-01-24
 */
@Slf4j
@RestController
@RequestMapping("/admin/insurance-products")
public class AdminInsuranceProductController {
    
    @Autowired
    private InsuranceProductMapper insuranceProductMapper;

    /**
     * 分页查询保险产品列表
     */
    @GetMapping("/list")
    public Result<Page<InsuranceProduct>> getProductList(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        try {
            Page<InsuranceProduct> page = new Page<>(pageNo, pageSize);
            QueryWrapper<InsuranceProduct> queryWrapper = new QueryWrapper<>();
            
            // 关键词搜索（产品名称、描述）
            if (keyword != null && !keyword.trim().isEmpty()) {
                queryWrapper.and(wrapper -> wrapper
                    .like("name", keyword)
                    .or().like("description", keyword)
                );
            }
            
            // 状态筛选
            if (status != null) {
                queryWrapper.eq("status", status);
            }
            
            // 排除已删除的记录
            queryWrapper.eq("deleted", 0);
            
            // 按创建时间倒序排列
            queryWrapper.orderByDesc("created_time");
            
            Page<InsuranceProduct> productPage = insuranceProductMapper.selectPage(page, queryWrapper);
            
            log.info("查询保险产品列表成功: pageNo={}, pageSize={}, keyword={}, status={}", 
                pageNo, pageSize, keyword, status);
            
            return Result.success(productPage);
        } catch (Exception e) {
            log.error("查询保险产品列表失败", e);
            return Result.error("查询保险产品列表失败");
        }
    }

    /**
     * 创建保险产品
     */
    @PostMapping
    public Result<InsuranceProduct> createProduct(@RequestBody InsuranceProduct product) {
        try {
            // 设置默认值
            product.setStatus(1); // 默认上架
            product.setCreatedTime(LocalDateTime.now());
            product.setUpdatedTime(LocalDateTime.now());
            product.setDeleted(0);
            
            int result = insuranceProductMapper.insert(product);
            if (result > 0) {
                log.info("创建保险产品成功: productId={}, name={}", product.getId(), product.getName());
                return Result.success(product);
            } else {
                return Result.error("创建保险产品失败");
            }
        } catch (Exception e) {
            log.error("创建保险产品失败", e);
            return Result.error("创建保险产品失败");
        }
    }

    /**
     * 更新保险产品
     */
    @PutMapping("/{id}")
    public Result<InsuranceProduct> updateProduct(@PathVariable Long id, @RequestBody InsuranceProduct product) {
        try {
            // 检查产品是否存在
            InsuranceProduct existingProduct = insuranceProductMapper.selectById(id);
            if (existingProduct == null || existingProduct.getDeleted() == 1) {
                return Result.error("保险产品不存在");
            }
            
            // 设置更新信息
            product.setId(id);
            product.setUpdatedTime(LocalDateTime.now());
            
            int result = insuranceProductMapper.updateById(product);
            if (result > 0) {
                log.info("更新保险产品成功: productId={}, name={}", id, product.getName());
                return Result.success(product);
            } else {
                return Result.error("更新保险产品失败");
            }
        } catch (Exception e) {
            log.error("更新保险产品失败: productId={}", id, e);
            return Result.error("更新保险产品失败");
        }
    }

    /**
     * 删除保险产品（逻辑删除）
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteProduct(@PathVariable Long id) {
        try {
            // 检查产品是否存在
            InsuranceProduct product = insuranceProductMapper.selectById(id);
            if (product == null || product.getDeleted() == 1) {
                return Result.error("保险产品不存在");
            }
            
            // 使用MyBatis-Plus的逻辑删除
            int result = insuranceProductMapper.deleteById(id);
            if (result > 0) {
                log.info("删除保险产品成功: productId={}, name={}", id, product.getName());
                return Result.success("删除保险产品成功");
            } else {
                return Result.error("删除保险产品失败");
            }
        } catch (Exception e) {
            log.error("删除保险产品失败: productId={}", id, e);
            return Result.error("删除保险产品失败");
        }
    }

    /**
     * 上架/下架产品
     */
    @PutMapping("/{id}/status")
    public Result<String> updateProductStatus(@PathVariable Long id, @RequestParam Integer status) {
        try {
            // 检查产品是否存在
            InsuranceProduct product = insuranceProductMapper.selectById(id);
            if (product == null || product.getDeleted() == 1) {
                return Result.error("保险产品不存在");
            }
            
            // 状态验证
            if (status != 0 && status != 1) {
                return Result.error("无效的状态值");
            }
            
            // 更新状态
            product.setStatus(status);
            product.setUpdatedTime(LocalDateTime.now());
            
            int result = insuranceProductMapper.updateById(product);
            if (result > 0) {
                String statusText = status == 1 ? "上架" : "下架";
                log.info("更新保险产品状态成功: productId={}, status={}", id, statusText);
                return Result.success("产品" + statusText + "成功");
            } else {
                return Result.error("更新产品状态失败");
            }
        } catch (Exception e) {
            log.error("更新保险产品状态失败: productId={}, status={}", id, status, e);
            return Result.error("更新产品状态失败");
        }
    }

    /**
     * 获取产品详情
     */
    @GetMapping("/{id}")
    public Result<InsuranceProduct> getProductDetail(@PathVariable Long id) {
        try {
            InsuranceProduct product = insuranceProductMapper.selectById(id);
            if (product == null || product.getDeleted() == 1) {
                return Result.error("保险产品不存在");
            }
            
            log.info("获取保险产品详情成功: productId={}", id);
            return Result.success(product);
        } catch (Exception e) {
            log.error("获取保险产品详情失败: productId={}", id, e);
            return Result.error("获取保险产品详情失败");
        }
    }

    /**
     * 获取所有产品列表（用于下拉选择）
     */
    @GetMapping("/all")
    public Result<List<InsuranceProduct>> getAllProducts() {
        try {
            QueryWrapper<InsuranceProduct> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("deleted", 0);
            queryWrapper.orderByDesc("created_time");
            
            List<InsuranceProduct> products = insuranceProductMapper.selectList(queryWrapper);
            
            log.info("获取所有保险产品成功: count={}", products.size());
            return Result.success(products);
        } catch (Exception e) {
            log.error("获取所有保险产品失败", e);
            return Result.error("获取所有保险产品失败");
        }
    }
}
