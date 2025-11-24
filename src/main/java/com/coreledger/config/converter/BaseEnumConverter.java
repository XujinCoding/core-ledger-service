package com.coreledger.config.converter;

import com.coreledger.enums.BaseEnum;
import jakarta.persistence.AttributeConverter;

/**
 * 通用枚举 JPA 转换器
 *
 * <p>用于所有实现 {@link BaseEnum} 接口的枚举类型与数据库 INT/TINYINT 类型的相互转换</p>
 *
 * <p>使用方式:</p>
 * <pre>
 * {@code
 * @Entity
 * public class Ledger {
 *     @Column(name = "ledger_status")
 *     @Convert(converter = LedgerStatusConverter.class)
 *     private LedgerStatus ledgerStatus;
 * }
 *
 * // 定义具体的转换器
 * @Converter(autoApply = false)
 * public class LedgerStatusConverter extends BaseEnumConverter<LedgerStatus> {
 *     public LedgerStatusConverter() {
 *         super(LedgerStatus.class);
 *     }
 * }
 * }
 * </pre>
 *
 * @param <E> 枚举类型，必须实现 BaseEnum 接口
 * @author Core Ledger Team
 * @since 1.0.0
 */
public abstract class BaseEnumConverter<E extends Enum<E> & BaseEnum> implements AttributeConverter<E, Integer> {

    private final Class<E> enumClass;

    /**
     * 构造函数
     *
     * @param enumClass 枚举类型
     */
    protected BaseEnumConverter(Class<E> enumClass) {
        this.enumClass = enumClass;
    }

    /**
     * 将枚举转换为数据库字段值
     *
     * @param attribute 枚举对象
     * @return 枚举值（Integer），枚举为 null 时返回 null
     */
    @Override
    public Integer convertToDatabaseColumn(E attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getValue();
    }

    /**
     * 将数据库字段值转换为枚举
     *
     * @param dbData 数据库值（Integer）
     * @return 枚举对象，数据库值为 null 时返回 null
     */
    @Override
    public E convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }

        for (E enumConstant : enumClass.getEnumConstants()) {
            if (enumConstant.getValue() == dbData) {
                return enumConstant;
            }
        }

        // 未找到匹配项时返回第一个枚举值作为默认值
        E[] constants = enumClass.getEnumConstants();
        return constants.length > 0 ? constants[0] : null;
    }
}
