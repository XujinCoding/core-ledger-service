package com.coreledger.vo.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 商户统计VO（本月统计）
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "商户统计VO")
public class MerchantStatsVO {

    @Schema(description = "本月销售额")
    private BigDecimal monthlySales;

    @Schema(description = "待收款金额（赊账中的账单未付金额）")
    private BigDecimal pendingAmount;

    @Schema(description = "本月订单数")
    private Integer monthlyOrders;
}
