package com.coreledger.dto.ledger;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 关闭账单 DTO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
public class CloseLedgerDTO {

    /** 关闭原因 */
    @NotBlank(message = "关闭原因不能为空")
    @Size(max = 255, message = "关闭原因长度不能超过255个字符")
    private String reason;
}
