package com.coreledger.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品信息实体
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "product")
public class Product extends BaseEntity {

    /** 分类ID */
    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    /** 商品名称 */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /** 商品主图URL */
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /** 商品描述 */
    @Column(name = "description", length = 500)
    private String description;

    /** 标准价格 */
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    /** 规格型号 */
    @Column(name = "spec", length = 100)
    private String spec;

    /** 单位 */
    @Column(name = "unit", nullable = false, length = 20)
    private String unit = "件";

    /** 存放位置 */
    @Column(name = "location", length = 100)
    private String location;

    /** 商品属性列表 */
    @OneToMany(mappedBy = "productId", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<ProductAttr> attrs = new ArrayList<>();

    /** 商品SKU列表 */
    @OneToMany(mappedBy = "productId", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<ProductSku> skus = new ArrayList<>();
}
