package com.coreledger.dto.ledger;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 新增账单 DTO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
public class CreateLedgerDTO {

    /** 客户ID */
    @NotNull(message = "客户ID不能为空")
    private Long customerId;

    /** 账单明细列表（可选，允许创建空明细账单）*/
    @Valid
    private List<LedgerItemDTO> items;

    /** 备注 */
    @Size(max = 255, message = "备注长度不能超过255个字符")
    private String memo;

    /** 账单标识 */
    @NotNull(message = "客户ID不能为空")
    private Long merchantId;
}
