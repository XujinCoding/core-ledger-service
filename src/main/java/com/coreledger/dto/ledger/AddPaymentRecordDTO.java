package com.coreledger.dto.ledger;

import com.coreledger.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 新增支付记录 DTO（后台接口，前端不开放）
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
public class AddPaymentRecordDTO {

    /** 支付金额（必须大于0）*/
    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0.01", message = "支付金额必须大于0")
    private BigDecimal paymentAmount;

    /** 支付方式（必填）*/
    @NotNull(message = "支付方式不能为空")
    private PaymentMethod paymentMethod;

    /** 备注 */
    @Size(max = 255, message = "备注长度不能超过255个字符")
    private String memo;
}
