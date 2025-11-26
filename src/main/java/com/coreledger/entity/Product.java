package com.coreledger.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * 商品实体 (SPU - Standard Product Unit)
 *
 * <p>对应数据库表: product</p>
 * <p>此表存储商品主信息，具体规格由 product_sku 表管理</p>
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "product")
public class Product extends BaseEntity {

    /** 分类ID */
    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    /** 商品名称 (SPU) */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /** 商品主图URL */
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /** 商品描述 */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 基本单位
     * <p>如：件、箱、斤、公斤等</p>
     */
    @Column(name = "unit", nullable = false, length = 20)
    private String unit = "件";

    /**
     * 存放位置
     * <p>如：A区3排5列</p>
     */
    @Column(name = "location", length = 100)
    private String location;
}
