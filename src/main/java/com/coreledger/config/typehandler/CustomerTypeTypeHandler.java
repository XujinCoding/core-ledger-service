package com.coreledger.config.typehandler;

import com.coreledger.enums.CustomerType;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * CustomerType 枚举类型处理器
 * <p>用于 MyBatis 中 CustomerType 与数据库 TINYINT 的转换</p>
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@MappedTypes(CustomerType.class)
@MappedJdbcTypes(JdbcType.TINYINT)
public class CustomerTypeTypeHandler extends BaseTypeHandler<CustomerType> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, CustomerType parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getValue());
    }

    @Override
    public CustomerType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : CustomerType.fromValue(value);
    }

    @Override
    public CustomerType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int value = rs.getInt(columnIndex);
        return rs.wasNull() ? null : CustomerType.fromValue(value);
    }

    @Override
    public CustomerType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int value = cs.getInt(columnIndex);
        return cs.wasNull() ? null : CustomerType.fromValue(value);
    }
}
