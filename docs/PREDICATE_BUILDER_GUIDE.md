# PredicateBuilder 使用指南

## 文档说明
本文档详细说明如何使用 PredicateBuilder 进行 JPA 单表条件查询。

**创建日期**: 2025-11-24  
**维护团队**: Core Ledger Team  
**文档版本**: 1.0.0

---

## 1. 设计原则

### ✅ 必须使用 PredicateBuilder 的场景
- **单表条件查询**（带筛选条件的查询）
- **动态条件查询**（根据参数动态组装查询条件）
- **分页查询**（配合 Pageable 使用）

### ❌ 不使用 PredicateBuilder 的场景
- **多表联查**（使用 MyBatis Mapper）
- **复杂统计查询**（使用 MyBatis Mapper）
- **简单的单条件查询**（可直接使用 Repository 方法）

---

## 2. 基础用法

### 2.1 简单等值查询

```java
@Service
public class CustomerService {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    /**
     * 根据状态查询客户
     */
    public List<Customer> findByStatus(Integer status) {
        Specification<Customer> spec = PredicateBuilder.<Customer>and()
            .equal("status", status)
            .build();
        
        return customerRepository.findAll(spec);
    }
}
```

### 2.2 多条件 AND 查询

```java
/**
 * 根据多个条件查询客户
 */
public Page<Customer> searchCustomers(String keyword, Long addressId, Pageable pageable) {
    Specification<Customer> spec = PredicateBuilder.<Customer>and()
        .equal("status", 1)                              // 状态=1
        .like(StrUtil::isNotBlank, "name", keyword)      // 姓名模糊查询（如果 keyword 不为空）
        .equal("addressId", addressId)                   // 地址ID（如果不为 null）
        .build();
    
    return customerRepository.findAll(spec, pageable);
}
```

### 2.3 OR 条件查询

```java
/**
 * 查询进行中或部分缴费的账本
 */
public List<Ledger> findActiveLedgers() {
    Specification<Ledger> spec = PredicateBuilder.<Ledger>or()
        .equal("ledgerStatus", LedgerStatus.IN_PROGRESS)
        .equal("ledgerStatus", LedgerStatus.PARTIAL)
        .build();
    
    return ledgerRepository.findAll(spec);
}
```

---

## 3. 常用方法

### 3.1 等值查询

```java
// 自动过滤 null 值
.equal("status", status)              // 如果 status 为 null，条件不生效

// 不等于
.notEqual("status", 0)                // status != 0
```

### 3.2 模糊查询

```java
// 两端模糊查询 %keyword%
.like(StrUtil::isNotBlank, "name", keyword)

// 右模糊查询 keyword%
.smartLike(StrUtil::isNotBlank, "name", keyword)

// 自动转义特殊字符（%, _, \）
// 输入: "test%"  → 查询: "test\%"
```

### 3.3 IN 查询

```java
// 集合查询
List<Long> addressIds = Arrays.asList(1L, 2L, 3L);
.in("addressId", addressIds)          // addressId IN (1, 2, 3)

// 自动过滤空集合
.in("addressId", Collections.emptyList())  // 条件不生效
```

### 3.4 范围查询

```java
// 时间范围
.betweenInstant("createInstant", startTime, endTime)
.betweenLocalDate("createDate", startDate, endDate)

// 大于/小于
.greaterThan("createInstant", instant)
.greaterThanOrEqualTo("createInstant", instant)
.lessThan("createInstant", instant)
.lessThanOrEqualTo("createInstant", instant)
```

### 3.5 空值判断

```java
// 字符串为空（null 或 ""）
.isStringBlank(true, "memo")

// 字符串不为空
.isStringNotBlank(true, "memo")

// 所有字段都为 null
.allFieldsNull(true, "field1", "field2", "field3")

// 至少一个字段不为 null
.atLeastOneNotNull(true, "field1", "field2", "field3")
```

---

## 4. 条件控制

### 4.1 使用 Predicate 控制条件

```java
// 使用 Hutool 的 StrUtil
.like(StrUtil::isNotBlank, "name", keyword)      // 仅当 keyword 不为空时生效

// 使用 Hutool 的 CollUtil
.in(CollUtil::isNotEmpty, "addressId", addressIds)  // 仅当集合不为空时生效

// 使用 Hutool 的 ObjectUtil
.equal(ObjectUtil::isNotEmpty, "status", status)    // 仅当 status 不为空时生效
```

### 4.2 使用 boolean 控制条件

```java
// 根据布尔值决定是否添加条件
.equal(status != null, "status", status)
.like(StrUtil.isNotBlank(keyword), "name", keyword)
```

---

## 5. 完整示例

### 5.1 客户查询示例

```java
@Service
@RequiredArgsConstructor
public class CustomerService {
    
    private final CustomerRepository customerRepository;
    
    /**
     * 高级搜索客户
     *
     * @param keyword 关键词（姓名/手机号）
     * @param gender 性别
     * @param addressId 地址ID
     * @param minAge 最小年龄
     * @param maxAge 最大年龄
     * @param pageable 分页参数
     * @return 客户分页列表
     */
    public Page<Customer> advancedSearch(
            String keyword,
            Gender gender,
            Long addressId,
            Integer minAge,
            Integer maxAge,
            Pageable pageable) {
        
        Specification<Customer> spec = PredicateBuilder.<Customer>and()
            // 状态必须为有效
            .equal("status", 1)
            
            // 姓名模糊查询（如果 keyword 不为空）
            .like(StrUtil::isNotBlank, "name", keyword)
            
            // 性别筛选（如果 gender 不为 null）
            .equal("gender", gender)
            
            // 地址筛选（如果 addressId 不为 null）
            .equal("addressId", addressId)
            
            // 年龄范围（如果指定）
            .greaterThanOrEqualTo(minAge != null, "age", minAge)
            .lessThanOrEqualTo(maxAge != null, "age", maxAge)
            
            .build();
        
        return customerRepository.findAll(spec, pageable);
    }
}
```

### 5.2 账本查询示例

```java
@Service
@RequiredArgsConstructor
public class LedgerService {
    
    private final LedgerRepository ledgerRepository;
    
    /**
     * 查询活跃账本（主页面）
     * 状态为 IN_PROGRESS 或 PARTIAL
     */
    public Page<Ledger> listActiveLedgers(Pageable pageable) {
        List<LedgerStatus> activeStatuses = Arrays.asList(
            LedgerStatus.IN_PROGRESS,
            LedgerStatus.PARTIAL
        );
        
        Specification<Ledger> spec = PredicateBuilder.<Ledger>and()
            .equal("status", 1)
            .in("ledgerStatus", activeStatuses)
            .build();
        
        return ledgerRepository.findAll(spec, pageable);
    }
    
    /**
     * 查询客户的所有账本
     */
    public Page<Ledger> listCustomerLedgers(Long customerId, Pageable pageable) {
        Specification<Ledger> spec = PredicateBuilder.<Ledger>and()
            .equal("status", 1)
            .equal("customerId", customerId)
            .build();
        
        return ledgerRepository.findAll(spec, pageable);
    }
    
    /**
     * 查询赊账账本
     */
    public Page<Ledger> listCreditLedgers(Pageable pageable) {
        Specification<Ledger> spec = PredicateBuilder.<Ledger>and()
            .equal("status", 1)
            .equal("ledgerStatus", LedgerStatus.ON_CREDIT)
            .build();
        
        return ledgerRepository.findAll(spec, pageable);
    }
}
```

### 5.3 商品查询示例

```java
@Service
@RequiredArgsConstructor
public class ProductService {
    
    private final ProductRepository productRepository;
    
    /**
     * 搜索商品
     *
     * @param keyword 关键词（商品名称）
     * @param categoryId 分类ID
     * @param minPrice 最小价格
     * @param maxPrice 最大价格
     * @param pageable 分页参数
     * @return 商品分页列表
     */
    public Page<Product> searchProducts(
            String keyword,
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable) {
        
        Specification<Product> spec = PredicateBuilder.<Product>and()
            .equal("status", 1)
            .like(StrUtil::isNotBlank, "name", keyword)
            .equal("categoryId", categoryId)
            .greaterThanOrEqualTo(minPrice != null, "price", minPrice)
            .lessThanOrEqualTo(maxPrice != null, "price", maxPrice)
            .build();
        
        return productRepository.findAll(spec, pageable);
    }
}
```

---

## 6. 高级用法

### 6.1 组合 AND 和 OR

```java
// 复杂条件：(status=1) AND ((name LIKE '%keyword%') OR (phone LIKE '%keyword%'))
Specification<Customer> nameOrPhoneSpec = PredicateBuilder.<Customer>or()
    .like(StrUtil::isNotBlank, "name", keyword)
    .like(StrUtil::isNotBlank, "phone", keyword)
    .build();

Specification<Customer> finalSpec = PredicateBuilder.<Customer>and()
    .equal("status", 1)
    .predicate(true, nameOrPhoneSpec)  // 嵌套 OR 条件
    .build();
```

### 6.2 动态排序

```java
// 结合 Pageable 实现动态排序
Sort sort = Sort.by(Sort.Direction.DESC, "createInstant");
Pageable pageable = PageRequest.of(page, size, sort);

Page<Customer> result = customerRepository.findAll(spec, pageable);
```

---

## 7. 最佳实践

### ✅ 推荐做法

```java
// 1. 使用 Predicate 自动过滤空值
.like(StrUtil::isNotBlank, "name", keyword)

// 2. 状态筛选放在最前面
.equal("status", 1)

// 3. 使用有意义的变量名
Specification<Customer> activeCustomersSpec = ...

// 4. 复杂条件拆分为多个方法
private Specification<Customer> buildStatusSpec() { ... }
private Specification<Customer> buildKeywordSpec(String keyword) { ... }
```

### ❌ 不推荐做法

```java
// 1. 不要手动判断 null
if (keyword != null && !keyword.isEmpty()) {
    .like("name", keyword)  // ❌ 应该使用 Predicate
}

// 2. 不要在 PredicateBuilder 中写业务逻辑
.equal("status", calculateStatus())  // ❌ 应该在外部计算

// 3. 不要嵌套太深
PredicateBuilder.and()
    .predicate(true, PredicateBuilder.or()
        .predicate(true, PredicateBuilder.and()...))  // ❌ 太复杂
```

---

## 8. 性能优化

### 8.1 索引优化

确保查询字段有索引：
```sql
-- 为常用查询字段添加索引
CREATE INDEX idx_customer_status ON customer(status);
CREATE INDEX idx_customer_name ON customer(name);
CREATE INDEX idx_customer_address_id ON customer(address_id);
```

### 8.2 分页查询

```java
// 总是使用分页查询，避免一次性加载大量数据
Pageable pageable = PageRequest.of(page, size);
Page<Customer> result = customerRepository.findAll(spec, pageable);
```

### 8.3 避免 N+1 查询

```java
// 如果需要关联数据，使用 MyBatis 联表查询
// ❌ 不要在循环中查询关联数据
for (Ledger ledger : ledgers) {
    Customer customer = customerRepository.findById(ledger.getCustomerId());  // N+1 问题
}

// ✅ 使用 MyBatis 一次性查询
List<LedgerWithCustomerVO> result = ledgerMapper.selectLedgersWithCustomer(spec);
```

---

## 9. 常见问题

### Q1: PredicateBuilder 和 MyBatis 如何选择？

**A**: 
- **单表条件查询** → PredicateBuilder
- **多表联查** → MyBatis
- **复杂统计** → MyBatis

### Q2: 如何处理 OR 条件？

**A**: 使用 `PredicateBuilder.or()` 构建 OR 条件，然后嵌套到 AND 条件中。

### Q3: 如何调试生成的 SQL？

**A**: 在 `application.yml` 中开启 SQL 日志：
```yaml
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

### Q4: 性能如何？

**A**: PredicateBuilder 生成的是标准的 JPA Criteria Query，性能与手写 JPQL 相当。关键是要有合适的索引。

---

## 10. 参考资料

- [Spring Data JPA Specifications](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#specifications)
- [JPA Criteria API](https://docs.oracle.com/javaee/7/tutorial/persistence-criteria.htm)
- [Hutool 文档](https://hutool.cn/docs/)

---

**文档维护**: 本文档应随 PredicateBuilder 功能演进及时更新  
**最后更新**: 2025-11-24  
**维护团队**: Core Ledger Team
