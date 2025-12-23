package com.coreledger.vo.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 客户统计信息VO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "客户统计信息")
public class CustomerStatsVO {

    @Schema(description = "总消费金额")
    private BigDecimal totalAmount;

    @Schema(description = "订单数量")
    private Integer orderCount;

    @Schema(description = "平均消费金额")
    private BigDecimal avgAmount;
}
