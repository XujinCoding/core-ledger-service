package com.coreledger.config.converter;

import com.coreledger.enums.BaseEnum;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 通用枚举 Spring MVC 转换器工厂
 *
 * <p>用于 GET 请求参数、表单提交等场景，将字符串转换为实现 {@link BaseEnum} 接口的枚举</p>
 *
 * <p>支持两种格式的输入：</p>
 * <ul>
 *     <li>数值格式：如 "1"、"2"，根据枚举的 value 值匹配</li>
 *     <li>名称格式：如 "IN_PROGRESS"，根据枚举名称匹配</li>
 * </ul>
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Component
public class BaseEnumConverterFactory implements ConverterFactory<String, BaseEnum> {

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BaseEnum> Converter<String, T> getConverter(Class<T> targetType) {
        return new StringToBaseEnumConverter<>((Class<T>) targetType);
    }

    /**
     * 字符串到 BaseEnum 枚举的转换器
     *
     * @param <T> 枚举类型
     */
    private static class StringToBaseEnumConverter<T extends BaseEnum> implements Converter<String, T> {

        private final Class<T> enumType;

        StringToBaseEnumConverter(Class<T> enumType) {
            this.enumType = enumType;
        }

        @Override
        public T convert(String source) {
            if (!StringUtils.hasText(source)) {
                return null;
            }

            source = source.trim();

            // 尝试按数值匹配
            try {
                int value = Integer.parseInt(source);
                for (T enumConstant : enumType.getEnumConstants()) {
                    if (enumConstant.getValue() == value) {
                        return enumConstant;
                    }
                }
            } catch (NumberFormatException ignored) {
                // 不是数字，尝试按名称匹配
            }

            // 尝试按枚举名称匹配
            for (T enumConstant : enumType.getEnumConstants()) {
                if (((Enum<?>) enumConstant).name().equalsIgnoreCase(source)) {
                    return enumConstant;
                }
            }

            throw new IllegalArgumentException(
                    String.format("无法将 '%s' 转换为枚举类型 %s", source, enumType.getSimpleName())
            );
        }
    }
}
