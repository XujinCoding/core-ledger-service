package com.coreledger.controller;

import com.coreledger.common.Result;
import com.coreledger.dto.product.SkuPriceUpdateDTO;
import com.coreledger.enums.PriceStatus;
import com.coreledger.enums.Status;
import com.coreledger.service.ProductSkuService;
import com.coreledger.vo.product.ProductSkuVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品SKU Controller
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/skus")
@RequiredArgsConstructor
@Tag(name = "商品SKU管理", description = "SKU查询、定价、状态管理")
public class ProductSkuController {

    private final ProductSkuService skuService;

    /**
     * 获取商品SKU列表
     */
    @Operation(summary = "获取商品SKU列表", description = "获取指定商品的所有SKU（支持定价状态筛选）")
    @GetMapping("/product/{productId}")
    public Result<List<ProductSkuVO>> getProductSkus(@PathVariable Long productId,
                                                     @RequestParam(required = false) PriceStatus priceStatus) {
        return Result.success(skuService.getProductSkus(productId, priceStatus));
    }

    /**
     * 获取已定价SKU列表
     */
    @Operation(summary = "获取已定价SKU", description = "获取商品的已定价SKU（业务专用）")
    @GetMapping("/product/{productId}/priced")
    public Result<List<ProductSkuVO>> getPricedSkus(@PathVariable Long productId) {
        return Result.success(skuService.getPricedSkus(productId));
    }

    /**
     * 获取SKU详情
     */
    @Operation(summary = "获取SKU详情", description = "根据ID获取SKU详细信息")
    @GetMapping("/{id}")
    public Result<ProductSkuVO> getSkuDetail(@PathVariable Long id) {
        return Result.success(skuService.getSkuDetail(id));
    }

    /**
     * 修改SKU价格
     */
    @Operation(summary = "修改SKU价格", description = "修改单个SKU的价格（价格必须>0）")
    @PutMapping("/{id}/price")
    public Result<ProductSkuVO> updateSkuPrice(@PathVariable Long id,
                                               @RequestParam BigDecimal price) {
        return Result.success(skuService.updateSkuPrice(id, price));
    }

    /**
     * 批量定价
     */
    @Operation(summary = "批量定价SKU", description = "批量修改多个SKU的价格")
    @PutMapping("/batch-price")
    public Result<Integer> batchUpdatePrice(@Valid @RequestBody SkuPriceUpdateDTO dto) {
        int successCount = skuService.batchUpdatePrice(dto);
        return Result.success(String.format("定价成功%d个", successCount), successCount);
    }

    @Operation(summary = "按名称模糊查询已定价SKU", description = "根据SKU名称（不区分大小写）全模糊查询，返回已定价且启用的SKU列表")
    @GetMapping("/search/priced")
    public Result<List<ProductSkuVO>> searchPricedSkusByName(@RequestParam("name") String name) {
        return Result.success(skuService.searchPricedSkusByName(name));
    }
}
