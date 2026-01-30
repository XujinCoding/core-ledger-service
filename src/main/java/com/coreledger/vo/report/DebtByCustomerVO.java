package com.coreledger.vo.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 按客户欠款VO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "按客户欠款VO")
public class DebtByCustomerVO {

    @Schema(description = "客户ID")
    private Long customerId;

    @Schema(description = "客户名称")
    private String customerName;

    @Schema(description = "欠款金额")
    private BigDecimal amount;

    @Schema(description = "账单数")
    private Integer ledgerCount;

    @Schema(description = "逾期天数")
    private Integer overdueDays;
}
