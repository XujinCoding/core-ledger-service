package com.coreledger.vo.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 客户列表统计VO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "客户列表统计VO")
public class CustomerListStatsVO {

    /** 客户总数 */
    @Schema(description = "客户总数")
    private Long customerCount;
}
