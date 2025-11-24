# Core Ledger 架构设计文档

## 1. 总体架构

Core Ledger 采用**分层架构**设计，基于 Spring Boot 3.x 单体应用模式。

```
┌─────────────────────────────────────────────────────────────┐
│                     Presentation Layer                      │
│                         (表现层)                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Controller  │  │  Exception   │  │   Knife4j    │      │
│  │   (接口层)    │  │   Handler    │  │  (API文档)   │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│         │ VO (Value Object)                                 │
└─────────┼─────────────────────────────────────────────────┘
          │
┌─────────┼─────────────────────────────────────────────────┐
│         │          Business Layer                          │
│         │             (业务层)                              │
│         ▼ DTO (Data Transfer Object)                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │   Service    │  │  Converter   │  │   Validator  │     │
│  │  (业务逻辑)   │  │ (DTO转换器)   │  │  (业务校验)   │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
│         │ Domain Object                                     │
└─────────┼─────────────────────────────────────────────────┘
          │
┌─────────┼─────────────────────────────────────────────────┐
│         │         Persistence Layer                        │
│         │            (持久层)                               │
│         ▼ Domain Entity                                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │ JPA Repository│ │ MyBatis Mapper│ │   Entity     │     │
│  │  (写/简单查询)│ │  (复杂查询)    │  │  (实体定义)   │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
│         │                  │                                │
└─────────┼──────────────────┼────────────────────────────────┘
          │                  │
┌─────────┴──────────────────┴────────────────────────────────┐
│                      Database Layer                          │
│                        MySQL 8.0                             │
│                   (由 Flyway 管理版本)                        │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. 分层职责

### 2.1 Presentation Layer (表现层)

**职责**: 处理 HTTP 请求，参数校验，返回响应

**组件**:
- **Controller**: RESTful API 接口
- **VO (Value Object)**: 视图对象，用于与前端交互
- **Exception Handler**: 全局异常处理

**约束**:
- ✅ Controller 只能调用 Service
- ✅ Controller 与前端使用 VO 交互
- ✅ Controller 与 Service 之间使用 DTO 交互
- ❌ Controller 不能直接访问 Repository
- ❌ Controller 不能包含业务逻辑

---

### 2.2 Business Layer (业务层)

**职责**: 核心业务逻辑，事务管理

**组件**:
- **Service**: 业务服务
- **DTO (Data Transfer Object)**: 数据传输对象
- **Converter/Mapper**: DTO ↔ Domain 转换（使用 MapStruct）
- **Validator**: 业务规则校验

**约束**:
- ✅ Service 调用 Repository (JPA/MyBatis)
- ✅ Service 之间使用 DTO 交互
- ✅ Service 与 Repository 之间使用 Domain Entity 交互
- ✅ Service 方法使用 `@Transactional` 管理事务
- ❌ Service 不能依赖 VO

---

### 2.3 Persistence Layer (持久层)

**职责**: 数据持久化，数据访问

**组件**:
- **JPA Repository**: 用于增删改和简单查询
- **MyBatis Mapper**: 用于复杂查询、联表查询、动态 SQL
- **Entity (Domain)**: JPA 实体，对应数据库表

**约束**:
- ✅ Entity 使用 JPA 注解定义
- ✅ 写操作（CUD）使用 JPA Repository
- ✅ **单表条件查询必须使用 JPA + PredicateBuilder**
- ✅ 复杂查询（多表联查、复杂统计）使用 MyBatis Mapper
- ❌ Repository 不能包含业务逻辑

**PredicateBuilder 使用规范**:
```java
// 单表条件查询示例
Specification<Customer> spec = PredicateBuilder.<Customer>and()
    .equal("status", 1)                    // 状态筛选
    .like(StrUtil::isNotBlank, "name", keyword)  // 姓名模糊查询
    .in("addressId", addressIds)           // 地址范围查询
    .build();

Page<Customer> customers = customerRepository.findAll(spec, pageable);
```

---

## 3. 数据对象分层

### 3.1 VO (Value Object) - 表现层对象

**位置**: `com.coreledger.vo`

**用途**: 与前端交互，API 请求/响应

**示例**:
```java
@Data
@Schema(description = "账本列表响应")
public class LedgerListVO {
    @Schema(description = "账本ID")
    private Long id;

    @Schema(description = "客户姓名")
    private String customerName;

    @Schema(description = "应收金额")
    private BigDecimal totalAmount;

    @Schema(description = "实收金额")
    private BigDecimal paidAmount;

    @Schema(description = "账本状态: 1=进行中, 2=部分缴费, 3=已结清, 4=赊账中, 5=已关闭")
    private Integer ledgerStatus;

    @Schema(description = "状态描述")
    private String ledgerStatusDesc;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createInstant;
}
```

**规范**:
- ✅ 使用 `@Schema` 注解添加 Swagger 文档
- ✅ 使用 `@JsonFormat` 格式化日期
- ✅ 枚举类型使用 `Integer` + 描述字段
- ✅ 字段名使用驼峰命名
- ✅ 所有字段必须有中文注释

---

### 3.2 DTO (Data Transfer Object) - 业务层对象

**位置**: `com.coreledger.dto`

**用途**: 业务层之间传递数据

**示例**:
```java
@Data
public class LedgerDTO {
    /** 账本ID */
    private Long id;

    /** 客户ID */
    private Long customerId;

    /** 应收总金额 */
    private BigDecimal totalAmount;

    /** 实收金额 */
    private BigDecimal paidAmount;

    /** 优惠金额 */
    private BigDecimal discountAmount;

    /** 账本状态 */
    private LedgerStatus ledgerStatus;

    /** 备注 */
    private String memo;

    /** 账本明细列表 */
    private List<LedgerItemDTO> items;
}
```

**规范**:
- ✅ 枚举类型使用枚举类（LedgerStatus）
- ✅ 包含业务相关的嵌套对象
- ✅ 使用 JavaDoc 注释
- ❌ 不包含 JPA 注解

---

### 3.3 Entity (Domain) - 持久层对象

**位置**: `com.coreledger.entity`

**用途**: 与数据库表映射

**示例**:
```java
@Data
@Entity
@Table(name = "ledger")
@EqualsAndHashCode(callSuper = true)
public class Ledger extends BaseEntity {

    /** 客户ID */
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    /** 应收总金额 */
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    /** 实收金额 */
    @Column(name = "paid_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    /** 优惠金额 */
    @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    /** 账本状态: 1=进行中, 2=部分缴费, 3=已结清, 4=赊账中, 5=已关闭 */
    @Column(name = "ledger_status", nullable = false)
    @Convert(converter = LedgerStatusConverter.class)
    private LedgerStatus ledgerStatus = LedgerStatus.IN_PROGRESS;

    /** 账本明细 */
    @OneToMany(mappedBy = "ledgerId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LedgerItem> items = new ArrayList<>();
}
```

**规范**:
- ✅ 使用 JPA 注解
- ✅ 枚举字段使用 `@Convert` 进行类型转换
- ✅ 继承 `BaseEntity`
- ✅ 所有字段必须有 JavaDoc 注释

---

## 4. 枚举映射策略

### 4.1 数据库存储

**原则**: 数据库使用 `TINYINT` 或 `INT` 存储枚举值

**示例**:
```sql
`ledger_status` TINYINT NOT NULL DEFAULT 1 COMMENT '账本状态: 1=进行中, 2=部分缴费, 3=已结清, 4=赊账中, 5=已关闭'
```

---

### 4.2 JPA 枚举转换器

**位置**: `com.coreledger.config.converter`

**实现**:
```java
@Converter(autoApply = false)
public class LedgerStatusConverter implements AttributeConverter<LedgerStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(LedgerStatus attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public LedgerStatus convertToEntityAttribute(Integer dbData) {
        return dbData == null ? null : LedgerStatus.fromValue(dbData);
    }
}
```

**使用**:
```java
@Column(name = "ledger_status")
@Convert(converter = LedgerStatusConverter.class)
private LedgerStatus ledgerStatus;
```

---

### 4.3 MyBatis 类型处理器

**位置**: `com.coreledger.config.typehandler`

**实现**:
```java
@MappedTypes(LedgerStatus.class)
@MappedJdbcTypes(JdbcType.TINYINT)
public class LedgerStatusTypeHandler extends BaseTypeHandler<LedgerStatus> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, LedgerStatus parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getValue());
    }

    @Override
    public LedgerStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : LedgerStatus.fromValue(value);
    }

    @Override
    public LedgerStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int value = rs.getInt(columnIndex);
        return rs.wasNull() ? null : LedgerStatus.fromValue(value);
    }

    @Override
    public LedgerStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int value = cs.getInt(columnIndex);
        return cs.wasNull() ? null : LedgerStatus.fromValue(value);
    }
}
```

**配置** (application.yml):
```yaml
mybatis:
  type-handlers-package: com.coreledger.config.typehandler
```

---

### 4.4 JSON 序列化

**枚举定义**:
```java
@Getter
public enum LedgerStatus {
    IN_PROGRESS(1, "进行中"),
    PARTIAL(2, "部分缴费"),
    CLEARED(3, "已结清"),
    ON_CREDIT(4, "赊账中"),
    CLOSED(5, "已关闭");

    @JsonValue  // JSON 序列化时返回 value
    private final int value;
    private final String description;
}
```

**效果**:
- API 响应: `{"ledgerStatus": 1}`
- 前端显示: 使用描述字段 `ledgerStatusDesc: "进行中"`

---

## 5. 对象转换

### 5.1 MapStruct 转换器

**位置**: `com.coreledger.converter`

**示例**:
```java
@Mapper(componentModel = "spring")
public interface LedgerConverter {

    /**
     * Entity → DTO
     */
    LedgerDTO toDTO(Ledger entity);

    /**
     * DTO → Entity
     */
    Ledger toEntity(LedgerDTO dto);

    /**
     * DTO → VO (带状态描述)
     */
    @Mapping(target = "ledgerStatusDesc", expression = "java(dto.getLedgerStatus().getDescription())")
    LedgerListVO toListVO(LedgerDTO dto);

    List<LedgerListVO> toListVO(List<LedgerDTO> dtoList);
}
```

**使用**:
```java
@Service
@RequiredArgsConstructor
public class LedgerService {
    private final LedgerRepository ledgerRepository;
    private final LedgerConverter ledgerConverter;

    public LedgerDTO getLedgerById(Long id) {
        Ledger entity = ledgerRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("账本不存在"));
        return ledgerConverter.toDTO(entity);
    }
}
```

---

## 6. ORM 策略

### 6.1 JPA Repository (写操作)

**用途**:
- ✅ 增删改操作（CUD）
- ✅ 简单查询（findById, findByXxx）
- ✅ 分页查询（使用 Spring Data JPA）

**示例**:
```java
public interface LedgerRepository extends JpaRepository<Ledger, Long> {

    // 简单查询
    Optional<Ledger> findByIdAndStatus(Long id, Integer status);

    // 分页查询（主页面-活跃账单）
    Page<Ledger> findByLedgerStatusInAndStatus(
        List<LedgerStatus> statuses,
        Integer status,
        Pageable pageable
    );
}
```

---

### 6.2 MyBatis Mapper (复杂查询)

**用途**:
- ✅ 联表查询
- ✅ 动态 SQL
- ✅ 报表统计
- ✅ 复杂条件查询

**示例**:
```java
@Mapper
public interface LedgerMapper {

    /**
     * 查询客户账本列表（含客户信息）
     */
    List<LedgerDetailVO> selectLedgerWithCustomer(@Param("customerId") Long customerId);

    /**
     * 统计客户欠款总额
     */
    BigDecimal sumDebtByCustomer(@Param("customerId") Long customerId);
}
```

**XML**:
```xml
<select id="selectLedgerWithCustomer" resultType="com.coreledger.vo.LedgerDetailVO">
    SELECT
        l.id,
        l.total_amount,
        l.paid_amount,
        l.ledger_status,
        c.name AS customer_name,
        c.phone AS customer_phone
    FROM ledger l
    LEFT JOIN customer c ON l.customer_id = c.id
    WHERE l.customer_id = #{customerId}
      AND l.status = 1
    ORDER BY l.create_instant DESC
</select>
```

---

## 7. 异常处理

### 7.1 自定义异常

**位置**: `com.coreledger.exception`

```java
public class BusinessException extends RuntimeException {
    private final String code;

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }
}

public class NotFoundException extends BusinessException {
    public NotFoundException(String message) {
        super("NOT_FOUND", message);
    }
}
```

---

### 7.2 全局异常处理

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public Result<?> handleNotFound(NotFoundException e) {
        return Result.error(404, e.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusiness(BusinessException e) {
        return Result.error(400, e.getMessage());
    }
}
```

---

## 8. 统一响应格式

```java
@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;
    private Long timestamp;

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }

    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }
}
```

---

## 9. 事务管理

### 9.1 事务注解

```java
@Service
@Transactional(readOnly = true)  // 默认只读
public class LedgerService {

    @Transactional  // 覆盖为读写事务
    public LedgerDTO createLedger(CreateLedgerDTO dto) {
        // 业务逻辑
    }
}
```

---

### 9.2 事务传播行为

- **REQUIRED** (默认): 如果当前存在事务，则加入；否则创建新事务
- **REQUIRES_NEW**: 总是创建新事务，挂起当前事务
- **NESTED**: 嵌套事务

---

## 10. 目录结构

```
src/main/java/com/coreledger/
├── config/                    # 配置类
│   ├── converter/             # JPA 枚举转换器
│   ├── typehandler/           # MyBatis 类型处理器
│   ├── JacksonConfig.java
│   └── OpenApiConfig.java
├── controller/                # 控制器层
│   ├── LedgerController.java
│   └── CustomerController.java
├── service/                   # 业务层
│   ├── LedgerService.java
│   └── CustomerService.java
├── repository/                # JPA Repository
│   ├── LedgerRepository.java
│   └── CustomerRepository.java
├── mapper/                    # MyBatis Mapper 接口
│   ├── LedgerMapper.java
│   └── CustomerMapper.java
├── entity/                    # 实体类 (Domain)
│   ├── BaseEntity.java
│   ├── Ledger.java
│   └── Customer.java
├── dto/                       # 数据传输对象
│   ├── LedgerDTO.java
│   └── CreateLedgerDTO.java
├── vo/                        # 视图对象
│   ├── LedgerListVO.java
│   └── LedgerDetailVO.java
├── converter/                 # MapStruct 转换器
│   ├── LedgerConverter.java
│   └── CustomerConverter.java
├── enums/                     # 枚举类
│   ├── LedgerStatus.java
│   ├── PaymentMethod.java
│   └── Gender.java
├── exception/                 # 自定义异常
│   ├── BusinessException.java
│   └── NotFoundException.java
└── CoreLedgerApplication.java

src/main/resources/
├── db/migration/              # Flyway 脚本
│   └── V1.0.0__init_schema.sql
├── mapper/                    # MyBatis XML
│   ├── LedgerMapper.xml
│   └── CustomerMapper.xml
└── application.yml
```

---

**文档版本**: 1.0.0
**最后更新**: 2025-11-24
**维护团队**: Core Ledger Team
