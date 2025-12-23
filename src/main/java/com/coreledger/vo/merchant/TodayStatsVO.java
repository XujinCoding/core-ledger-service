package com.coreledger.vo.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 今日汇总统计VO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "今日汇总统计VO")
public class TodayStatsVO {

    @Schema(description = "今日销售额（总金额）")
    private BigDecimal sales;

    @Schema(description = "今日已收款")
    private BigDecimal payment;

    @Schema(description = "今日新增欠款")
    private BigDecimal debt;

    @Schema(description = "今日订单数")
    private Integer orders;
}
