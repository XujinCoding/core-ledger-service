# JPA 实体最佳实践

## 文档说明
本文档说明 JPA 实体类的最佳实践和注意事项。

**创建日期**: 2025-11-24  
**维护团队**: Core Ledger Team  
**文档版本**: 1.0.0

---

## 1. Lombok 注解使用规范

### ❌ 不推荐的做法

```java
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
public class Customer extends BaseEntity {
    // ...
}
```

**问题**:
1. **性能问题**: `@Data` 包含 `equals()` 和 `hashCode()`，在 JPA 实体中可能导致懒加载触发
2. **内存消耗**: `@EqualsAndHashCode(callSuper = true)` 会递归调用父类的 equals/hashCode
3. **循环引用**: 双向关联时可能导致无限递归
4. **代理问题**: Hibernate 代理对象的 equals/hashCode 可能不符合预期

### ✅ 推荐的做法

```java
@Getter
@Setter
@ToString
@Entity
@Table(name = "customer")
public class Customer extends BaseEntity {
    // ...
}
```

**优点**:
1. **避免性能问题**: 不生成 equals/hashCode，避免懒加载触发
2. **减少内存消耗**: 不会递归调用父类方法
3. **避免循环引用**: toString 可以通过 `@ToString.Exclude` 排除关联字段
4. **更可控**: 如果需要 equals/hashCode，可以手动实现

---

## 2. 实体类标准模板

### 2.1 基础实体

```java
package com.coreledger.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 客户实体
 *
 * <p>对应数据库表: customer</p>
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "customer")
public class Customer extends BaseEntity {

    /** 客户姓名 */
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    /** 手机号 */
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;
}
```

### 2.2 包含关联关系的实体

```java
@Getter
@Setter
@ToString(exclude = {"ledgerItems"}) // 排除集合关联
@Entity
@Table(name = "ledger")
public class Ledger extends BaseEntity {

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    // 一对多关联（如果需要）
    @OneToMany(mappedBy = "ledgerId", fetch = FetchType.LAZY)
    @ToString.Exclude  // 避免 toString 触发懒加载
    private List<LedgerItem> ledgerItems = new ArrayList<>();
}
```

---

## 3. equals() 和 hashCode() 的处理

### 3.1 不需要实现的场景

大多数情况下，JPA 实体**不需要**重写 equals/hashCode：
- 实体主要用于数据库持久化
- 不需要放入 Set 或作为 Map 的 key
- 不需要在集合中去重

### 3.2 需要实现的场景

如果确实需要，推荐基于**业务唯一键**实现：

```java
@Getter
@Setter
@ToString
@Entity
@Table(name = "customer")
public class Customer extends BaseEntity {

    @Column(name = "phone", nullable = false, unique = true)
    private String phone;

    /**
     * 基于业务唯一键（手机号）实现 equals
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer)) return false;
        Customer customer = (Customer) o;
        return phone != null && phone.equals(customer.getPhone());
    }

    /**
     * 基于业务唯一键（手机号）实现 hashCode
     */
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
```

**注意**:
- ❌ 不要基于 `id` 实现（id 在持久化前为 null）
- ✅ 基于业务唯一键（如手机号、用户名）
- ✅ hashCode() 返回固定值或基于 class

---

## 4. 关联关系最佳实践

### 4.1 避免双向关联

**不推荐**:
```java
@Entity
public class Ledger {
    @OneToMany(mappedBy = "ledger")
    private List<LedgerItem> items;  // 双向关联
}

@Entity
public class LedgerItem {
    @ManyToOne
    private Ledger ledger;  // 双向关联
}
```

**推荐**:
```java
@Entity
public class Ledger {
    // 不维护关联关系
}

@Entity
public class LedgerItem {
    @Column(name = "ledger_id")
    private Long ledgerId;  // 只存储外键
}
```

**优点**:
- 避免循环引用
- 减少内存占用
- 查询更灵活（通过 Repository 查询）

### 4.2 使用 @ToString.Exclude

如果必须使用关联关系，务必排除：

```java
@Getter
@Setter
@ToString(exclude = {"items", "customer"})
@Entity
public class Ledger {
    
    @OneToMany(mappedBy = "ledgerId", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<LedgerItem> items;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    private Customer customer;
}
```

---

## 5. 懒加载注意事项

### 5.1 默认使用懒加载

```java
@ManyToOne(fetch = FetchType.LAZY)  // 推荐
private Customer customer;

@OneToMany(fetch = FetchType.LAZY)  // 推荐
private List<LedgerItem> items;
```

### 5.2 避免在 toString/equals/hashCode 中触发懒加载

```java
@Getter
@Setter
@ToString(exclude = {"customer"})  // ✅ 排除懒加载字段
@Entity
public class Ledger {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude  // ✅ 避免 toString 触发懒加载
    private Customer customer;
}
```

---

## 6. 枚举映射

### 6.1 使用自定义转换器

```java
@Getter
@Setter
@ToString
@Entity
public class Ledger {
    
    @Column(name = "ledger_status", nullable = false)
    @Convert(converter = LedgerStatusConverter.class)  // ✅ 使用转换器
    private LedgerStatus ledgerStatus;
}
```

### 6.2 不要使用 @Enumerated

```java
// ❌ 不推荐
@Enumerated(EnumType.STRING)  // 存储字符串，浪费空间
private LedgerStatus status;

@Enumerated(EnumType.ORDINAL)  // 存储序号，不安全
private LedgerStatus status;
```

---

## 7. 审计字段

### 7.1 使用 BaseEntity

所有实体继承 `BaseEntity`，自动包含审计字段：

```java
@Getter
@Setter
@ToString
@Entity
public class Customer extends BaseEntity {
    // 自动包含: id, memo, status, createInstant, modifyInstant, version
}
```

### 7.2 启用 JPA 审计

```java
@Configuration
@EnableJpaAuditing
public class JpaConfig {
    // ...
}
```

---

## 8. 常见问题

### Q1: 为什么不用 @Data？

**A**: `@Data` 包含 `equals()` 和 `hashCode()`，在 JPA 实体中可能导致：
- 懒加载触发
- 循环引用
- 性能问题
- 内存消耗

### Q2: 如何比较两个实体是否相等？

**A**: 
- 如果只是判断是否为同一个对象，使用 `==`
- 如果需要业务逻辑判断，手动实现 equals（基于业务唯一键）
- 大多数情况下不需要比较实体

### Q3: toString 会触发懒加载吗？

**A**: 
- 如果 toString 包含懒加载字段，会触发
- 使用 `@ToString(exclude = {"lazyField"})` 排除
- 或使用 `@ToString.Exclude` 注解在字段上

### Q4: 可以在实体中使用 @Builder 吗？

**A**: 
- 可以，但要小心
- 推荐在 DTO 中使用 @Builder
- 实体类建议使用 setter 方法

---

## 9. 检查清单

创建新实体时，请检查：

- [ ] ✅ 使用 `@Getter`、`@Setter`、`@ToString`
- [ ] ❌ 不使用 `@Data`
- [ ] ❌ 不使用 `@EqualsAndHashCode`
- [ ] ✅ 继承 `BaseEntity`
- [ ] ✅ 关联关系使用 `@ToString.Exclude`
- [ ] ✅ 枚举使用自定义转换器
- [ ] ✅ 添加完整的 JavaDoc 注释
- [ ] ✅ 字段使用 `@Column` 注解

---

## 10. 参考资料

- [Hibernate Best Practices](https://vladmihalcea.com/hibernate-facts-equals-and-hashcode/)
- [Lombok and JPA](https://www.baeldung.com/lombok-ide)
- [JPA Performance Tips](https://thoughts-on-java.org/tips-to-boost-your-hibernate-performance/)

---

**文档维护**: 本文档应随最佳实践演进及时更新  
**最后更新**: 2025-11-24  
**维护团队**: Core Ledger Team
