package com.coreledger.vo.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 收入统计VO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "收入统计VO")
public class IncomeStatsVO {

    @Schema(description = "总收入")
    private BigDecimal totalIncome;

    @Schema(description = "订单数")
    private Integer orderCount;

    @Schema(description = "客户数")
    private Integer customerCount;

    @Schema(description = "最大金额（用于计算柱状图比例）")
    private BigDecimal maxAmount;

    @Schema(description = "收入明细列表")
    private List<IncomeDetailItem> details;
}
