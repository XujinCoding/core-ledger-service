package com.coreledger.controller;

import com.coreledger.common.Result;
import com.coreledger.dto.product.ProductAttrBatchUpdateDTO;
import com.coreledger.service.ProductAttrService;
import com.coreledger.vo.product.ProductAttrVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author XuJ
 * @date 2025年12月04日 17:24
 */
@Slf4j
@RestController
@RequestMapping("/api/product-attr")
@RequiredArgsConstructor
@Tag(name = "商品属性管理", description = "商品属性管理")
public class ProductAttrController {

    private final ProductAttrService attrService;

    /**
     * 批量更新商品属性（推荐使用）⭐
     *
     * <p>一次性提交商品的所有属性和属性值，后端自动识别新增、修改、删除操作，并增量更新SKU</p>
     */
    @Operation(summary = "批量更新商品属性（推荐）",
            description = "一次性提交所有属性和属性值，后端智能识别新增/修改/删除，增量更新SKU（保留未变化的SKU ID）")
    @PutMapping("/{id}/attrs/batch")
    public Result<List<ProductAttrVO>> batchUpdateAttrs(@PathVariable Long id,
                                                        @Valid @RequestBody ProductAttrBatchUpdateDTO dto) {
        return Result.success(attrService.batchUpdateAttrs(id, dto));
    }

    /**
     * 获取商品所有属性及其值
     */
    @Operation(summary = "获取商品属性", description = "获取商品所有属性及其值")
    @GetMapping("/{id}/attrs")
    public Result<List<ProductAttrVO>> getProductAttrs(@PathVariable Long id) {
        return Result.success(attrService.getProductAttrs(id));
    }
}
