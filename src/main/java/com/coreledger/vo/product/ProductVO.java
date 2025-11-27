package com.coreledger.vo.product;

import com.coreledger.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品详情VO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Schema(description = "商品详情")
public class ProductVO {

    /**
     * 商品ID
     */
    @Schema(description = "商品ID")
    private Long id;

    /**
     * 分类ID
     */
    @Schema(description = "分类ID")
    private Long categoryId;

    /**
     * 分类名称
     */
    @Schema(description = "分类名称")
    private String categoryName;

    /**
     * 商品名称
     */
    @Schema(description = "商品名称")
    private String name;

    /**
     * 商品主图URL
     */
    @Schema(description = "商品主图URL")
    private String imageUrl;

    /**
     * 商品描述
     */
    @Schema(description = "商品描述")
    private String description;

    /**
     * 标准价格
     */
    @Schema(description = "标准价格")
    private BigDecimal price;

    /**
     * 规格型号
     */
    @Schema(description = "规格型号")
    private String spec;

    /**
     * 单位
     */
    @Schema(description = "单位")
    private String unit;

    /**
     * 存放位置
     */
    @Schema(description = "存放位置")
    private String location;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String memo;

    /**
     * 状态
     */
    @Schema(description = "状态")
    private Status status;

    /**
     * 商品属性列表
     */
    @Schema(description = "商品属性列表")
    private List<ProductAttrVO> attrs = new ArrayList<>();

    /**
     * 商品SKU列表
     */
    @Schema(description = "商品SKU列表")
    private List<ProductSkuVO> skus = new ArrayList<>();

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createInstant;

    /**
     * 修改时间
     */
    @Schema(description = "修改时间")
    private LocalDateTime modifyInstant;
}
