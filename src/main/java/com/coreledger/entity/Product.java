package com.coreledger.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * 商品实体
 *
 * <p>对应数据库表: product</p>
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

    /** 商品名称 */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 标准价格
     * <p>实际销售时价格可能不同</p>
     */
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    /** 规格型号 */
    @Column(name = "spec", length = 100)
    private String spec;

    /**
     * 单位
     * <p>如：件、箱、斤、公斤等</p>
     */
    @Column(name = "unit", nullable = false, length = 20)
    private String unit = "件";
}
