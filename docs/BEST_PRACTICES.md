# Core Ledger 开发最佳实践与约束

## 文档说明
本文档整合了所有开发规范和最佳实践，是团队开发的核心约束文档。

**创建日期**: 2025-11-26  
**维护团队**: Core Ledger Team  
**文档版本**: 2.0.0

---

## 🚨 强制约束（必须遵守）

### 1. 分层架构约束

```java
// 架构层次
Controller (VO) → Service (DTO) → Repository (Entity)

// ✅ 正确
@RestController
public class LedgerController {
    private final LedgerService ledgerService;  // Controller只依赖Service
    
    public Result<LedgerVO> get(Long id) {
        LedgerDTO dto = ledgerService.getById(id);
        return Result.success(converter.toVO(dto));
    }
}

// ❌ 禁止
@RestController
public class LedgerController {
    private final LedgerRepository repository;  // ❌ Controller直接访问Repository
}
```

**约束规则**:
- ❌ Controller不能直接访问Repository
- ❌ Controller不能包含业务逻辑
- ❌ Service不能依赖VO
- ✅ 层间使用MapStruct转换

---

### 2. JPA实体类约束

```java
// ✅ 正确写法
@Getter
@Setter
@ToString
@Entity
@Table(name = "customer")
public class Customer extends BaseEntity {
    
    /** 客户姓名 */
    @Column(name = "name", nullable = false)
    private String name;
    
    // 关联关系必须排除
    @OneToMany(mappedBy = "customerId", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Ledger> ledgers;
}

// ❌ 禁止写法
@Data  // ❌ 会导致懒加载和循环引用问题
@EqualsAndHashCode(callSuper = true)  // ❌ 会触发懒加载
@Entity
public class Customer extends BaseEntity {
    // ...
}
```

**约束规则**:
- ❌ **禁止使用 @Data**
- ❌ **禁止使用 @EqualsAndHashCode**
- ✅ 使用 @Getter + @Setter + @ToString
- ✅ 关联关系使用 @ToString.Exclude
- ✅ 继承 BaseEntity

**原因**: @Data包含equals/hashCode会导致懒加载触发、循环引用、性能问题

---

### 3. 数据查询约束

```java
// ✅ 单表条件查询 - 使用PredicateBuilder
@Service
public class CustomerService {
    public Page<Customer> search(String keyword, Pageable pageable) {
        Specification<Customer> spec = PredicateBuilder.<Customer>and()
            .equal("status", 1)
            .like(StrUtil::isNotBlank, "name", keyword)
            .build();
        return customerRepository.findAll(spec, pageable);
    }
}

// ❌ 单表查询禁止使用MyBatis
@Mapper
public interface CustomerMapper {
    List<Customer> selectByCondition(CustomerQuery query);  // ❌ 单表查询不用MyBatis
}

// ✅ 多表联查 - 使用MyBatis
@Mapper
public interface LedgerMapper {
    @Select("SELECT l.*, c.name FROM ledger l LEFT JOIN customer c ...")
    List<LedgerWithCustomerVO> selectWithCustomer();  // ✅ 多表联查用MyBatis
}
```

**约束规则**:
- ✅ **单表条件查询** → 强制使用 JPA + PredicateBuilder
- ✅ **多表联查/复杂统计** → 使用 MyBatis Mapper
- ✅ **写操作(CUD)** → 使用 JPA Repository
- ❌ **禁止单表查询使用MyBatis**

---

### 4. 异常处理约束

```java
// ✅ 正确：使用BusinessCode枚举
@Service
public class LedgerService {
    
    public LedgerDTO getById(Long id) {
        Ledger ledger = repository.findById(id)
            .orElseThrow(() -> new NotFoundException(BusinessCode.LEDGER_NOT_FOUND));
        return converter.toDTO(ledger);
    }
    
    public void receivePayment(Long id, BigDecimal amount) {
        // 使用BusinessCode枚举
        if (amount.compareTo(remaining) > 0) {
            throw new BusinessException(BusinessCode.LEDGER_PAYMENT_EXCEED);
        }
        
        // 如需自定义消息，传入第二个参数
        if (amount.compareTo(remaining) > 0) {
            throw new BusinessException(BusinessCode.LEDGER_PAYMENT_EXCEED, 
                String.format("支付金额%.2f超过应收金额%.2f", amount, remaining));
        }
    }
}

// ✅ Controller不捕获异常
@RestController
public class LedgerController {
    public Result<LedgerDTO> get(Long id) {
        return Result.success(ledgerService.getById(id));  // 不要try-catch
    }
}

// ❌ 禁止写法1：直接传字符串
@Service
public class LedgerService {
    public LedgerDTO getById(Long id) {
        Ledger ledger = repository.findById(id)
            .orElseThrow(() -> new NotFoundException("账本不存在"));  // ❌ 不要直接传字符串
        return converter.toDTO(ledger);
    }
}

// ❌ 禁止写法2：Controller捕获异常
@RestController
public class LedgerController {
    public Result<LedgerDTO> get(Long id) {
        try {
            return Result.success(ledgerService.getById(id));
        } catch (Exception e) {  // ❌ Controller不要捕获异常
            return Result.error("查询失败");
        }
    }
}
```

**约束规则**:
- ✅ **必须使用 BusinessCode 枚举**
- ✅ Service层抛出异常（NotFoundException/BusinessException）
- ❌ **禁止直接传字符串**
- ❌ Controller层不要try-catch
- ✅ 全局异常处理器统一处理

---

### 5. BusinessCode 错误码约束

**所有错误码定义在** `com.coreledger.enums.BusinessCode` 枚举中。

```java
// BusinessCode枚举结构
@Getter
public enum BusinessCode {
    // 客户模块 (1000-1999)
    CUSTOMER_NOT_FOUND(1001, "客户不存在"),
    CUSTOMER_PHONE_EXISTS(1002, "手机号已被注册"),
    
    // 账本模块 (2000-2999)
    LEDGER_NOT_FOUND(2001, "账本不存在"),
    LEDGER_STATUS_NOT_ALLOWED(2002, "账本状态不允许此操作"),
    LEDGER_PAYMENT_EXCEED(2003, "支付金额超过应收金额"),
    
    // 权限模块 (5000-5999)
    ADMIN_ONLY(5001, "该操作仅限管理员执行"),
    USER_CREDENTIALS_INVALID(5003, "用户名或密码错误");
    
    private final int code;
    private final String message;
}
```

**使用方式**:

```java
// ✅ 方式1：仅使用枚举（推荐）
throw new NotFoundException(BusinessCode.LEDGER_NOT_FOUND);
throw new BusinessException(BusinessCode.LEDGER_PAYMENT_EXCEED);

// ✅ 方式2：枚举 + 自定义消息（需要补充上下文）
throw new BusinessException(BusinessCode.LEDGER_PAYMENT_EXCEED,
    String.format("支付金额%.2f超过应收金额%.2f", amount, remaining));

throw new NotFoundException(BusinessCode.CUSTOMER_NOT_FOUND,
    "客户ID=" + customerId + "不存在");

// ❌ 禁止：直接传字符串
throw new NotFoundException("账本不存在");  // ❌ 错误
throw new BusinessException("支付金额超限");  // ❌ 错误
```

**约束规则**:
- ✅ **所有异常必须使用 BusinessCode 枚举**
- ✅ 使用枚举保证错误码统一
- ✅ 如需补充上下文信息，传入第二个参数
- ❌ **严禁直接传字符串**
- ✅ 新增业务错误码要在 BusinessCode 枚举中定义

**错误码范围划分**:
- 1000-1999: 客户模块
- 2000-2999: 账本模块
- 3000-3999: 商品模块
- 4000-4999: 支付模块
- 5000-5999: 权限模块
- 6000-6999: 地址模块
- 9000-9999: 系统错误

---

### 6. 枚举映射约束

**枚举映射3步配置法**：定义枚举 → 创建 JPA Converter → 创建 MyBatis TypeHandler

#### 第1步：定义枚举（必须实现 BaseEnum 接口）

```java
// ✅ 正确：实现 BaseEnum 接口
@Getter
public enum LedgerStatus implements BaseEnum {  // ← 必须实现 BaseEnum
    
    /** 进行中 */
    IN_PROGRESS(1, "进行中"),
    
    /** 部分缴费 */
    PARTIAL(2, "部分缴费"),
    
    /** 已结清 */
    CLEARED(3, "已结清");
    
    /** 枚举值（数据库存储） */
    @JsonValue  // ← JSON序列化返回value
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

// ❌ 错误：没有实现 BaseEnum
@Getter
public enum LedgerStatus {  // ❌ 缺少 implements BaseEnum
    IN_PROGRESS(1, "进行中");
    // ...
}
```

#### 第2步：创建 JPA Converter（继承 BaseEnumConverter）

```java
// ✅ 创建 JPA 转换器
package com.coreledger.config.converter;

import com.coreledger.enums.LedgerStatus;
import jakarta.persistence.Converter;

/**
 * 账本状态 JPA 转换器
 */
@Converter(autoApply = false)  // ← autoApply=false，需要手动指定
public class LedgerStatusConverter extends BaseEnumConverter<LedgerStatus> {
    
    public LedgerStatusConverter() {
        super(LedgerStatus.class);
    }
}
```

#### 第3步：创建 MyBatis TypeHandler（继承 BaseEnumTypeHandler）

```java
// ✅ 创建 MyBatis 类型处理器
package com.coreledger.config.typehandler;

import com.coreledger.enums.LedgerStatus;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

/**
 * 账本状态 MyBatis 类型处理器
 */
@MappedTypes(LedgerStatus.class)
@MappedJdbcTypes(JdbcType.TINYINT)
public class LedgerStatusTypeHandler extends BaseEnumTypeHandler<LedgerStatus> {
    
    public LedgerStatusTypeHandler() {
        super(LedgerStatus.class);
    }
}
```

#### 第4步：在 Entity 中使用

```java
// ✅ 在 JPA Entity 中使用
@Entity
@Table(name = "ledger")
public class Ledger extends BaseEntity {
    
    /** 账本状态 */
    @Column(name = "ledger_status", nullable = false)
    @Convert(converter = LedgerStatusConverter.class)  // ← 指定转换器
    private LedgerStatus ledgerStatus;
}

// ❌ 禁止写法1：使用 @Enumerated
@Entity
public class Ledger {
    @Enumerated(EnumType.STRING)  // ❌ 浪费空间，不统一
    private LedgerStatus status;
}

// ❌ 禁止写法2：使用 ORDINAL（不安全）
@Entity
public class Ledger {
    @Enumerated(EnumType.ORDINAL)  // ❌ 枚举顺序改变会导致数据错乱
    private LedgerStatus status;
}
```

#### application.yml 配置（MyBatis）

```yaml
mybatis:
  type-handlers-package: com.coreledger.config.typehandler  # 自动扫描TypeHandler
```

---

**约束规则**:
- ✅ **枚举必须实现 BaseEnum 接口**
- ✅ **必须创建 JPA Converter（继承 BaseEnumConverter）**
- ✅ **必须创建 MyBatis TypeHandler（继承 BaseEnumTypeHandler）**
- ✅ 数据库使用 `TINYINT` 或 `INT` 存储
- ✅ 枚举使用 `@JsonValue` 标注value字段
- ✅ Entity 中使用 `@Convert(converter=XxxConverter.class)`
- ❌ **禁止使用 @Enumerated**

**通用基类**:
- `BaseEnum` 接口：定义 `getValue()` 和 `getDescription()` 方法
- `BaseEnumConverter<E>` 抽象类：JPA 通用转换器
- `BaseEnumTypeHandler<E>` 抽象类：MyBatis 通用类型处理器

---

### 7. Controller接口设计约束

```java
// ✅ 正确：参数不超过3个
@GetMapping("/{id}")
public Result<LedgerVO> getById(@PathVariable Long id) {
    return Result.success(ledgerService.getById(id));
}

@GetMapping("/search")
public Result<Page<LedgerVO>> search(
    @RequestParam String keyword,
    @RequestParam(required = false) Integer status,
    Pageable pageable
) {
    // 最多3个参数
}

// ✅ 正确：超过3个参数，封装成VO
@PostMapping("/search")
public Result<Page<LedgerVO>> search(@RequestBody @Valid LedgerSearchVO searchVO) {
    // 4个以上参数必须封装
}

// ❌ 禁止：参数超过3个
@GetMapping("/search")
public Result<Page<LedgerVO>> search(
    @RequestParam String keyword,
    @RequestParam Integer status,
    @RequestParam Long customerId,
    @RequestParam LocalDate startDate,  // ❌ 第4个参数，应该封装成VO
    @RequestParam LocalDate endDate
) {
    // ...
}
```

**HTTP方法约束**:
```java
// ✅ 查询使用 GET
@GetMapping("/{id}")
public Result<LedgerVO> getById(@PathVariable Long id) { }

@GetMapping("/list")
public Result<List<LedgerVO>> list() { }

// ✅ 新增使用 POST
@PostMapping
public Result<LedgerDTO> create(@RequestBody @Valid CreateLedgerDTO dto) { }

// ✅ 修改使用 PUT
@PutMapping("/{id}")
public Result<LedgerDTO> update(
    @PathVariable Long id,
    @RequestBody @Valid UpdateLedgerDTO dto
) { }

// ✅ 删除使用 DELETE
@DeleteMapping("/{id}")
public Result<Void> delete(@PathVariable Long id) { }

// ❌ 禁止：方法使用错误
@PostMapping("/{id}")  // ❌ 查询不要用POST
public Result<LedgerVO> getById(@PathVariable Long id) { }

@GetMapping  // ❌ 新增不要用GET
public Result<LedgerDTO> create(@RequestParam String name) { }
```

**约束规则**:
- ✅ **接口参数不超过3个**
- ✅ **超过3个参数必须封装成VO**
- ✅ **查询使用 GET**
- ✅ **新增使用 POST**
- ✅ **修改使用 PUT**
- ✅ **删除使用 DELETE**
- ❌ 禁止GET请求使用@RequestBody
- ❌ 禁止路径参数过多（建议最多2个）

---

### 8. 枚举强制使用约束 ⚠️ 最高优先级

**所有状态、类型、分类相关字段，必须使用枚举，严禁使用基本类型！**

```java
// ✅ 正确：状态、类型、分类使用枚举
@Entity
public class Ledger {
    
    /** 账本状态 */
    @Convert(converter = LedgerStatusConverter.class)
    private LedgerStatus ledgerStatus;  // ✅ 使用枚举
    
    /** 支付方式 */
    @Convert(converter = PaymentMethodConverter.class)
    private PaymentMethod paymentMethod;  // ✅ 使用枚举
    
    /** 商品分类 */
    @Convert(converter = ProductCategoryConverter.class)
    private ProductCategory productCategory;  // ✅ 使用枚举
}

// ❌ 禁止：使用基本类型
@Entity
public class Ledger {
    private Integer status;  // ❌ 禁止！必须用枚举 LedgerStatus
    private String type;     // ❌ 禁止！必须用枚举
    private int category;    // ❌ 禁止！必须用枚举
}

// ❌ 禁止：使用字符串常量
public class Constants {
    public static final String STATUS_ACTIVE = "active";     // ❌ 应该用枚举
    public static final String TYPE_NORMAL = "normal";       // ❌ 应该用枚举
    public static final int CATEGORY_FOOD = 1;               // ❌ 应该用枚举
}
```

**必须使用枚举的字段类型**:
- ✅ **状态类（status）**: 订单状态、账本状态、用户状态等
- ✅ **类型类（type）**: 支付类型、商品类型、用户类型等
- ✅ **分类类（category）**: 商品分类、账单分类等
- ✅ **方式类（method）**: 支付方式、配送方式等
- ✅ **性别（gender）**: 男、女、未知
- ✅ **角色（role）**: 用户角色、权限角色等

**如何识别需要使用枚举的字段**:
1. 字段值是**有限固定的几个选项**
2. 字段名包含 **status、type、category、method、role、gender** 等关键词
3. 业务上有明确的**状态流转或分类规则**
4. 如果不确定是否需要用枚举，**必须先询问确认**

**约束规则**:
- ✅ **所有状态、类型、分类字段必须使用枚举（最高优先级）**
- ✅ 枚举必须实现 BaseEnum 接口
- ✅ 枚举必须创建 Converter 和 TypeHandler
- ❌ **严禁使用 Integer、String、int 等基本类型表示状态/类型/分类**
- ❌ **严禁使用字符串常量或数字常量**
- ⚠️ **不确定是否需要枚举时，必须先询问确认**

---

### 9. 安全比较约束

**使用 `equals()` 方法进行比较，避免空指针异常**

```java
// ✅ 正确：枚举比较（常量在前）
if (LedgerStatus.IN_PROGRESS.equals(status)) {
    // 即使 status 为 null 也不会报空指针
}

// ✅ 正确：使用 Objects.equals()
if (Objects.equals(status, LedgerStatus.IN_PROGRESS)) {
    // 两个参数都可能为null
}

// ✅ 正确：对象比较
if (Objects.equals(customer.getName(), "张三")) {
    // 安全的字符串比较
}

// ❌ 禁止：直接使用 ==（容易空指针）
if (status == LedgerStatus.IN_PROGRESS) {  // ❌ status为null会报错
    // ...
}

// ❌ 禁止：可能空指针的equals
if (status.equals(LedgerStatus.IN_PROGRESS)) {  // ❌ status为null会报空指针
    // ...
}

// ❌ 禁止：字符串直接比较
if (name == "张三") {  // ❌ 字符串用==不对
    // ...
}
```

**约束规则**:
- ✅ **枚举比较使用常量.equals(变量)**
- ✅ **对象比较使用 Objects.equals(a, b)**
- ✅ **字符串比较使用 equals() 或 Objects.equals()**
- ❌ **禁止枚举使用 ==（基本类型除外）**
- ❌ **禁止变量.equals(常量)（容易空指针）**

---

### 10. MapStruct配置约束

```java
// ✅ 所有Mapper继承统一配置
@Mapper(config = BeanMapperConf.class)
public interface LedgerConverter {
    LedgerDTO toDTO(Ledger entity);
    Ledger toEntity(LedgerDTO dto);
    
    @Mapping(target = "statusDesc", source = "status.description")
    LedgerVO toVO(LedgerDTO dto);
}

// ❌ 禁止重复配置
@Mapper(
    componentModel = "spring",  // ❌ 不要重复配置
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CustomerConverter {
    // ...
}
```

**统一配置** (`BeanMapperConf.java`):
```java
@MapperConfig(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    builder = @Builder(disableBuilder = true)
)
public interface BeanMapperConf {
}
```

**约束规则**:
- ✅ 所有Mapper继承 `BeanMapperConf`
- ❌ 禁止在每个Mapper重复配置
- ✅ 使用 `@Mapping` 处理特殊映射

---

### 11. Flyway 数据库迁移文件规范

#### 文件命名规范

```
格式: V10.0.0.{yyyymmddhhmm}__{描述}.sql

示例:
V10.0.0.202511261430__create_sys_address_table.sql
V10.0.0.202511261445__add_ledger_status_column.sql
V10.0.0.202511271000__init_product_category_data.sql
```

**命名规则**:
- ✅ 固定前缀: `V10.0.0.`
- ✅ 时间戳: `{yyyymmddhhmm}` (年月日时分，如 202511261430)
- ✅ 双下划线: `__`
- ✅ 描述: 使用小写英文，下划线分隔，简明扼要
- ❌ 禁止使用中文
- ❌ 禁止使用空格

#### 建表语句格式化规范

**必须使用列对齐格式**:

```sql
-- ✅ 正确：列对齐格式
CREATE TABLE `sys_address`
(
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `parent_id`      BIGINT       NOT NULL DEFAULT 0 COMMENT '父级ID (0表示顶级)',
    `name`           VARCHAR(100) NOT NULL COMMENT '地址名称',
    `level`          TINYINT      NOT NULL COMMENT '层级: 1=省, 2=市, 3=区县, 4=镇/乡, 5=村',
    `merger_name`    VARCHAR(500)          DEFAULT NULL COMMENT '全称路径',
    `memo`           VARCHAR(255)          DEFAULT NULL COMMENT '备注',
    `status`         INT          NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
    `create_instant` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_instant` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `version`        INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_level` (`level`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='行政区划地址库';

-- ❌ 禁止：不对齐的格式
CREATE TABLE `sys_address` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父级ID',
    `name` VARCHAR(100) NOT NULL COMMENT '地址名称',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**格式化规则**:
1. ✅ **字段名对齐**: 所有字段名左对齐
2. ✅ **数据类型对齐**: 数据类型在同一列
3. ✅ **约束对齐**: NOT NULL、DEFAULT 等约束对齐
4. ✅ **COMMENT 对齐**: 所有注释在同一列
5. ✅ **必须添加中文注释**: 所有字段、表必须有 COMMENT
6. ✅ **索引单独一行**: PRIMARY KEY 和 KEY 独立成行
7. ✅ **ENGINE 配置**: 统一使用 InnoDB、utf8mb4、utf8mb4_unicode_ci

#### 标准字段（必须包含）

所有业务表必须包含以下基础字段：

```sql
-- 审计字段（必须）
`create_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`modify_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
`version`        INT      NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',

-- 逻辑删除（可选）
`status`         INT      NOT NULL DEFAULT 1 COMMENT '状态: 1=有效, 0=删除',
```

#### ALTER 语句规范

```sql
-- ✅ 正确：添加列
ALTER TABLE `ledger`
    ADD COLUMN `payment_method` TINYINT NOT NULL DEFAULT 1 COMMENT '支付方式: 1=现金, 2=微信, 3=支付宝' AFTER `ledger_status`;

-- ✅ 正确：添加索引
ALTER TABLE `ledger`
    ADD KEY `idx_customer_id_status` (`customer_id`, `ledger_status`);

-- ✅ 正确：修改列
ALTER TABLE `ledger`
    MODIFY COLUMN `memo` VARCHAR(500) DEFAULT NULL COMMENT '备注（修改长度）';
```

#### 数据初始化规范

```sql
-- ✅ 正确：插入初始数据
INSERT INTO `product_category` (`id`, `name`, `parent_id`, `level`, `status`)
VALUES (1, '食品', 0, 1, 1),
       (2, '日用品', 0, 1, 1),
       (3, '水果', 1, 2, 1),
       (4, '蔬菜', 1, 2, 1);
```

**约束规则**:
- ✅ **必须使用列对齐格式**
- ✅ **所有字段必须有中文 COMMENT**
- ✅ **表必须有 COMMENT**
- ✅ **必须包含审计字段**: create_instant, modify_instant, version
- ✅ **使用反引号**: 表名、字段名用反引号包裹
- ✅ **字符集**: utf8mb4 + utf8mb4_unicode_ci
- ✅ **引擎**: InnoDB
- ❌ **禁止删除字段**: 使用 status 标记删除
- ❌ **禁止直接修改表结构**: 必须通过 Flyway 迁移文件

#### 文件组织

```
src/main/resources/db/migration/
├── V10.0.0.202511201000__init_database_schema.sql
├── V10.0.0.202511201100__create_sys_user_table.sql
├── V10.0.0.202511201200__create_customer_table.sql
├── V10.0.0.202511201300__create_ledger_table.sql
└── V10.0.0.202511261430__create_sys_address_table.sql
```

---

## 📝 命名规范

### 类命名

| 类型 | 规则 | 示例 |
|------|------|------|
| Entity | 名词 | `Ledger`, `Customer` |
| DTO | 名词 + DTO | `LedgerDTO`, `CreateLedgerDTO` |
| VO | 名词 + VO | `LedgerListVO`, `LedgerDetailVO` |
| Service | 名词 + Service | `LedgerService` |
| Controller | 名词 + Controller | `LedgerController` |
| Repository | 名词 + Repository | `LedgerRepository` |
| Mapper (MyBatis) | 名词 + Mapper | `LedgerMapper` |
| Converter (MapStruct) | 名词 + Converter | `LedgerConverter` |
| Enum | 名词 | `LedgerStatus`, `PaymentMethod` |

### 方法命名

| 方法类型 | 前缀 | 示例 |
|---------|------|------|
| 获取单个 | get | `getLedgerById(Long id)` |
| 获取列表 | list | `listActiveLedgers()` |
| 新增 | create | `createLedger(CreateLedgerDTO dto)` |
| 修改 | update | `updateLedger(Long id, UpdateLedgerDTO dto)` |
| 删除 | delete | `deleteLedger(Long id)` |
| 判断 | is/has/can | `isActive()`, `canReceivePayment()` |
| 转换 | to | `toDTO()`, `toEntity()` |

---

## 💡 最佳实践

### 1. 注释规范

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
    
    private final CustomerRepository customerRepository;
    
    /**
     * 创建账本
     *
     * @param dto 账本创建请求DTO
     * @return 创建成功的账本DTO
     * @throws NotFoundException 当客户不存在时抛出 (BusinessCode.CUSTOMER_NOT_FOUND)
     * @throws BusinessException 当业务校验失败时抛出
     */
    @Transactional
    public LedgerDTO createLedger(CreateLedgerDTO dto) {
        // 校验客户是否存在
        Customer customer = customerRepository.findById(dto.getCustomerId())
            .orElseThrow(() -> new NotFoundException(BusinessCode.CUSTOMER_NOT_FOUND));
        
        // 校验明细
        if (CollUtil.isEmpty(dto.getItems())) {
            throw new BusinessException(BusinessCode.LEDGER_ITEMS_EMPTY);
        }
        
        // 业务逻辑...
    }
}
```

**规范**:
- ✅ 所有类必须有JavaDoc注释
- ✅ 所有public方法必须有注释
- ✅ 所有字段必须有注释
- ✅ 使用中文描述

### 2. 事务管理

```java
@Service
@Transactional(readOnly = true)  // ✅ 默认只读
public class LedgerService {
    
    @Transactional  // ✅ 写操作覆盖为读写
    public LedgerDTO createLedger(CreateLedgerDTO dto) {
        // ...
    }
    
    public LedgerDTO getById(Long id) {
        // 只读事务
    }
}
```

### 3. 对象转换

```java
// ✅ 使用MapStruct
@Mapper(config = BeanMapperConf.class)
public interface LedgerConverter {
    LedgerDTO toDTO(Ledger entity);
    List<LedgerDTO> toDTOList(List<Ledger> entities);
}

// ❌ 禁止手动转换
public LedgerDTO toDTO(Ledger entity) {
    LedgerDTO dto = new LedgerDTO();
    dto.setId(entity.getId());
    dto.setCustomerId(entity.getCustomerId());
    // ... 手动设置每个字段
    return dto;
}
```

### 4. 参数校验

```java
// DTO中定义校验规则
@Data
public class CreateLedgerDTO {
    
    @NotNull(message = "客户ID不能为空")
    private Long customerId;
    
    @NotEmpty(message = "账本明细不能为空")
    @Valid
    private List<LedgerItemDTO> items;
    
    @Size(max = 255, message = "备注长度不能超过255个字符")
    private String memo;
}

// Controller使用@Valid触发校验
@PostMapping
public Result<LedgerDTO> create(@Valid @RequestBody CreateLedgerDTO dto) {
    return Result.success(ledgerService.createLedger(dto));
}
```

---

## 🔍 代码审查清单

创建新功能时，请检查：

### Entity
- [ ] ✅ 使用 `@Getter` + `@Setter` + `@ToString`
- [ ] ❌ 不使用 `@Data`
- [ ] ❌ 不使用 `@EqualsAndHashCode`
- [ ] ✅ 继承 `BaseEntity`
- [ ] ✅ 关联关系使用 `@ToString.Exclude`
- [ ] ✅ **状态、类型、分类字段必须使用枚举（最高优先级）**
- [ ] ❌ **禁止用Integer/String表示状态/类型/分类**
- [ ] ✅ 枚举使用 `@Convert(converter=XxxConverter.class)`
- [ ] ✅ 所有字段有注释

### DTO/VO
- [ ] ✅ 使用 `@Data`（DTO/VO可以用）
- [ ] ✅ 添加参数校验注解
- [ ] ✅ 所有字段有注释

### Service
- [ ] ✅ 类上标注 `@Transactional(readOnly = true)`
- [ ] ✅ 写操作方法标注 `@Transactional`
- [ ] ✅ 单表查询使用 PredicateBuilder
- [ ] ✅ 多表查询使用 MyBatis
- [ ] ✅ **抛出异常必须使用 BusinessCode 枚举**
- [ ] ❌ **禁止直接传字符串**
- [ ] ✅ 不返回null，找不到抛NotFoundException
- [ ] ✅ 所有public方法有JavaDoc注释

### Controller
- [ ] ✅ 返回 `Result<T>` 统一响应
- [ ] ✅ 不要try-catch
- [ ] ✅ 使用 `@Valid` 校验参数
- [ ] ✅ **接口参数不超过3个**
- [ ] ✅ **超过3个参数必须封装成VO**
- [ ] ✅ **查询用GET、新增用POST、修改用PUT、删除用DELETE**
- [ ] ✅ 添加 Swagger 注解

### Converter (MapStruct)
- [ ] ✅ 继承 `BeanMapperConf` 配置
- [ ] ✅ 不要重复配置

### Enum
- [ ] ✅ **实现 BaseEnum 接口**
- [ ] ✅ 使用 `@JsonValue` 标注value字段
- [ ] ✅ 提供 `fromValue(int)` 静态方法
- [ ] ✅ **创建 JPA Converter（继承 BaseEnumConverter）**
- [ ] ✅ **创建 MyBatis TypeHandler（继承 BaseEnumTypeHandler）**
- [ ] ✅ 所有枚举值有JavaDoc注释

### Flyway 迁移文件
- [ ] ✅ **文件命名：V10.0.0.{yyyymmddhhmm}__{描述}.sql**
- [ ] ✅ **建表语句使用列对齐格式**
- [ ] ✅ **所有字段必须有中文 COMMENT**
- [ ] ✅ **表必须有 COMMENT**
- [ ] ✅ **包含审计字段：create_instant, modify_instant, version**
- [ ] ✅ **使用反引号包裹表名和字段名**
- [ ] ✅ **字符集：utf8mb4 + utf8mb4_unicode_ci**
- [ ] ✅ **引擎：InnoDB**
- [ ] ❌ **禁止删除字段（使用 status 标记）**

---

## ❌ 禁止事项

### 代码风格
```java
// ❌ 魔法数字
if (status == 1) { }

// ✅ 使用枚举
if ( LedgerStatus.IN_PROGRESS.equals(status)) { }

// ❌ 空catch块
try { } catch (Exception e) { }

// ✅ 记录日志或抛出异常
try { } catch (Exception e) {
    log.error("操作失败", e);
    throw new BusinessException(BusinessCode.BUSINESS_ERROR, "操作失败");
}

// ❌ 异常直接传字符串
throw new NotFoundException("账本不存在");  // ❌ 禁止
throw new BusinessException("支付金额超限");  // ❌ 禁止

// ✅ 异常必须使用BusinessCode枚举
throw new NotFoundException(BusinessCode.LEDGER_NOT_FOUND);  // ✅ 正确
throw new BusinessException(BusinessCode.LEDGER_PAYMENT_EXCEED);  // ✅ 正确
```

### 命名规范
```java
// ❌ 拼音命名
private String zhangsan;

// ❌ 下划线命名（非常量）
private String customer_name;

// ❌ 单字符变量（非循环）
BigDecimal t = ...;

// ✅ 有意义的英文命名
private String customerName;
```

### 枚举使用（最高优先级）
```java
// ❌ 致命错误：状态/类型/分类使用基本类型
@Entity
public class Ledger {
    private Integer status;      // ❌ 禁止！必须用枚举 LedgerStatus
    private String type;         // ❌ 禁止！必须用枚举
    private int category;        // ❌ 禁止！必须用枚举
    private String paymentMethod; // ❌ 禁止！必须用枚举 PaymentMethod
}

// ❌ 使用字符串常量
public class Constants {
    public static final String STATUS_ACTIVE = "active";  // ❌ 应该用枚举
    public static final int TYPE_NORMAL = 1;             // ❌ 应该用枚举
}

// ❌ 枚举未实现 BaseEnum
public enum LedgerStatus {  // ❌ 缺少 implements BaseEnum
    IN_PROGRESS(1, "进行中");
}

// ❌ 使用 @Enumerated 注解
@Entity
public class Ledger {
    @Enumerated(EnumType.STRING)  // ❌ 禁止使用
    private LedgerStatus status;
    
    @Enumerated(EnumType.ORDINAL)  // ❌ 禁止使用
    private LedgerStatus status;
}

// ❌ 未创建 Converter 和 TypeHandler
// 枚举定义后必须创建对应的转换器

// ✅ 正确写法
public enum LedgerStatus implements BaseEnum {  // ✅ 实现 BaseEnum
    IN_PROGRESS(1, "进行中"),
    PARTIAL(2, "部分缴费");
    
    @JsonValue
    private final int value;
    private final String description;
}

@Entity
public class Ledger {
    @Convert(converter = LedgerStatusConverter.class)  // ✅ 使用转换器
    private LedgerStatus status;
}
```

### 接口设计
```java
// ❌ 参数超过3个
@GetMapping("/search")
public Result<List<LedgerVO>> search(
    @RequestParam String keyword,
    @RequestParam Integer status,
    @RequestParam Long customerId,
    @RequestParam LocalDate startDate,  // ❌ 超过3个，应封装成VO
    @RequestParam LocalDate endDate
) { }

// ❌ HTTP方法使用错误
@PostMapping("/{id}")  // ❌ 查询不要用POST
public Result<LedgerVO> getById(@PathVariable Long id) { }

@GetMapping("/create")  // ❌ 新增不要用GET
public Result<LedgerDTO> create(@RequestParam String name) { }

// ✅ 正确写法
@PostMapping("/search")
public Result<List<LedgerVO>> search(@RequestBody @Valid LedgerSearchVO searchVO) { }

@GetMapping("/{id}")  // ✅ 查询用GET
public Result<LedgerVO> getById(@PathVariable Long id) { }

@PostMapping  // ✅ 新增用POST
public Result<LedgerDTO> create(@RequestBody @Valid CreateLedgerDTO dto) { }
```

### 对象比较
```java
// ❌ 可能空指针的比较
if (status == LedgerStatus.IN_PROGRESS) {  // ❌ status为null会报错
    // ...
}

if (status.equals(LedgerStatus.IN_PROGRESS)) {  // ❌ status为null会空指针
    // ...
}

if (name == "张三") {  // ❌ 字符串用==不对
    // ...
}

// ✅ 正确写法
if (LedgerStatus.IN_PROGRESS.equals(status)) {  // ✅ 常量在前
    // ...
}

if (Objects.equals(status, LedgerStatus.IN_PROGRESS)) {  // ✅ 使用Objects.equals()
    // ...
}

if (Objects.equals(name, "张三")) {  // ✅ 安全的字符串比较
    // ...
}
```

### Flyway 数据库迁移
```sql
-- ❌ 文件命名错误
V1.0__create_table.sql  -- ❌ 版本号格式错误
V10.0.0.20251126__create_table.sql  -- ❌ 缺少时分
create_ledger_table.sql  -- ❌ 没有版本号
V10.0.0.202511261430__创建账本表.sql  -- ❌ 使用中文描述

-- ✅ 正确命名
V10.0.0.202511261430__create_ledger_table.sql

-- ❌ 不对齐的建表语句
CREATE TABLE `ledger` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `customer_id` BIGINT NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB;  -- ❌ 缺少注释、不对齐、缺少字符集

-- ✅ 正确做法：使用纵向对齐的格式化方式
CREATE TABLE `sys_address`
(
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `parent_id`      BIGINT       NOT NULL DEFAULT 0 COMMENT '父级ID (0表示顶级)',
    `name`           VARCHAR(100) NOT NULL COMMENT '地址名称',
    `level`          TINYINT      NOT NULL COMMENT '层级: 1=省, 2=市, 3=区县, 4=镇/乡, 5=村',
    `merger_name`    VARCHAR(500)          DEFAULT NULL COMMENT '全称路径 (如: 广东省-深圳市-南山区-西丽街道-留仙村)',
    `memo`           VARCHAR(255)          DEFAULT NULL COMMENT '备注',
    `status`         INT          NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
    `create_instant` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_instant` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `version`        INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_level` (`level`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='行政区划地址库';

-- ❌ 缺少必要字段
CREATE TABLE `ledger` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name` VARCHAR(100) NOT NULL COMMENT '名称',
    PRIMARY KEY (`id`)
);  -- ❌ 缺少 create_instant, modify_instant, version

-- ❌ 直接删除字段
ALTER TABLE `ledger` DROP COLUMN `old_field`;  -- ❌ 禁止删除

-- ✅ 正确做法：使用 status 标记
UPDATE `ledger` SET `status` = 0 WHERE condition;

-- ❌ 状态字段使用数字常量
`status` TINYINT NOT NULL DEFAULT 1 COMMENT '1启用2禁用'  -- ❌ 应该详细说明

-- ✅ 正确：详细说明状态值含义
`status` INT NOT NULL DEFAULT 1 COMMENT '状态: 1=有效, 0=删除'
```

---

**文档维护**: 本文档是核心约束，必须严格遵守  
**最后更新**: 2025-11-26  
**维护团队**: Core Ledger Team
