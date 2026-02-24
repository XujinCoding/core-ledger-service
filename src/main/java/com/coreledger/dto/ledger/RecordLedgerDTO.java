package com.coreledger.dto.ledger;

import com.coreledger.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 记账 DTO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
public class RecordLedgerDTO {

    /** 支付金额（可以为0，表示完全赊账）*/
    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0.00", message = "支付金额不能为负数")
    private BigDecimal paymentAmount;

    /** 支付方式（有支付时必填）*/
    private PaymentMethod paymentMethod;

    /** 备注 */
    @Size(max = 255, message = "备注长度不能超过255个字符")
    private String memo;

    /** 签名图片（base64编码或文件路径）*/
    private String signatureImage;
}
