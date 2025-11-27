package com.coreledger.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 商品SKU属性值VO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Schema(description = "商品SKU属性值")
public class ProductSkuAttrVO {

    /**
     * 属性名称
     */
    @Schema(description = "属性名称")
    private String attrName;

    /**
     * 属性值
     */
    @Schema(description = "属性值")
    private String attrValue;

    /**
     * 排序序号
     */
    @Schema(description = "排序序号")
    private Integer sortOrder;
}
