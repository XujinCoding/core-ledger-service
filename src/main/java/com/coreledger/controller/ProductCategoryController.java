package com.coreledger.controller;

import com.coreledger.common.Result;
import com.coreledger.dto.product.CategoryCreateDTO;
import com.coreledger.dto.product.CategoryUpdateDTO;
import com.coreledger.enums.Status;
import com.coreledger.service.ProductCategoryService;
import com.coreledger.vo.product.CategoryTreeVO;
import com.coreledger.vo.product.CategoryVO;
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
 * 商品分类Controller
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "商品分类管理", description = "商品分类的增删改查、分类树查询")
public class ProductCategoryController {

    private final ProductCategoryService categoryService;

    /**
     * 创建商品分类
     */
    @Operation(summary = "创建商品分类", description = "创建新的商品分类（自动计算层级）")
    @PostMapping
    public Result<CategoryVO> createCategory(@Valid @RequestBody CategoryCreateDTO dto) {
        return Result.success(categoryService.createCategory(dto));
    }

    /**
     * 修改商品分类
     */
    @Operation(summary = "修改商品分类", description = "修改分类基本信息")
    @PutMapping("/{id}")
    public Result<CategoryVO> updateCategory(@PathVariable Long id,
                                             @Valid @RequestBody CategoryUpdateDTO dto) {
        return Result.success(categoryService.updateCategory(id, dto));
    }

    /**
     * 删除商品分类
     */
    @Operation(summary = "删除商品分类", description = "删除分类（不能有子分类和商品）")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success();
    }

    /**
     * 获取分类详情
     */
    @Operation(summary = "获取分类详情", description = "根据ID获取分类详情")
    @GetMapping("/{id}")
    public Result<CategoryVO> getCategory(@PathVariable Long id) {
        return Result.success(categoryService.getCategory(id));
    }


    /**
     * 获取分类树
     */
    @Operation(summary = "获取分类树", description = "获取完整的分类树形结构")
    @GetMapping("/tree")
    public Result<List<CategoryTreeVO>> getCategoryTree() {
        return Result.success(categoryService.getCategoryTree());
    }
}
