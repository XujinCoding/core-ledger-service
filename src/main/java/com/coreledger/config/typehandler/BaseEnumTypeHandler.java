package com.coreledger.config.typehandler;

import com.coreledger.enums.BaseEnum;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 通用枚举 MyBatis 类型处理器
 *
 * <p>用于所有实现 {@link BaseEnum} 接口的枚举类型与数据库 INT/TINYINT 类型的相互转换</p>
 *
 * <p>使用方式:</p>
 * <pre>
 * {@code
 * // 定义具体的类型处理器
 * @MappedTypes(LedgerStatus.class)
 * @MappedJdbcTypes(JdbcType.TINYINT)
 * public class LedgerStatusTypeHandler extends BaseEnumTypeHandler<LedgerStatus> {
 *     public LedgerStatusTypeHandler() {
 *         super(LedgerStatus.class);
 *     }
 * }
 *
 * // application.yml 中配置
 * mybatis:
 *   type-handlers-package: com.coreledger.config.typehandler
 * }
 * </pre>
 *
 * @param <E> 枚举类型，必须实现 BaseEnum 接口
 * @author Core Ledger Team
 * @since 1.0.0
 */
public abstract class BaseEnumTypeHandler<E extends Enum<E> & BaseEnum> extends BaseTypeHandler<E> {

    private final Class<E> enumClass;

    /**
     * 构造函数
     *
     * @param enumClass 枚举类型
     */
    protected BaseEnumTypeHandler(Class<E> enumClass) {
        if (enumClass == null) {
            throw new IllegalArgumentException("枚举类型不能为空");
        }
        this.enumClass = enumClass;
    }

    /**
     * 设置非空参数（Java → 数据库）
     *
     * @param ps PreparedStatement
     * @param i 参数索引
     * @param parameter 枚举对象
     * @param jdbcType JDBC类型
     * @throws SQLException SQL异常
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setInt(i, parameter.getValue());
    }

    /**
     * 根据列名获取可空结果（数据库 → Java）
     *
     * @param rs ResultSet
     * @param columnName 列名
     * @return 枚举对象
     * @throws SQLException SQL异常
     */
    @Override
    public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : fromValue(value);
    }

    /**
     * 根据列索引获取可空结果（数据库 → Java）
     *
     * @param rs ResultSet
     * @param columnIndex 列索引
     * @return 枚举对象
     * @throws SQLException SQL异常
     */
    @Override
    public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int value = rs.getInt(columnIndex);
        return rs.wasNull() ? null : fromValue(value);
    }

    /**
     * 获取可空结果（存储过程）
     *
     * @param cs CallableStatement
     * @param columnIndex 列索引
     * @return 枚举对象
     * @throws SQLException SQL异常
     */
    @Override
    public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int value = cs.getInt(columnIndex);
        return cs.wasNull() ? null : fromValue(value);
    }

    /**
     * 根据枚举值获取枚举对象
     *
     * @param value 枚举值
     * @return 枚举对象，未找到时返回第一个枚举值
     */
    private E fromValue(int value) {
        for (E enumConstant : enumClass.getEnumConstants()) {
            if (enumConstant.getValue() == value) {
                return enumConstant;
            }
        }

        // 未找到匹配项时返回第一个枚举值作为默认值
        E[] constants = enumClass.getEnumConstants();
        return constants.length > 0 ? constants[0] : null;
    }
}
