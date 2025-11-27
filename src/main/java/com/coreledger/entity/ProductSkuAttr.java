package com.coreledger.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 商品SKU属性值实体
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "product_sku_attr")
public class ProductSkuAttr extends BaseEntity {

    /** SKU ID */
    @Column(name = "sku_id", nullable = false)
    private Long skuId;

    /** 商品属性ID */
    @Column(name = "product_attr_id", nullable = false)
    private Long productAttrId;

    /** 商品属性名称（冗余） */
    @Column(name = "product_attr_name", nullable = false, length = 50)
    private String productAttrName;

    /** 商品属性值ID */
    @Column(name = "product_attr_value_id", nullable = false)
    private Long productAttrValueId;

    /** 商品属性值名称（冗余） */
    @Column(name = "product_attr_value_name", nullable = false, length = 255)
    private String productAttrValueName;

    /** 排序序号 */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;
}
