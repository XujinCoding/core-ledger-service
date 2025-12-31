package com.coreledger.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 账单列表统计VO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "账单列表统计VO")
public class LedgerListStatsVO {

    /** 总金额 */
    @Schema(description = "总金额")
    private BigDecimal totalAmount;

    /** 已收金额 */
    @Schema(description = "已收金额")
    private BigDecimal paidAmount;

    /** 待收金额 */
    @Schema(description = "待收金额")
    private BigDecimal pendingAmount;

    /** 账单数量 */
    @Schema(description = "账单数量")
    private Long ledgerCount;
}
