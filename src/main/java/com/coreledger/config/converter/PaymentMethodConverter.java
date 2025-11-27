package com.coreledger.config.converter;

import com.coreledger.enums.PaymentMethod;
import jakarta.persistence.Converter;

/**
 * 支付方式枚举 JPA 转换器
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Converter(autoApply = false)
public class PaymentMethodConverter extends BaseEnumConverter<PaymentMethod> {

    public PaymentMethodConverter() {
        super(PaymentMethod.class);
    }
}
