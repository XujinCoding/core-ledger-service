package com.coreledger.config.converter;

import com.coreledger.enums.BaseEnum;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.stereotype.Component;

/**
 * String 到 BaseEnum 枚举的转换器工厂
 *
 * <p>用于 Spring MVC 参数绑定时，将请求参数中的数字字符串转换为对应的枚举类型</p>
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Component
public class StringToBaseEnumConverterFactory implements ConverterFactory<String, BaseEnum> {

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BaseEnum> Converter<String, T> getConverter(Class<T> targetType) {
        return new StringToEnumConverter<>((Class<T>) targetType);
    }

    private static class StringToEnumConverter<T extends BaseEnum> implements Converter<String, T> {

        private final Class<T> enumType;

        StringToEnumConverter(Class<T> enumType) {
            this.enumType = enumType;
        }

        @Override
        public T convert(String source) {
            if (source == null || source.trim().isEmpty()) {
                return null;
            }

            // 尝试按数字值转换
            try {
                int value = Integer.parseInt(source.trim());
                for (T enumConstant : enumType.getEnumConstants()) {
                    if (enumConstant.getValue() == value) {
                        return enumConstant;
                    }
                }
            } catch (NumberFormatException ignored) {
                // 不是数字，尝试按枚举名称转换
            }

            // 尝试按枚举名称转换
            for (T enumConstant : enumType.getEnumConstants()) {
                if (((Enum<?>) enumConstant).name().equalsIgnoreCase(source.trim())) {
                    return enumConstant;
                }
            }

            throw new IllegalArgumentException(
                    String.format("无法将 '%s' 转换为枚举类型 %s", source, enumType.getSimpleName()));
        }
    }
}
