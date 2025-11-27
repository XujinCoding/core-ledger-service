package com.coreledger.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 商品属性值VO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Schema(description = "商品属性值")
public class ProductAttrValueVO {

    /**
     * 属性值ID
     */
    @Schema(description = "属性值ID")
    private Long id;

    /**
     * 商品属性ID
     */
    @Schema(description = "商品属性ID")
    private Long productAttrId;

    /**
     * 属性值
     */
    @Schema(description = "属性值")
    private String value;

    /**
     * 排序序号
     */
    @Schema(description = "排序序号")
    private Integer sortOrder;
}
