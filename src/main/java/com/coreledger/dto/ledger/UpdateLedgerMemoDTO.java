package com.coreledger.dto.ledger;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改账单备注 DTO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
public class UpdateLedgerMemoDTO {

    /** 备注 */
    @Size(max = 255, message = "备注长度不能超过255个字符")
    private String memo;
}
