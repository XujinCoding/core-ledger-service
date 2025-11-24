package com.coreledger.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * 账本明细实体
 *
 * <p>对应数据库表: ledger_item</p>
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "ledger_item")
public class LedgerItem extends BaseEntity {

    /** 账本ID */
    @Column(name = "ledger_id", nullable = false)
    private Long ledgerId;

    /** 商品ID */
    @Column(name = "product_id", nullable = false)
    private Long productId;

    /**
     * 商品名称（冗余存储）
     * <p>冗余存储商品名称，防止商品信息变更影响历史账单</p>
     */
    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    /**
     * 实际售价
     * <p>可能与商品标准价格不同</p>
     */
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /** 数量 */
    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    /**
     * 计算小计金额
     *
     * @return 小计金额 = 单价 × 数量
     */
    public BigDecimal getSubtotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}
