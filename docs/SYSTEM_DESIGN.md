# Core Ledger 系统设计文档

## 目录
- [1. 系统概述](#1-系统概述)
- [2. 整体架构](#2-整体架构)
- [3. 数据库设计](#3-数据库设计)
- [4. 核心模块设计](#4-核心模块设计)
- [5. 技术方案](#5-技术方案)
- [6. 业务规则](#6-业务规则)

---

## 1. 系统概述

Core Ledger 是一个灵活的账本管理系统，支持客户管理、商品（SPU/SKU）管理和账单全生命周期管理。

### 1.1 核心功能
- **客户管理**：客户信息维护、欠款统计、客户类型管理
- **商品管理**：SPU/SKU架构、动态属性、灵活定价
- **账单管理**：开单、收款、赊账、优惠结清、状态流转

### 1.2 设计目标
- **灵活性**：支持各种类型的商品（动态属性）
- **可靠性**：严格的状态流转、数据校验
- **性能**：批量查询优化、索引设计
- **可维护性**：清晰的分层架构、完善的注释

---

## 2. 整体架构

### 2.1 分层架构

```
┌─────────────────────────────────────────┐
│         Controller Layer                 │  REST API接口层
│  (CustomerController, ProductController, │
│   LedgerController)                      │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│          Service Layer                   │  业务逻辑层
│  (CustomerService, ProductService,       │
│   LedgerService)                         │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│       Repository Layer                   │  数据访问层
│  JPA Repository (写操作、简单查询)       │
│  MyBatis Mapper (复杂统计查询)          │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│          Database Layer                  │  MySQL 数据库
│  (customer, product, ledger, etc.)       │
└─────────────────────────────────────────┘
```

### 2.2 技术栈

| 层次 | 技术选型 | 说明 |
|-----|---------|------|
| 框架 | Spring Boot 3.2.0 | 核心框架 |
| ORM | JPA + MyBatis | 双ORM策略 |
| 数据库 | MySQL 8.0+ | 关系型数据库 |
| 转换器 | MapStruct | DTO/Entity转换 |
| 文档 | Knife4j/Swagger | API文档 |
| 迁移 | Flyway | 数据库版本管理 |

---

## 3. 数据库设计

### 3.1 核心表结构

#### 3.1.1 客户表 (customer)
```sql
CREATE TABLE customer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,              -- 客户姓名
    phone VARCHAR(20) NOT NULL UNIQUE,      -- 手机号（唯一）
    gender TINYINT DEFAULT 1,               -- 性别
    address_id BIGINT,                      -- 地址ID（村级）
    address_detail VARCHAR(200),            -- 详细地址
    customer_type TINYINT DEFAULT 1,        -- 客户类型: 0=潜在, 1=活跃
    memo TEXT,                              -- 备注
    status TINYINT DEFAULT 1,               -- 状态: 0=删除, 1=有效
    version INT DEFAULT 0,                  -- 乐观锁版本号
    create_instant DATETIME,
    modify_instant DATETIME
);

CREATE INDEX idx_customer_phone ON customer(phone);
CREATE INDEX idx_customer_type ON customer(customer_type);
CREATE INDEX idx_customer_address ON customer(address_id);
```

**设计要点：**
- 手机号唯一索引，防止重复
- customer_type 枚举：潜在客户(0) / 活跃客户(1)
- 软删除设计（status字段）
- 乐观锁（version字段）

#### 3.1.2 商品表 (product) - SPU
```sql
CREATE TABLE product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_id BIGINT NOT NULL,            -- 分类ID
    name VARCHAR(100) NOT NULL,             -- 商品名称（SPU）
    image_url VARCHAR(500),                 -- 商品主图
    description TEXT,                       -- 商品描述
    unit VARCHAR(20) NOT NULL,              -- 基本单位
    location VARCHAR(100),                  -- 存放位置
    memo TEXT,                              -- 备注
    status TINYINT DEFAULT 1,
    version INT DEFAULT 0,
    create_instant DATETIME,
    modify_instant DATETIME
);

CREATE INDEX idx_product_category ON product(category_id);
CREATE INDEX idx_product_name ON product(name);
```

**设计要点：**
- SPU（Standard Product Unit）：商品基本信息
- 不存储价格（价格在SKU层）
- 支持图片和描述

#### 3.1.3 商品属性表 (product_attribute)
```sql
CREATE TABLE product_attribute (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,             -- 商品ID
    attr_name VARCHAR(50) NOT NULL,         -- 属性名（如：重量）
    attr_values JSON NOT NULL,              -- 属性值列表（如：["5斤","10斤"]）
    sort_order INT DEFAULT 0,               -- 排序
    status TINYINT DEFAULT 1,
    create_instant DATETIME,
    modify_instant DATETIME
);

CREATE INDEX idx_attr_product ON product_attribute(product_id);
```

**设计要点：**
- 动态属性设计，支持任意属性
- JSON存储属性值列表
- sort_order控制前端展示顺序

#### 3.1.4 商品SKU表 (product_sku)
```sql
CREATE TABLE product_sku (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,             -- 商品ID（SPU）
    sku_name VARCHAR(150) NOT NULL,         -- SKU名称（自动生成）
    attr_value_map JSON NOT NULL,           -- 属性值映射（如：{"重量":"5斤"}）
    price DECIMAL(10,2) NOT NULL,           -- 销售价格
    image_url VARCHAR(500),                 -- SKU图片
    sort_order INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    version INT DEFAULT 0,
    create_instant DATETIME,
    modify_instant DATETIME
);

CREATE INDEX idx_sku_product ON product_sku(product_id);
CREATE INDEX idx_sku_name ON product_sku(sku_name);
```

**设计要点：**
- SKU（Stock Keeping Unit）：具体规格的商品
- sku_name 自动生成：`商品名-属性值1-属性值2`
- attr_value_map 存储该SKU选择的属性组合
- 每个SKU独立定价

#### 3.1.5 账单表 (ledger)
```sql
CREATE TABLE ledger (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,            -- 客户ID
    total_amount DECIMAL(10,2) DEFAULT 0,   -- 应收总金额
    paid_amount DECIMAL(10,2) DEFAULT 0,    -- 实收金额
    discount_amount DECIMAL(10,2) DEFAULT 0,-- 优惠金额
    ledger_status TINYINT DEFAULT 1,        -- 账单状态
    memo TEXT,                              -- 备注
    status TINYINT DEFAULT 1,
    version INT DEFAULT 0,
    create_instant DATETIME,
    modify_instant DATETIME
);

CREATE INDEX idx_ledger_customer ON ledger(customer_id);
CREATE INDEX idx_ledger_status ON ledger(ledger_status);
CREATE INDEX idx_ledger_create ON ledger(create_instant);
```

**账单状态枚举 (ledger_status)：**
- `1` - IN_PROGRESS（进行中）：刚创建，未收款
- `2` - PARTIAL（部分缴费）：已收部分款项
- `3` - CLEARED（已结清）：全额收款或优惠结清
- `4` - ON_CREDIT（赊账中）：客户赊账
- `5` - CLOSED（已关闭）：取消的账单

#### 3.1.6 账单明细表 (ledger_item)
```sql
CREATE TABLE ledger_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ledger_id BIGINT NOT NULL,              -- 账单ID
    sku_id BIGINT,                          -- SKU ID
    product_id BIGINT NOT NULL,             -- 商品ID（SPU）
    product_name VARCHAR(100) NOT NULL,     -- 商品名称（冗余）
    sku_name VARCHAR(150),                  -- SKU名称（冗余）
    attr_value_map JSON,                    -- 属性值映射（冗余）
    price DECIMAL(10,2) NOT NULL,           -- 实际售价
    quantity INT NOT NULL DEFAULT 1,        -- 数量
    status TINYINT DEFAULT 1,
    create_instant DATETIME,
    modify_instant DATETIME
);

CREATE INDEX idx_item_ledger ON ledger_item(ledger_id);
CREATE INDEX idx_item_sku ON ledger_item(sku_id);
```

**设计要点：**
- 冗余存储商品信息，防止商品变更影响历史账单
- 支持实际售价与标准价格不同
- 小计金额 = price × quantity（计算字段）

#### 3.1.7 支付记录表 (payment_record)
```sql
CREATE TABLE payment_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ledger_id BIGINT NOT NULL,              -- 账单ID
    amount DECIMAL(10,2) NOT NULL,          -- 支付金额
    payment_method TINYINT NOT NULL,        -- 支付方式
    memo TEXT,                              -- 备注
    status TINYINT DEFAULT 1,
    create_instant DATETIME,
    modify_instant DATETIME
);

CREATE INDEX idx_payment_ledger ON payment_record(ledger_id);
```

**支付方式枚举 (payment_method)：**
- `1` - 现金
- `2` - 微信
- `3` - 支付宝
- `4` - 银行转账

### 3.2 ER图关系

```
┌──────────┐         ┌──────────┐         ┌──────────────┐
│ customer │1      N│  ledger  │1      N│ ledger_item  │
│          ├────────▶│          ├────────▶│              │
└──────────┘         └────┬─────┘         └──────┬───────┘
                          │                      │
                          │1                     │N
                          │                      │
                          │N                     │1
                     ┌────▼──────────┐     ┌─────▼──────┐
                     │payment_record │     │product_sku │
                     └───────────────┘     └─────┬──────┘
                                                 │N
                                                 │
                                                 │1
                                           ┌─────▼──────┐
                                           │  product   │
                                           │   (SPU)    │
                                           └─────┬──────┘
                                                 │1
                                                 │
                                                 │N
                                      ┌──────────▼──────────┐
                                      │product_attribute    │
                                      └─────────────────────┘
```

---

## 4. 核心模块设计

### 4.1 客户模块 (Customer Module)

#### 4.1.1 功能特性
- ✅ 客户CRUD操作
- ✅ 手机号唯一性校验
- ✅ 客户类型管理（活跃/潜在）
- ✅ 欠款统计（批量查询优化）
- ✅ 账单统计（进行中/部分缴费/赊账/已结清）
- ✅ 软删除（级联检查未结清账单）

#### 4.1.2 核心类
```
CustomerController.java      # REST接口层
CustomerService.java         # 业务逻辑层
CustomerConverter.java       # MapStruct转换器
CustomerRepository.java      # JPA Repository
CustomerMapper.java          # MyBatis Mapper（复杂统计）
CustomerMapper.xml           # MyBatis SQL映射
```

#### 4.1.3 欠款统计实现

**性能优化方案：**
1. 先用JPA分页查询客户列表（主查询）
2. 提取所有客户ID
3. 使用MyBatis单次批量查询所有欠款统计（N+1问题优化）
4. 合并统计数据到VO

**SQL示例（CustomerMapper.xml）：**
```xml
<select id="batchQueryDebtSummary">
    SELECT
        customer_id AS customerId,
        -- 欠款总额（进行中+部分缴费）
        SUM(CASE WHEN ledger_status IN (1, 2)
            THEN total_amount - paid_amount - discount_amount
            ELSE 0 END) AS debtAmount,
        -- 赊账总额
        SUM(CASE WHEN ledger_status = 4
            THEN total_amount - paid_amount
            ELSE 0 END) AS creditAmount,
        -- 活跃账单数
        COUNT(CASE WHEN ledger_status IN (1, 2) THEN 1 END) AS activeLedgerCount
    FROM ledger
    WHERE customer_id IN
    <foreach collection="customerIds" item="id" open="(" separator="," close=")">
        #{id}
    </foreach>
    AND status = 1
    GROUP BY customer_id
</select>
```

### 4.2 商品模块 (Product Module)

#### 4.2.1 SPU/SKU架构

**概念说明：**
- **SPU (Standard Product Unit)**：商品标准单位，如"红富士苹果"
- **SKU (Stock Keeping Unit)**：库存单位，如"红富士苹果-5斤-一级"

**设计优势：**
- 灵活支持多规格商品
- 动态属性，不受固定字段限制
- 每个SKU独立定价

#### 4.2.2 动态属性系统

**工作流程：**
```
1. 创建SPU（商品基本信息）
   POST /api/products
   { "name": "红富士苹果", "categoryId": 1, "unit": "斤" }

2. 设置属性（动态属性）
   POST /api/products/1/attributes
   {
     "attributes": [
       { "attrName": "重量", "attrValues": ["5斤", "10斤", "20斤"] },
       { "attrName": "等级", "attrValues": ["一级", "二级"] }
     ]
   }

3. 生成SKU（两种策略）
   POST /api/products/1/skus

   # 策略A：统一价格（笛卡尔积生成所有组合）
   {
     "priceStrategy": "UNIFORM",
     "uniformPrice": 50.00
   }
   # 结果：生成6个SKU（3*2）
   # - 红富士苹果-5斤-一级    50元
   # - 红富士苹果-5斤-二级    50元
   # - 红富士苹果-10斤-一级   50元
   # - 红富士苹果-10斤-二级   50元
   # - 红富士苹果-20斤-一级   50元
   # - 红富士苹果-20斤-二级   50元

   # 策略B：手动指定（只生成指定的SKU）
   {
     "priceStrategy": "MANUAL",
     "skus": [
       { "attrValueMap": {"重量":"5斤","等级":"一级"}, "price": 30 },
       { "attrValueMap": {"重量":"10斤","等级":"一级"}, "price": 55 }
     ]
   }
```

#### 4.2.3 笛卡尔积算法

```java
/**
 * 生成属性组合的笛卡尔积
 * 输入：[{"重量": ["5斤","10斤"]}, {"等级": ["一级","二级"]}]
 * 输出：[
 *   {"重量":"5斤", "等级":"一级"},
 *   {"重量":"5斤", "等级":"二级"},
 *   {"重量":"10斤", "等级":"一级"},
 *   {"重量":"10斤", "等级":"二级"}
 * ]
 */
private void generateCartesianProductRecursive(
    List<ProductAttribute> attributes,
    int index,
    Map<String, String> current,
    List<Map<String, String>> result
) {
    if (index == attributes.size()) {
        result.add(new HashMap<>(current));
        return;
    }

    ProductAttribute attribute = attributes.get(index);
    for (String value : attribute.getAttrValues()) {
        current.put(attribute.getAttrName(), value);
        generateCartesianProductRecursive(attributes, index + 1, current, result);
        current.remove(attribute.getAttrName());
    }
}
```

### 4.3 账单模块 (Ledger Module)

#### 4.3.1 状态流转图

```
                    ┌─────────────┐
                    │ IN_PROGRESS │  进行中（初始状态）
                    │    (1)      │
                    └──────┬──────┘
                           │
         ┌─────────────────┼─────────────────┐
         │                 │                 │
    收款部分          收款全额           标记赊账
         │                 │                 │
         ▼                 ▼                 ▼
   ┌──────────┐      ┌──────────┐    ┌──────────┐
   │ PARTIAL  │      │ CLEARED  │    │ON_CREDIT │
   │   (2)    │─────▶│   (3)    │    │   (4)    │
   └────┬─────┘ 收款  └──────────┘    └────┬─────┘
        │       全额      ▲                  │
        │                 │                  │
        │                 │ 收款全额          │
        └─────────────────┴──────────────────┘
                          │
                     优惠结清
                          │
                     ┌────▼────┐
                     │ CLOSED  │  已关闭（任意状态可关闭）
                     │   (5)   │
                     └─────────┘
```

#### 4.3.2 业务规则矩阵

| 操作 | IN_PROGRESS | PARTIAL | CLEARED | ON_CREDIT | CLOSED |
|-----|-------------|---------|---------|-----------|--------|
| 修改明细 | ✅ | ✅ | ❌ | ❌ | ❌ |
| 收款 | ✅ | ✅ | ❌ | ✅ | ❌ |
| 优惠结清 | ✅ | ✅ | ❌ | ✅ | ❌ |
| 赊账 | ✅ | ❌ | ❌ | ❌ | ❌ |
| 关闭 | ✅ | ✅ | ❌ | ✅ | ❌ |
| 删除 | ✅ | ✅ | ❌ | ✅ | ✅ |

**规则说明：**
- **修改明细**：只有未结清的账单可以修改，且只能修改价格和数量，不能修改商品
- **收款**：不允许超额收款，收款后自动更新状态
- **优惠结清**：抹零，直接结清账单
- **赊账**：只有进行中的账单可以转赊账
- **删除**：已结清的账单不允许删除（保留历史记录）

#### 4.3.3 核心算法

**1. 剩余应收金额计算：**
```java
public BigDecimal getRemainingAmount() {
    return totalAmount.subtract(paidAmount).subtract(discountAmount);
}
```

**2. 收款状态自动流转：**
```java
// 更新实收金额
ledger.setPaidAmount(ledger.getPaidAmount().add(paymentAmount));

// 自动判断状态
BigDecimal remainingAmount = ledger.getRemainingAmount();
if (remainingAmount.compareTo(BigDecimal.ZERO) == 0) {
    ledger.setLedgerStatus(LedgerStatus.CLEARED);  // 全额缴费
} else {
    ledger.setLedgerStatus(LedgerStatus.PARTIAL);  // 部分缴费
}
```

**3. 明细修改校验：**
```java
// 只允许进行中和部分缴费状态修改
if (!Arrays.asList(IN_PROGRESS, PARTIAL).contains(ledger.getLedgerStatus())) {
    throw new BusinessException("账单状态不允许修改明细");
}

// 只允许修改价格和数量，不允许修改商品（skuId不能改）
if (updateDTO.getPrice() != null) {
    item.setPrice(updateDTO.getPrice());
}
if (updateDTO.getQuantity() != null) {
    item.setQuantity(updateDTO.getQuantity());
}
```

---

## 5. 技术方案

### 5.1 双ORM策略

| 场景 | 技术选型 | 原因 |
|-----|---------|-----|
| 写操作 | JPA | 对象化操作，级联保存方便 |
| 简单查询 | JPA | 自动生成SQL，开发效率高 |
| 复杂统计 | MyBatis | SQL灵活，性能可控 |
| 批量查询 | MyBatis | IN查询优化，解决N+1问题 |

**示例：客户列表查询**
```java
// 1. JPA：主查询（分页）
Page<Customer> customerPage = customerRepository.findAll(spec, pageable);

// 2. MyBatis：批量统计（一次查询）
List<Long> customerIds = customerPage.getContent().stream()
    .map(Customer::getId).collect(Collectors.toList());
List<DebtSummary> debtSummaries = customerMapper.batchQueryDebtSummary(customerIds);

// 3. 合并数据
Map<Long, DebtSummary> debtMap = debtSummaries.stream()
    .collect(Collectors.toMap(DebtSummary::getCustomerId, Function.identity()));
voPage.getContent().forEach(vo -> {
    DebtSummary summary = debtMap.get(vo.getId());
    if (summary != null) {
        vo.setDebtAmount(summary.getDebtAmount());
    }
});
```

### 5.2 PredicateBuilder动态查询

```java
// 传统方式：需要写很多if判断
Specification<Customer> spec = (root, query, cb) -> {
    List<Predicate> predicates = new ArrayList<>();
    predicates.add(cb.equal(root.get("status"), 1));
    if (customerType != null) {
        predicates.add(cb.equal(root.get("customerType"), customerType));
    }
    if (StrUtil.isNotBlank(keyword)) {
        predicates.add(cb.or(
            cb.like(root.get("name"), "%" + keyword + "%"),
            cb.like(root.get("phone"), "%" + keyword + "%")
        ));
    }
    return cb.and(predicates.toArray(new Predicate[0]));
};

// PredicateBuilder方式：链式调用，简洁优雅
Specification<Customer> spec = PredicateBuilder.<Customer>and()
    .equal("status", 1)
    .equal(customerType != null, "customerType", CustomerType.fromValue(customerType))
    .or(StrUtil::isNotBlank, keyword,
        builder -> builder.like("name", keyword),
        builder -> builder.like("phone", keyword))
    .build();
```

### 5.3 MapStruct转换器

**自动生成转换代码，性能优于反射：**
```java
@Mapper(componentModel = "spring")
public interface CustomerConverter {
    // Entity → VO（带枚举描述）
    @Mapping(target = "genderDesc",
             expression = "java(entity.getGender().getDescription())")
    @Mapping(target = "customerType",
             expression = "java(entity.getCustomerType().getValue())")
    CustomerListVO toListVO(Customer entity);

    // 部分更新（只更新非null字段）
    @BeanMapping(nullValuePropertyMappingStrategy =
                 NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateCustomerDTO dto, @MappingTarget Customer entity);
}
```

### 5.4 枚举转换器

**三层枚举转换：**
```java
// 1. JPA转换器（数据库 ↔ Java对象）
@Converter(autoApply = false)
public class CustomerTypeConverter implements AttributeConverter<CustomerType, Integer> {
    @Override
    public Integer convertToDatabaseColumn(CustomerType attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public CustomerType convertToEntityAttribute(Integer dbData) {
        return CustomerType.fromValue(dbData);
    }
}

// 2. MyBatis TypeHandler（结果集 ↔ Java对象）
@MappedTypes(CustomerType.class)
public class CustomerTypeTypeHandler extends BaseTypeHandler<CustomerType> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i,
                                     CustomerType parameter, JdbcType jdbcType) {
        ps.setInt(i, parameter.getValue());
    }

    @Override
    public CustomerType getNullableResult(ResultSet rs, String columnName) {
        return CustomerType.fromValue(rs.getInt(columnName));
    }
}

// 3. JSON序列化（Java对象 ↔ JSON）
public enum CustomerType implements BaseEnum {
    POTENTIAL(0, "潜在客户"),
    ACTIVE(1, "活跃客户");

    @JsonValue  // 序列化时使用value字段
    private final int value;
    private final String description;
}
```

### 5.5 JSON转换器

**动态属性的JSON存储：**
```java
@Converter(autoApply = false)
public class JsonMapConverter implements AttributeConverter<Map<String, String>, String> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, String> attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("JSON转换失败", e);
        }
    }

    @Override
    public Map<String, String> convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData,
                new TypeReference<Map<String, String>>() {});
        } catch (JsonProcessingException e) {
            return new HashMap<>();
        }
    }
}
```

---

## 6. 业务规则

### 6.1 客户管理规则

| 规则 | 说明 | 实现位置 |
|-----|------|---------|
| 手机号唯一 | 同一手机号不能重复注册 | CustomerService.createCustomer() |
| 删除校验 | 有未结清账单的客户不能删除 | CustomerService.deleteCustomer() |
| 软删除 | 删除时设置status=0，不物理删除 | 所有delete方法 |
| 客户类型 | 新建默认为活跃客户(1) | CustomerService.createCustomer() |
| 地址限制 | 地址必须选择到村级(level=5) | TODO: 待实现 |

### 6.2 商品管理规则

| 规则 | 说明 | 实现位置 |
|-----|------|---------|
| SKU名称 | 自动生成：商品名-属性值1-属性值2 | ProductService.generateSkuName() |
| 属性排序 | sort_order控制展示顺序 | ProductAttribute.sortOrder |
| 笛卡尔积 | 统一价格策略生成所有组合 | ProductService.generateCartesianProduct() |
| 级联删除 | 删除商品时同时软删除所有SKU和属性 | ProductService.deleteProduct() |
| 删除校验 | 有未结清账单使用的商品不能删除 | TODO: 待实现 |

### 6.3 账单管理规则

| 规则 | 说明 | 实现位置 |
|-----|------|---------|
| 状态流转 | 严格按照状态机流转 | LedgerService各方法 |
| 明细修改限制 | 只能改价格/数量，不能改商品 | LedgerService.updateLedgerItems() |
| 超额校验 | 收款金额不能超过剩余应收 | LedgerService.receivePayment() |
| 已结清保护 | 已结清账单不能删除 | LedgerService.deleteLedger() |
| 明细不能为空 | 账单至少需要一个明细项 | LedgerService.updateLedgerItems() |
| 冗余存储 | 商品信息冗余到明细，防止历史数据受影响 | LedgerItem实体设计 |

### 6.4 数据完整性规则

| 规则 | 说明 | 实现方式 |
|-----|------|---------|
| 乐观锁 | 防止并发修改冲突 | @Version字段 |
| 非空约束 | 关键字段NOT NULL | 数据库约束 |
| 唯一约束 | 手机号UNIQUE | 数据库索引 |
| 外键逻辑 | 不使用数据库外键，应用层校验 | Service层校验 |
| 软删除 | 所有表使用status字段 | 统一规范 |

---

## 附录

### A. 枚举定义

**CustomerType（客户类型）：**
```java
POTENTIAL(0, "潜在客户")
ACTIVE(1, "活跃客户")
```

**LedgerStatus（账单状态）：**
```java
IN_PROGRESS(1, "进行中")
PARTIAL(2, "部分缴费")
CLEARED(3, "已结清")
ON_CREDIT(4, "赊账中")
CLOSED(5, "已关闭")
```

**PaymentMethod（支付方式）：**
```java
CASH(1, "现金")
WECHAT(2, "微信")
ALIPAY(3, "支付宝")
BANK_TRANSFER(4, "银行转账")
```

**Gender（性别）：**
```java
MALE(1, "男")
FEMALE(2, "女")
UNKNOWN(0, "未知")
```

### B. 索引设计

**关键索引：**
```sql
-- 客户模块
CREATE INDEX idx_customer_phone ON customer(phone);
CREATE INDEX idx_customer_type ON customer(customer_type);
CREATE INDEX idx_customer_address ON customer(address_id);

-- 商品模块
CREATE INDEX idx_product_category ON product(category_id);
CREATE INDEX idx_product_name ON product(name);
CREATE INDEX idx_sku_product ON product_sku(product_id);
CREATE INDEX idx_sku_name ON product_sku(sku_name);

-- 账单模块
CREATE INDEX idx_ledger_customer ON ledger(customer_id);
CREATE INDEX idx_ledger_status ON ledger(ledger_status);
CREATE INDEX idx_ledger_create ON ledger(create_instant);
CREATE INDEX idx_item_ledger ON ledger_item(ledger_id);
CREATE INDEX idx_payment_ledger ON payment_record(ledger_id);
```

### C. 性能优化点

1. **批量查询优化**：使用MyBatis IN查询，避免N+1问题
2. **索引覆盖**：查询条件字段都有索引
3. **分页查询**：所有列表接口都支持分页
4. **冗余设计**：账单明细冗余商品信息，减少JOIN
5. **懒加载**：账单明细不查询时不加载

---

**文档版本：** v1.0
**最后更新：** 2025-01-25
**维护者：** Core Ledger Team
