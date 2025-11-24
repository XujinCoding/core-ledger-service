# 枚举与数据库映射规范

## 1. 概述

Core Ledger 系统使用枚举类型来表示固定的业务状态和类型，为了确保枚举在 Java 代码、数据库、JSON 序列化中的一致性，制定本规范。

---

## 2. 设计原则

### 2.1 数据库存储原则

**原则**: 数据库使用 `TINYINT` 或 `INT` 类型存储枚举值，**不使用** `VARCHAR` 或 `ENUM` 类型

**理由**:
- ✅ 性能：整数索引性能优于字符串
- ✅ 存储：整数占用空间更小
- ✅ 兼容性：整数类型在各数据库中兼容性更好
- ✅ 可读性：通过注释说明枚举含义

**示例**:
```sql
`ledger_status` TINYINT NOT NULL DEFAULT 1 COMMENT '账本状态: 1=进行中, 2=部分缴费, 3=已结清, 4=赊账中, 5=已关闭'
```

---

### 2.2 Java 枚举设计原则

**结构**:
```java
@Getter
public enum EnumName {
    CONSTANT_1(value1, "描述1"),
    CONSTANT_2(value2, "描述2");

    @JsonValue  // JSON 序列化时返回 value
    private final int value;
    private final String description;

    EnumName(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public static EnumName fromValue(int value) {
        // 反序列化逻辑
    }
}
```

**要素**:
1. ✅ 使用 `@Getter` 自动生成 getter
2. ✅ 使用 `@JsonValue` 标注序列化字段
3. ✅ 提供 `fromValue(int)` 静态方法
4. ✅ 包含 `value` (int) 和 `description` (String) 字段
5. ✅ 添加详细的 JavaDoc 注释

---

## 3. 枚举定义规范

### 3.1 完整枚举示例

```java
package com.coreledger.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 账本状态枚举
 *
 * <p>状态说明:</p>
 * <ul>
 *   <li><b>IN_PROGRESS (进行中)</b>: 账单创建，未收款，在主页面可查询</li>
 *   <li><b>PARTIAL (部分缴费)</b>: 已收部分款项，继续收款，在主页面可查询</li>
 *   <li><b>CLEARED (已结清)</b>: 完全缴费或抹零结清，不在主页面显示</li>
 *   <li><b>ON_CREDIT (赊账中)</b>: 客户赊账，暂不催收，不在主页面显示</li>
 *   <li><b>CLOSED (已关闭)</b>: 作废的账单</li>
 * </ul>
 *
 * <p>状态转换规则请参考: docs/LEDGER_STATUS_TRANSITIONS.md</p>
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Getter
public enum LedgerStatus {

    /** 进行中 */
    IN_PROGRESS(1, "进行中"),

    /** 部分缴费 */
    PARTIAL(2, "部分缴费"),

    /** 已结清 */
    CLEARED(3, "已结清"),

    /** 赊账中 */
    ON_CREDIT(4, "赊账中"),

    /** 已关闭 */
    CLOSED(5, "已关闭");

    /**
     * 枚举值（数据库存储）
     */
    @JsonValue
    private final int value;

    /**
     * 描述
     */
    private final String description;

    /**
     * 构造函数
     *
     * @param value 枚举值
     * @param description 描述
     */
    LedgerStatus(int value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 根据枚举值获取枚举对象
     *
     * @param value 枚举值
     * @return 枚举对象，未找到时返回 IN_PROGRESS
     */
    public static LedgerStatus fromValue(int value) {
        for (LedgerStatus status : LedgerStatus.values()) {
            if (status.value == value) {
                return status;
            }
        }
        return IN_PROGRESS;
    }

    /**
     * 判断是否为活跃状态（在主页面可查询）
     *
     * @return true 如果状态为 IN_PROGRESS 或 PARTIAL
     */
    public boolean isActive() {
        return this == IN_PROGRESS || this == PARTIAL;
    }
}
```

---

## 4. JPA 枚举映射

### 4.1 JPA 枚举转换器

**位置**: `com.coreledger.config.converter`

**命名规则**: `枚举名 + Converter`

**示例**: `LedgerStatusConverter.java`

```java
package com.coreledger.config.converter;

import com.coreledger.enums.LedgerStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * LedgerStatus 枚举 JPA 转换器
 *
 * <p>用于 JPA 实体中 LedgerStatus 枚举与数据库 TINYINT 类型的相互转换</p>
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Converter(autoApply = false)
public class LedgerStatusConverter implements AttributeConverter<LedgerStatus, Integer> {

    /**
     * 将枚举转换为数据库字段值
     *
     * @param attribute 枚举对象
     * @return 枚举值（Integer），枚举为 null 时返回 null
     */
    @Override
    public Integer convertToDatabaseColumn(LedgerStatus attribute) {
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
    public LedgerStatus convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }
        return LedgerStatus.fromValue(dbData);
    }
}
```

---

### 4.2 Entity 中使用转换器

```java
/**
 * 账本实体
 */
@Data
@Entity
@Table(name = "ledger")
public class Ledger extends BaseEntity {

    /**
     * 账本状态: 1=进行中, 2=部分缴费, 3=已结清, 4=赊账中, 5=已关闭
     */
    @Column(name = "ledger_status", nullable = false)
    @Convert(converter = LedgerStatusConverter.class)  // 使用转换器
    private LedgerStatus ledgerStatus = LedgerStatus.IN_PROGRESS;
}
```

**关键点**:
- ✅ 使用 `@Convert(converter = XxxConverter.class)` 注解
- ✅ `@Converter(autoApply = false)` 不自动应用，需要显式声明
- ✅ 数据库字段类型为 `TINYINT` 或 `INT`

---

## 5. MyBatis 枚举映射

### 5.1 MyBatis 类型处理器

**位置**: `com.coreledger.config.typehandler`

**命名规则**: `枚举名 + TypeHandler`

**示例**: `LedgerStatusTypeHandler.java`

```java
package com.coreledger.config.typehandler;

import com.coreledger.enums.LedgerStatus;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * LedgerStatus 枚举 MyBatis 类型处理器
 *
 * <p>用于 MyBatis 查询结果中 TINYINT 类型与 LedgerStatus 枚举的相互转换</p>
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@MappedTypes(LedgerStatus.class)
@MappedJdbcTypes(JdbcType.TINYINT)
public class LedgerStatusTypeHandler extends BaseTypeHandler<LedgerStatus> {

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
    public void setNonNullParameter(PreparedStatement ps, int i, LedgerStatus parameter, JdbcType jdbcType)
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
    public LedgerStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : LedgerStatus.fromValue(value);
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
    public LedgerStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int value = rs.getInt(columnIndex);
        return rs.wasNull() ? null : LedgerStatus.fromValue(value);
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
    public LedgerStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int value = cs.getInt(columnIndex);
        return cs.wasNull() ? null : LedgerStatus.fromValue(value);
    }
}
```

---

### 5.2 MyBatis 配置

**application.yml**:
```yaml
mybatis:
  type-handlers-package: com.coreledger.config.typehandler
  configuration:
    map-underscore-to-camel-case: true
```

---

### 5.3 Mapper XML 中使用

**方式一：自动识别（推荐）**

配置 `type-handlers-package` 后，MyBatis 会自动识别类型处理器：

```xml
<select id="selectLedgerById" resultType="com.coreledger.entity.Ledger">
    SELECT
        id,
        customer_id,
        total_amount,
        ledger_status  <!-- 自动转换为 LedgerStatus 枚举 -->
    FROM ledger
    WHERE id = #{id}
</select>
```

**方式二：显式指定**

```xml
<resultMap id="LedgerResultMap" type="com.coreledger.entity.Ledger">
    <id property="id" column="id"/>
    <result property="customerId" column="customer_id"/>
    <result property="ledgerStatus" column="ledger_status"
            typeHandler="com.coreledger.config.typehandler.LedgerStatusTypeHandler"/>
</resultMap>

<select id="selectLedgerById" resultMap="LedgerResultMap">
    SELECT * FROM ledger WHERE id = #{id}
</select>
```

---

## 6. JSON 序列化

### 6.1 Jackson 配置

枚举使用 `@JsonValue` 注解后，JSON 序列化时会自动返回 `value` 字段：

```java
@Getter
public enum LedgerStatus {
    IN_PROGRESS(1, "进行中");

    @JsonValue  // 序列化时返回 value
    private final int value;
    private final String description;
}
```

**序列化结果**:
```json
{
  "ledgerStatus": 1
}
```

---

### 6.2 VO 中提供描述字段

为了前端显示，VO 中通常需要提供描述字段：

```java
@Data
@Schema(description = "账本列表响应")
public class LedgerListVO {

    @Schema(description = "账本状态: 1=进行中, 2=部分缴费, 3=已结清, 4=赊账中, 5=已关闭")
    private Integer ledgerStatus;

    @Schema(description = "状态描述")
    private String ledgerStatusDesc;
}
```

**转换器**:
```java
@Mapper(componentModel = "spring")
public interface LedgerConverter {

    @Mapping(target = "ledgerStatus", source = "ledgerStatus.value")
    @Mapping(target = "ledgerStatusDesc", source = "ledgerStatus.description")
    LedgerListVO toListVO(LedgerDTO dto);
}
```

**JSON 响应**:
```json
{
  "ledgerStatus": 1,
  "ledgerStatusDesc": "进行中"
}
```

---

## 7. 所有枚举类型汇总

### 7.1 系统中的枚举

| 枚举类 | 数据库字段类型 | 值范围 | 说明 |
|-------|--------------|--------|------|
| `LedgerStatus` | TINYINT | 1-5 | 账本状态 |
| `PaymentMethod` | TINYINT | 1-4 | 支付方式 |
| `Gender` | TINYINT | 0-2 | 性别 |
| `UserRole` | TINYINT | 0-1 | 用户角色 |

---

### 7.2 LedgerStatus（账本状态）

| 枚举值 | value | description |
|-------|-------|-------------|
| IN_PROGRESS | 1 | 进行中 |
| PARTIAL | 2 | 部分缴费 |
| CLEARED | 3 | 已结清 |
| ON_CREDIT | 4 | 赊账中 |
| CLOSED | 5 | 已关闭 |

---

### 7.3 PaymentMethod（支付方式）

| 枚举值 | value | description |
|-------|-------|-------------|
| CASH | 1 | 现金 |
| WECHAT | 2 | 微信支付 |
| ALIPAY | 3 | 支付宝 |
| BANK_TRANSFER | 4 | 银行转账 |

---

### 7.4 Gender（性别）

| 枚举值 | value | description |
|-------|-------|-------------|
| UNKNOWN | 0 | 未知 |
| MALE | 1 | 男 |
| FEMALE | 2 | 女 |

---

### 7.5 UserRole（用户角色）

| 枚举值 | value | description |
|-------|-------|-------------|
| USER | 0 | 普通用户 |
| ADMIN | 1 | 管理员 |

---

## 8. 完整实现清单

对于每个枚举类型，需要创建以下文件：

### 8.1 枚举类
- ✅ 位置：`com.coreledger.enums`
- ✅ 文件：`LedgerStatus.java`
- ✅ 内容：枚举定义 + `fromValue()` 方法 + 业务方法

### 8.2 JPA 转换器
- ✅ 位置：`com.coreledger.config.converter`
- ✅ 文件：`LedgerStatusConverter.java`
- ✅ 继承：`AttributeConverter<LedgerStatus, Integer>`
- ✅ 注解：`@Converter(autoApply = false)`

### 8.3 MyBatis 类型处理器
- ✅ 位置：`com.coreledger.config.typehandler`
- ✅ 文件：`LedgerStatusTypeHandler.java`
- ✅ 继承：`BaseTypeHandler<LedgerStatus>`
- ✅ 注解：`@MappedTypes` 和 `@MappedJdbcTypes`

### 8.4 数据库表定义
- ✅ 字段类型：`TINYINT`
- ✅ 注释：完整的枚举值说明
- ✅ 默认值：合理的默认枚举值

---

## 9. 测试验证

### 9.1 JPA 测试

```java
@Test
void testJpaEnumMapping() {
    Ledger ledger = new Ledger();
    ledger.setLedgerStatus(LedgerStatus.IN_PROGRESS);
    ledger = ledgerRepository.save(ledger);

    Ledger found = ledgerRepository.findById(ledger.getId()).orElseThrow();
    assertEquals(LedgerStatus.IN_PROGRESS, found.getLedgerStatus());
}
```

### 9.2 MyBatis 测试

```java
@Test
void testMyBatisEnumMapping() {
    Ledger ledger = ledgerMapper.selectById(1L);
    assertNotNull(ledger.getLedgerStatus());
    assertEquals(LedgerStatus.class, ledger.getLedgerStatus().getClass());
}
```

### 9.3 JSON 序列化测试

```java
@Test
void testJsonSerialization() throws Exception {
    LedgerListVO vo = new LedgerListVO();
    vo.setLedgerStatus(1);
    vo.setLedgerStatusDesc("进行中");

    String json = objectMapper.writeValueAsString(vo);
    assertTrue(json.contains("\"ledgerStatus\":1"));
    assertTrue(json.contains("\"ledgerStatusDesc\":\"进行中\""));
}
```

---

## 10. 常见问题

### Q1: 为什么不使用 JPA 的 `@Enumerated` 注解？

**A**: `@Enumerated` 有两种模式：
- `ORDINAL`：使用枚举序号（0, 1, 2...），顺序变化会导致数据错乱
- `STRING`：使用枚举名称，占用空间大，性能差

我们使用自定义 `value` 字段，既保证稳定性，又保证性能。

---

### Q2: 为什么 `fromValue()` 找不到时返回默认值而不是抛异常？

**A**: 为了系统健壮性，避免因数据问题导致系统崩溃。生产环境中可能存在旧数据或异常数据，返回默认值可以保证系统继续运行。

---

### Q3: 如何处理枚举值的变更？

**A**:
1. **新增枚举值**：直接添加，不影响现有数据
2. **删除枚举值**：先迁移数据，再删除枚举定义
3. **修改 value**：禁止修改，会导致数据混乱

---

**文档版本**: 1.0.0
**最后更新**: 2025-11-24
**维护团队**: Core Ledger Team
