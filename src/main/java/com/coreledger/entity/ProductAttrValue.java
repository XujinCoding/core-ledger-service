package com.coreledger.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 商品属性值实体
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "product_attr_value")
public class ProductAttrValue extends BaseEntity {

    /** 商品属性ID */
    @Column(name = "product_attr_id", nullable = false)
    private Long productAttrId;

    /** 属性值 */
    @Column(name = "value", nullable = false, length = 255)
    private String value;

    /** 排序序号 */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;
}
