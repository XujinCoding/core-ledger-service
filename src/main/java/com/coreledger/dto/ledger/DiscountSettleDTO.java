package com.coreledger.dto.ledger;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;

/**
 * 抹零结清DTO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
public class DiscountSettleDTO {

    /** 抹零金额 */
    @NotNull(message = "抹零金额不能为空")
    @DecimalMin(value = "0.01", message = "抹零金额必须大于0")
    private BigDecimal discountAmount;

    /** 备注 */
    @Length(max = 255, message = "备注长度不能超过255")
    private String memo;
}
