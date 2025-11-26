package com.coreledger.common.mapper;

import org.mapstruct.*;

/**
 * MapStruct统一配置
 *
 * <p>所有Mapper接口继承此配置，避免重复配置</p>
 *
 * <p>配置说明：</p>
 * <ul>
 *   <li>componentModel = "spring": 生成Spring Bean</li>
 *   <li>unmappedTargetPolicy = IGNORE: 忽略未映射的目标字段</li>
 *   <li>nullValuePropertyMappingStrategy = IGNORE: null值不覆盖目标字段</li>
 *   <li>nullValueCheckStrategy = ALWAYS: 总是检查null值</li>
 *   <li>disableBuilder = true: 禁用Builder模式，使用setter</li>
 * </ul>
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@MapperConfig(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        builder = @Builder(disableBuilder = true)
)
public interface BeanMapperConf {
}
