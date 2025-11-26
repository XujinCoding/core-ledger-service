package com.coreledger.controller;

import com.coreledger.common.Result;
import com.coreledger.dto.product.CreateProductDTO;
import com.coreledger.dto.product.GenerateSkusDTO;
import com.coreledger.dto.product.SetProductAttributesDTO;
import com.coreledger.dto.product.UpdateProductDTO;
import com.coreledger.service.ProductService;
import com.coreledger.vo.product.ProductDetailVO;
import com.coreledger.vo.product.ProductListVO;
import com.coreledger.vo.product.SkuListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

/**
 * 商品控制器 (SPU)
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Tag(name = "商品管理", description = "商品(SPU)、属性、SKU的创建、查询、修改、删除等接口")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 创建商品 (SPU)
     */
    @Operation(summary = "创建商品", description = "创建新商品(SPU)，不包含属性和SKU")
    @PostMapping
    public Result<ProductDetailVO> createProduct(@Valid @RequestBody CreateProductDTO dto) {
        ProductDetailVO product = productService.createProduct(dto);
        return Result.success(product);
    }

    /**
     * 设置商品属性
     */
    @Operation(summary = "设置商品属性", description = "为商品设置动态属性（如：重量、等级等），会覆盖原有属性")
    @PostMapping("/{id}/attributes")
    public Result<Void> setProductAttributes(
            @Parameter(description = "商品ID", required = true)
            @PathVariable Long id,

            @Valid @RequestBody SetProductAttributesDTO dto
    ) {
        productService.setProductAttributes(id, dto);
        return Result.success("属性设置成功");
    }

    /**
     * 生成SKU
     */
    @Operation(summary = "生成SKU", description = "根据属性生成SKU，支持统一价格或手动指定")
    @PostMapping("/{id}/skus")
    public Result<Void> generateSkus(
            @Parameter(description = "商品ID", required = true)
            @PathVariable Long id,

            @Valid @RequestBody GenerateSkusDTO dto
    ) {
        productService.generateSkus(id, dto);
        return Result.success("SKU生成成功");
    }

    /**
     * 查询商品列表
     */
    @Operation(summary = "查询商品列表", description = "支持关键词搜索（名称）、分类筛选，含SKU数量统计")
    @GetMapping
    public Result<Page<ProductListVO>> listProducts(
            @Parameter(description = "关键词（名称模糊查询）")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "分类ID筛选")
            @RequestParam(required = false) Long categoryId,

            @PageableDefault(size = 20, sort = "createInstant", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ProductListVO> products = productService.listProducts(keyword, categoryId, pageable);
        return Result.success(products);
    }

    /**
     * 查询商品详情
     */
    @Operation(summary = "查询商品详情", description = "查询商品基本信息、属性列表、SKU列表")
    @GetMapping("/{id}")
    public Result<ProductDetailVO> getProductDetail(
            @Parameter(description = "商品ID", required = true)
            @PathVariable Long id
    ) {
        ProductDetailVO product = productService.getProductDetail(id);
        return Result.success(product);
    }

    /**
     * 更新商品信息
     */
    @Operation(summary = "更新商品信息", description = "更新商品基本信息，只更新提供的字段")
    @PutMapping("/{id}")
    public Result<ProductDetailVO> updateProduct(
            @Parameter(description = "商品ID", required = true)
            @PathVariable Long id,

            @Valid @RequestBody UpdateProductDTO dto
    ) {
        ProductDetailVO product = productService.updateProduct(id, dto);
        return Result.success(product);
    }

    /**
     * 删除商品
     */
    @Operation(summary = "删除商品", description = "软删除商品及其所有SKU，有未结清账单时禁止删除")
    @DeleteMapping("/{id}")
    public Result<Void> deleteProduct(
            @Parameter(description = "商品ID", required = true)
            @PathVariable Long id
    ) {
        productService.deleteProduct(id);
        return Result.success("商品删除成功");
    }

    /**
     * 查询SKU列表（用于开单）
     */
    @Operation(summary = "查询SKU列表", description = "用于开单时选择商品，支持SKU名称搜索")
    @GetMapping("/skus")
    public Result<Page<SkuListVO>> listSkusForBilling(
            @Parameter(description = "关键词（SKU名称模糊查询）")
            @RequestParam(required = false) String keyword,

            @PageableDefault(size = 20, sort = "sortOrder", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<SkuListVO> skus = productService.listSkusForBilling(keyword, pageable);
        return Result.success(skus);
    }
}
