# Core Ledger 编码规范文档

## 1. 通用规范

### 1.1 字符编码
- 所有源代码文件使用 **UTF-8** 编码
- 所有文本文件（包括 SQL、XML、YAML）使用 **UTF-8** 编码

### 1.2 缩进与格式
- 使用 **4 个空格** 缩进，禁止使用 Tab
- 每行代码不超过 **120** 个字符
- 左大括号 `{` 不换行，右大括号 `}` 单独一行

### 1.3 文件命名
- Java 类文件：大驼峰命名法（PascalCase），如 `LedgerService.java`
- 配置文件：小写字母 + 连字符，如 `application-dev.yml`
- SQL 文件：版本号 + 下划线 + 描述，如 `V1.0.0__init_schema.sql`

---

## 2. 命名规范

### 2.1 包命名
- 全部小写字母
- 使用单数形式
- 示例：`com.coreledger.service`，`com.coreledger.entity`

### 2.2 类命名

| 类型 | 规则 | 示例 |
|------|------|------|
| 实体类 (Entity) | 名词，大驼峰 | `Ledger`, `Customer` |
| DTO | 名词 + DTO | `LedgerDTO`, `CreateLedgerDTO` |
| VO | 名词 + VO | `LedgerListVO`, `LedgerDetailVO` |
| Service | 名词 + Service | `LedgerService` |
| Controller | 名词 + Controller | `LedgerController` |
| Repository | 名词 + Repository | `LedgerRepository` |
| Mapper (MyBatis) | 名词 + Mapper | `LedgerMapper` |
| Converter | 名词 + Converter | `LedgerConverter` |
| Exception | 描述 + Exception | `NotFoundException`, `BusinessException` |
| Config | 描述 + Config | `JacksonConfig`, `OpenApiConfig` |
| 枚举类 | 名词，大驼峰 | `LedgerStatus`, `PaymentMethod` |

### 2.3 方法命名

| 方法类型 | 前缀 | 示例 |
|---------|------|------|
| 获取单个对象 | get | `getLedgerById(Long id)` |
| 获取列表 | list/get | `listActiveLedgers()` |
| 查询（MyBatis） | select | `selectLedgerWithCustomer()` |
| 新增 | create/add | `createLedger(CreateLedgerDTO dto)` |
| 修改 | update | `updateLedger(Long id, UpdateLedgerDTO dto)` |
| 删除 | delete/remove | `deleteLedger(Long id)` |
| 保存（Insert/Update） | save | `saveLedger(LedgerDTO dto)` |
| 判断 | is/has/can | `isActive()`, `canReceivePayment()` |
| 转换 | to/convert | `toDTO()`, `convertToEntity()` |

### 2.4 变量命名

#### 2.4.1 成员变量
- 使用小驼峰命名法（camelCase）
- 禁止使用单字符变量（除循环变量 i, j, k）
- Boolean 类型使用 `is/has/can` 前缀

```java
// ✅ 正确
private Long customerId;
private BigDecimal totalAmount;
private boolean isActive;

// ❌ 错误
private Long customer_id;  // 禁止下划线
private BigDecimal t;      // 禁止单字符
private boolean active;    // Boolean 应有前缀
```

#### 2.4.2 常量
- 全大写字母 + 下划线
- 使用 `static final` 修饰

```java
public static final int MAX_PAGE_SIZE = 100;
public static final String DEFAULT_CHARSET = "UTF-8";
```

#### 2.4.3 局部变量
- 使用小驼峰命名法
- 尽量简短但有意义

```java
// ✅ 正确
List<Ledger> ledgerList = ...;
BigDecimal remainingAmount = ...;

// ❌ 错误
List<Ledger> list = ...;  // 太笼统
BigDecimal ra = ...;      // 缩写不清晰
```

---

## 3. 注释规范

### 3.1 类注释

**规则**:
- 所有类必须添加 JavaDoc 注释
- 包含类的功能描述
- 包含 `@author` 和 `@since` 标签
- 使用中文描述

```java
/**
 * 账本业务服务类
 *
 * <p>提供账本的创建、查询、收款、赊账等核心业务功能</p>
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class LedgerService {
    // ...
}
```

### 3.2 字段注释

**规则**:
- 所有字段必须添加注释
- 使用 `/** */` 格式
- 使用中文描述
- 枚举字段需标注取值范围

```java
/** 主键ID */
private Long id;

/** 客户ID */
@Column(name = "customer_id", nullable = false)
private Long customerId;

/** 应收总金额 */
@Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
private BigDecimal totalAmount;

/** 账本状态: 1=进行中, 2=部分缴费, 3=已结清, 4=赊账中, 5=已关闭 */
@Column(name = "ledger_status", nullable = false)
@Convert(converter = LedgerStatusConverter.class)
private LedgerStatus ledgerStatus;
```

### 3.3 方法注释

**规则**:
- 所有 public 方法必须添加 JavaDoc 注释
- 包含方法功能描述
- 使用 `@param` 描述参数
- 使用 `@return` 描述返回值
- 使用 `@throws` 描述异常
- 使用中文描述

```java
/**
 * 创建账本
 *
 * @param dto 账本创建请求DTO
 * @return 创建成功的账本DTO
 * @throws NotFoundException 当客户不存在时抛出
 * @throws BusinessException 当业务校验失败时抛出
 */
@Transactional
public LedgerDTO createLedger(CreateLedgerDTO dto) {
    // ...
}
```

### 3.4 复杂逻辑注释

**规则**:
- 对于复杂的业务逻辑，添加块注释说明
- 使用 `//` 单行注释

```java
// 计算剩余应收金额
BigDecimal remainingAmount = ledger.getTotalAmount()
    .subtract(ledger.getPaidAmount())
    .subtract(ledger.getDiscountAmount());

// 根据剩余金额判断状态
if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
    // 已结清
    ledger.setLedgerStatus(LedgerStatus.CLEARED);
} else {
    // 部分缴费
    ledger.setLedgerStatus(LedgerStatus.PARTIAL);
}
```

---

## 4. Entity 注释规范

### 4.1 类注释

```java
/**
 * 账本实体
 *
 * <p>对应数据库表: ledger</p>
 * <p>账本状态流转规则请参考: {@link com.coreledger.enums.LedgerStatus}</p>
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Entity
@Table(name = "ledger")
@EqualsAndHashCode(callSuper = true)
public class Ledger extends BaseEntity {
    // ...
}
```

### 4.2 字段注释（必须）

```java
/** 客户ID */
@Column(name = "customer_id", nullable = false)
private Long customerId;

/**
 * 应收总金额
 * <p>所有明细项的金额总和</p>
 */
@Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
private BigDecimal totalAmount = BigDecimal.ZERO;

/**
 * 账本状态
 * <ul>
 *   <li>1 = IN_PROGRESS (进行中)</li>
 *   <li>2 = PARTIAL (部分缴费)</li>
 *   <li>3 = CLEARED (已结清)</li>
 *   <li>4 = ON_CREDIT (赊账中)</li>
 *   <li>5 = CLOSED (已关闭)</li>
 * </ul>
 */
@Column(name = "ledger_status", nullable = false)
@Convert(converter = LedgerStatusConverter.class)
private LedgerStatus ledgerStatus = LedgerStatus.IN_PROGRESS;
```

---

## 5. DTO/VO 注释规范

### 5.1 DTO 注释

```java
/**
 * 账本数据传输对象
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
public class LedgerDTO {

    /** 账本ID */
    private Long id;

    /** 客户ID */
    private Long customerId;

    /** 应收总金额 */
    private BigDecimal totalAmount;

    /** 账本状态 */
    private LedgerStatus ledgerStatus;
}
```

### 5.2 VO 注释（使用 Swagger）

```java
/**
 * 账本列表视图对象
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Schema(description = "账本列表响应")
public class LedgerListVO {

    @Schema(description = "账本ID")
    private Long id;

    @Schema(description = "客户姓名")
    private String customerName;

    @Schema(description = "应收金额")
    private BigDecimal totalAmount;

    @Schema(description = "账本状态: 1=进行中, 2=部分缴费, 3=已结清, 4=赊账中, 5=已关闭")
    private Integer ledgerStatus;

    @Schema(description = "状态描述")
    private String ledgerStatusDesc;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createInstant;
}
```

---

## 6. 枚举类注释规范

```java
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

    /** 枚举值（数据库存储） */
    @JsonValue
    private final int value;

    /** 描述 */
    private final String description;

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
}
```

---

## 7. Controller 注释规范

```java
/**
 * 账本管理控制器
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/ledgers")
@RequiredArgsConstructor
@Tag(name = "账本管理", description = "账本的创建、查询、收款、赊账等操作")
public class LedgerController {

    private final LedgerService ledgerService;

    /**
     * 查询主页面账本列表（活跃账单）
     *
     * @param customerName 客户姓名（模糊查询）
     * @param pageable 分页参数
     * @return 账本列表
     */
    @GetMapping("/active")
    @Operation(summary = "查询活跃账单", description = "查询主页面显示的账单（状态为进行中或部分缴费）")
    public Result<Page<LedgerListVO>> getActiveLedgers(
            @Parameter(description = "客户姓名") @RequestParam(required = false) String customerName,
            Pageable pageable) {
        return Result.success(ledgerService.listActiveLedgers(customerName, pageable));
    }
}
```

---

## 8. Service 注释规范

```java
/**
 * 账本业务服务类
 *
 * <p>提供账本的创建、查询、收款、赊账等核心业务功能</p>
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LedgerService {

    private final LedgerRepository ledgerRepository;
    private final LedgerMapper ledgerMapper;

    /**
     * 创建账本
     *
     * <p>业务规则:</p>
     * <ul>
     *   <li>客户必须存在</li>
     *   <li>至少包含一条明细</li>
     *   <li>自动计算总金额</li>
     *   <li>默认状态为 IN_PROGRESS</li>
     * </ul>
     *
     * @param dto 账本创建请求DTO
     * @return 创建成功的账本DTO
     * @throws NotFoundException 当客户不存在时抛出
     * @throws BusinessException 当明细为空时抛出
     */
    @Transactional
    public LedgerDTO createLedger(CreateLedgerDTO dto) {
        // ...
    }
}
```

---

## 9. 数据访问规范

### 9.1 单表条件查询

**强制约束**: 单表条件查询必须使用 **JPA + PredicateBuilder**

```java
// ✅ 正确：使用 PredicateBuilder
@Service
public class CustomerService {
    
    public Page<Customer> searchCustomers(String keyword, Long addressId, Pageable pageable) {
        Specification<Customer> spec = PredicateBuilder.<Customer>and()
            .equal("status", 1)
            .like(StrUtil::isNotBlank, "name", keyword)
            .equal("addressId", addressId)
            .build();
        
        return customerRepository.findAll(spec, pageable);
    }
}

// ❌ 错误：手写 JPQL
@Query("SELECT c FROM Customer c WHERE c.status = 1 AND c.name LIKE %:keyword%")
List<Customer> searchByKeyword(@Param("keyword") String keyword);

// ❌ 错误：使用 MyBatis 做单表查询
List<Customer> selectByCondition(CustomerQuery query);
```

**PredicateBuilder 优势**:
- 类型安全，编译期检查
- 自动处理 null 值
- 代码简洁，易于维护
- 支持动态条件组装

**详细用法**: 参考 [PredicateBuilder 使用指南](./PREDICATE_BUILDER_GUIDE.md)

### 9.2 复杂查询

**使用 MyBatis 的场景**:
- 多表联查
- 复杂统计查询
- 需要优化的 SQL

```java
// ✅ 正确：使用 MyBatis 做多表联查
@Mapper
public interface LedgerMapper {
    
    @Select("SELECT l.*, c.name as customer_name " +
            "FROM ledger l " +
            "LEFT JOIN customer c ON l.customer_id = c.id " +
            "WHERE l.status = 1")
    List<LedgerWithCustomerVO> selectLedgersWithCustomer();
}
```

---

## 10. 禁止事项

### 9.1 禁止的代码风格

```java
// ❌ 禁止：魔法数字
if (ledgerStatus == 1) {
    // ...
}

// ✅ 正确：使用枚举
if (ledgerStatus == LedgerStatus.IN_PROGRESS) {
    // ...
}

// ❌ 禁止：无意义的注释
// 获取ID
private Long id;

// ✅ 正确：有意义的注释
/** 主键ID */
private Long id;

// ❌ 禁止：空catch块
try {
    // ...
} catch (Exception e) {
    // 忽略异常
}

// ✅ 正确：记录异常
try {
    // ...
} catch (Exception e) {
    log.error("操作失败", e);
    throw new BusinessException("操作失败");
}
```

### 9.2 禁止的命名

```java
// ❌ 禁止：拼音命名
private String zhangsan;

// ❌ 禁止：下划线命名（非常量）
private String customer_name;

// ❌ 禁止：单字符变量（非循环）
BigDecimal t = ...;

// ❌ 禁止：过于简写
BigDecimal amt = ...;  // 应使用 amount
```

---

## 10. 代码示例（完整示例）

### 10.1 Entity 示例

```java
/**
 * 账本实体
 *
 * <p>对应数据库表: ledger</p>
 * <p>账本状态流转规则请参考: {@link com.coreledger.enums.LedgerStatus}</p>
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
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

    /**
     * 计算剩余应收金额
     *
     * @return 剩余应收金额
     */
    public BigDecimal getRemainingAmount() {
        return totalAmount.subtract(paidAmount).subtract(discountAmount);
    }
}
```

---

**文档版本**: 1.0.0
**最后更新**: 2025-11-24
**维护团队**: Core Ledger Team
