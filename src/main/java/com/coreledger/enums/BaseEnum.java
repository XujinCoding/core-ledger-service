package com.coreledger.enums;

/**
 * 枚举基础接口
 *
 * <p>所有业务枚举类必须实现此接口，用于统一的枚举转换处理</p>
 *
 * <p>实现此接口的枚举可以使用通用的 JPA 转换器和 MyBatis 类型处理器</p>
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
public interface BaseEnum {

    /**
     * 获取枚举值（数据库存储值）
     *
     * @return 枚举值
     */
    int getValue();

    /**
     * 获取枚举描述
     *
     * @return 枚举描述
     */
    String getDescription();
}
