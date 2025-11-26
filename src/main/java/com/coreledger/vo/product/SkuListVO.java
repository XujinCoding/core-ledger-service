package com.coreledger.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * SKU列表VO (用于开单时选择商品)
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Schema(description = "SKU列表响应")
public class SkuListVO {

    @Schema(description = "SKU ID")
    private Long skuId;

    @Schema(description = "商品ID (SPU)")
    private Long productId;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "SKU名称")
    private String skuName;

    @Schema(description = "属性值映射")
    private Map<String, String> attrValueMap;

    @Schema(description = "销售价格")
    private BigDecimal price;

    @Schema(description = "SKU图片URL")
    private String imageUrl;

    @Schema(description = "基本单位")
    private String unit;

    @Schema(description = "存放位置")
    private String location;
}
