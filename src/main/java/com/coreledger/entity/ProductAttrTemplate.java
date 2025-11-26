package com.coreledger.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 商品属性模板实体
 *
 * <p>对应数据库表: product_attr_template</p>
 * <p>为商品分类预定义常用属性模板</p>
 * <p>例如：水果分类可预定义"重量"、"等级"等属性</p>
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "product_attr_template")
public class ProductAttrTemplate extends BaseEntity {

    /** 关联分类ID */
    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    /**
     * 属性名称
     * <p>如：重量、规格、等级</p>
     */
    @Column(name = "attr_name", nullable = false, length = 50)
    private String attrName;

    /** 排序序号 */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    /**
     * 是否必填
     * <p>1=必填, 0=可选</p>
     */
    @Column(name = "is_required", nullable = false)
    private Boolean isRequired = false;
}
