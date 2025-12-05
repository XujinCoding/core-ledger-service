package com.coreledger.controller;

import com.coreledger.common.Result;
import com.coreledger.dto.product.ProductAttrBatchUpdateDTO;
import com.coreledger.dto.product.ProductAttrCreateDTO;
import com.coreledger.dto.product.ProductAttrValueCreateDTO;
import com.coreledger.dto.product.ProductCreateDTO;
import com.coreledger.dto.product.ProductUpdateDTO;
import com.coreledger.enums.Status;
import com.coreledger.service.ProductAttrService;
import com.coreledger.service.ProductService;
import com.coreledger.vo.product.ProductAttrVO;
import com.coreledger.vo.product.ProductAttrValueVO;
import com.coreledger.vo.product.ProductVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品管理Controller
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "商品管理", description = "商品及SKU的增删改查")
public class ProductController {

    private final ProductService productService;

    /**
     * 创建商品
     */
    @Operation(summary = "创建商品", description = "创建新商品")
    @PostMapping
    public Result<ProductVO> createProduct(@Valid @RequestBody ProductCreateDTO dto) {
        return Result.success(productService.createProduct(dto));
    }

    /**
     * 修改商品
     */
    @Operation(summary = "修改商品", description = "修改商品基本信息")
    @PutMapping("/{id}")
    public Result<ProductVO> updateProduct(@PathVariable Long id,
                                          @Valid @RequestBody ProductUpdateDTO dto) {
        return Result.success(productService.updateProduct(id, dto));
    }

    /**
     * 删除商品
     */
    @Operation(summary = "删除商品", description = "删除商品及其属性和SKU")
    @DeleteMapping("/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return Result.success("删除成功");
    }

    /**
     * 获取商品详情
     */
    @Operation(summary = "获取商品详情", description = "获取商品详情（含属性和SKU）")
    @GetMapping("/{id}")
    public Result<ProductVO> getProduct(@PathVariable Long id) {
        return Result.success(productService.getProduct(id));
    }

    /**
     * 获取商品列表
     */
    @Operation(summary = "获取商品列表", description = "获取商品列表（支持分类筛选、关键词搜索）")
    @GetMapping
    public Result<Page<ProductVO>> listProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return Result.success(productService.listProducts(categoryId, keyword, pageable));
    }

    // ==================== 商品属性管理 ====================



}
