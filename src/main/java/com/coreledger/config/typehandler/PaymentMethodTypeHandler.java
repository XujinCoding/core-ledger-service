package com.coreledger.config.typehandler;

import com.coreledger.enums.PaymentMethod;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

/**
 * 支付方式枚举 MyBatis 类型处理器
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@MappedTypes(PaymentMethod.class)
@MappedJdbcTypes(JdbcType.TINYINT)
public class PaymentMethodTypeHandler extends BaseEnumTypeHandler<PaymentMethod> {

    public PaymentMethodTypeHandler() {
        super(PaymentMethod.class);
    }
}
