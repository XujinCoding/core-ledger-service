package com.coreledger.dto.ledger;

import com.coreledger.common.PageCondition;
import com.coreledger.enums.LedgerStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 账单搜索条件DTO（支持客户姓名和电话模糊查询）
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Getter
@Setter
@Schema(description = "账单搜索条件")
public class LedgerSearchDTO extends PageCondition {

    /** 客户姓名（模糊查询） */
    @Schema(description = "客户姓名（模糊查询）")
    private String customerName;

    /** 客户电话（模糊查询） */
    @Schema(description = "客户电话（模糊查询）")
    private String customerPhone;

    /** 账单状态 */
    @Schema(description = "账单状态")
    private LedgerStatus ledgerStatus;
}
