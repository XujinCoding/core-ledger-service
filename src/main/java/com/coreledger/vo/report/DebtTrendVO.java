package com.coreledger.vo.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 欠款趋势VO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "欠款趋势VO")
public class DebtTrendVO {

    @Schema(description = "当前总欠款")
    private BigDecimal totalDebt;

    @Schema(description = "欠款客户数")
    private Integer debtCustomerCount;

    @Schema(description = "最大金额（用于计算柱状图比例）")
    private BigDecimal maxAmount;

    @Schema(description = "趋势明细列表")
    private List<DebtTrendDetailItem> details;
}
