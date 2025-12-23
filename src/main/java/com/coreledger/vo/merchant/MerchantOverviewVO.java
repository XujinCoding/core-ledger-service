package com.coreledger.vo.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商户概览统计VO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "商户概览统计VO")
public class MerchantOverviewVO {

    @Schema(description = "客户数")
    private Integer customerCount;

    @Schema(description = "商品数")
    private Integer productCount;

    @Schema(description = "账单数")
    private Integer ledgerCount;
}
