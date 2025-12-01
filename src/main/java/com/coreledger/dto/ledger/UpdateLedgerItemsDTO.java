package com.coreledger.dto.ledger;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量更新账单明细 DTO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
public class UpdateLedgerItemsDTO {

    /** 明细列表 */
    @NotEmpty(message = "明细列表不能为空")
    @Valid
    private List<LedgerItemDTO> items;
}
