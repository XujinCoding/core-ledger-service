package com.coreledger.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * 账本明细实体
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "ledger_item")
public class LedgerItem extends BaseEntity {

    /** 账本ID */
    @Column(name = "ledger_id", nullable = false)
    private Long ledgerId;

    /** 商品ID */
    @Column(name = "product_id", nullable = false)
    private Long productId;

    /** 商品名称（冗余） */
    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    /** SKU ID */
    @Column(name = "sku_id")
    private Long skuId;

    /** SKU名称（冗余） */
    @Column(name = "sku_name", length = 150)
    private String skuName;

    /** 实际售价（单价） */
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /** 数量 */
    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;
}
