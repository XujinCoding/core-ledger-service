package com.coreledger.vo.product;

import com.coreledger.enums.PriceStatus;
import com.coreledger.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品SKU VO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Schema(description = "商品SKU")
public class ProductSkuVO {

    /**
     * SKU ID
     */
    @Schema(description = "SKU ID")
    private Long id;

    /**
     * 商品ID
     */
    @Schema(description = "商品ID")
    private Long productId;

    /**
     * SKU名称
     */
    @Schema(description = "SKU名称")
    private String skuName;

    /**
     * 定价状态
     */
    @Schema(description = "定价状态")
    private PriceStatus priceStatus;

    /**
     * SKU销售价格
     */
    @Schema(description = "SKU销售价格")
    private BigDecimal price;

    /**
     * SKU图片URL
     */
    @Schema(description = "SKU图片URL")
    private String imageUrl;

    /**
     * 排序序号
     */
    @Schema(description = "排序序号")
    private Integer sortOrder;

    /**
     * 状态
     */
    @Schema(description = "状态")
    private Status status;

    /**
     * SKU属性值列表
     */
    @Schema(description = "SKU属性值列表")
    private List<ProductSkuAttrVO> skuAttrs = new ArrayList<>();
}
