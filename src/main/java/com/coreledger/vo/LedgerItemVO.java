package com.coreledger.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 账单明细 VO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
public class LedgerItemVO {

    /** 明细ID */
    private Long id;

    /** 商品ID */
    private Long productId;

    /** 商品名称 */
    private String productName;

    /** SKU ID */
    private Long skuId;

    /** SKU名称 */
    private String skuName;

    /** 单价 */
    private BigDecimal price;

    /** 数量 */
    private Integer quantity;

    /** 小计金额 */
    private BigDecimal amount;
}
