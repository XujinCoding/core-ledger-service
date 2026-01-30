package com.coreledger.vo.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 欠款趋势明细项
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "欠款趋势明细项")
public class DebtTrendDetailItem {

    @Schema(description = "标签（如：1月、2月...）")
    private String label;

    @Schema(description = "新增欠款")
    private BigDecimal newDebt;

    @Schema(description = "已还款金额")
    private BigDecimal paidAmount;

    @Schema(description = "累计欠款")
    private BigDecimal totalDebt;
}
