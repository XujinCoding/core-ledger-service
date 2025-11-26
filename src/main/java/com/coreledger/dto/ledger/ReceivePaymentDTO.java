package com.coreledger.dto.ledger;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;

/**
 * 收款DTO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
public class ReceivePaymentDTO {

    /** 支付金额 */
    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0.01", message = "支付金额必须大于0")
    private BigDecimal amount;

    /** 支付方式: 1=现金, 2=微信, 3=支付宝, 4=银行转账 */
    @NotNull(message = "支付方式不能为空")
    private Integer paymentMethod;

    /** 备注 */
    @Length(max = 255, message = "备注长度不能超过255")
    private String memo;
}
