package com.coreledger.dto.ledger;

import com.coreledger.enums.LedgerStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 账单查询条件DTO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Getter
@Setter
@Schema(description = "账单查询条件")
public class LedgerQueryDTO {

    /** 客户ID */
    @Schema(description = "客户ID")
    private Long customerId;

    /** 账单状态 */
    @Schema(description = "账单状态")
    private LedgerStatus ledgerStatus;

    /** 创建时间-开始 */
    @Schema(description = "创建时间-开始")
    private LocalDate createdAtStart;

    /** 创建时间-结束 */
    @Schema(description = "创建时间-结束")
    private LocalDate createdAtEnd;
}
