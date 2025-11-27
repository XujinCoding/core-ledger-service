package com.coreledger.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品属性实体
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "product_attr")
public class ProductAttr extends BaseEntity {

    /** 商品ID */
    @Column(name = "product_id", nullable = false)
    private Long productId;

    /** 属性名称 */
    @Column(name = "attr_name", nullable = false, length = 50)
    private String attrName;

    /** 排序序号 */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    /** 属性值列表 */
    @OneToMany(mappedBy = "productAttrId", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<ProductAttrValue> attrValues = new ArrayList<>();
}
