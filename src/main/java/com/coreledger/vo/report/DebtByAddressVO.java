package com.coreledger.vo.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 按地址欠款VO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "按地址欠款VO")
public class DebtByAddressVO {

    @Schema(description = "地址")
    private String address;

    @Schema(description = "欠款金额")
    private BigDecimal amount;

    @Schema(description = "客户数")
    private Integer customerCount;
}
