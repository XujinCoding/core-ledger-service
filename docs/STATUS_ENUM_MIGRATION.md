# Status 枚举迁移指南

## 变更说明

将 `BaseEntity` 中的 `status` 字段从 `Integer` 类型改为 `Status` 枚举类型。

**变更日期**: 2025-11-24  
**影响范围**: 所有继承 `BaseEntity` 的实体类

---

## 1. 变更内容

### 1.1 新增枚举

创建 `Status` 枚举：

```java
public enum Status implements BaseEnum {
    INACTIVE(0, "无效"),  // 已删除/禁用
    ACTIVE(1, "有效");    // 启用/正常
}
```

### 1.2 BaseEntity 变更

**之前**:
```java
@Column(name = "status", nullable = false)
private Integer status = 1;
```

**之后**:
```java
@Column(name = "status", nullable = false)
@Convert(converter = StatusConverter.class)
private Status status = Status.ACTIVE;
```

### 1.3 Repository 方法变更

所有 Repository 中的 `status` 参数从 `Integer` 改为 `Status`：

**之前**:
```java
Optional<SysUser> findByIdAndStatus(Long id, Integer status);
```

**之后**:
```java
Optional<SysUser> findByIdAndStatus(Long id, Status status);
```

---

## 2. 代码迁移

### 2.1 查询方法调用

**之前**:
```java
// 查询有效用户
sysUserRepository.findByPhoneAndStatus(phone, 1);

// 检查是否存在
sysUserRepository.existsByPhoneAndStatus(phone, 1);
```

**之后**:
```java
// 查询有效用户
sysUserRepository.findByPhoneAndStatus(phone, Status.ACTIVE);

// 检查是否存在
sysUserRepository.existsByPhoneAndStatus(phone, Status.ACTIVE);
```

### 2.2 设置状态

**之前**:
```java
user.setStatus(1);  // 启用
user.setStatus(0);  // 禁用
```

**之后**:
```java
user.setStatus(Status.ACTIVE);    // 启用
user.setStatus(Status.INACTIVE);  // 禁用
```

### 2.3 状态判断

**之前**:
```java
if (user.getStatus() == 1) {
    // 用户已启用
}
```

**之后**:
```java
if (user.getStatus() == Status.ACTIVE) {
    // 用户已启用
}
```

---

## 3. 数据库兼容性

### 3.1 数据库字段

数据库中的 `status` 字段仍然是 `TINYINT` 类型，存储值为：
- `0` = INACTIVE（无效）
- `1` = ACTIVE（有效）

### 3.2 无需数据迁移

由于枚举值与数据库存储值一致，**不需要进行数据迁移**。

---

## 4. 优势

### 4.1 类型安全

```java
// ❌ 之前：可能传入错误的值
user.setStatus(999);  // 编译通过，但逻辑错误

// ✅ 之后：编译期检查
user.setStatus(Status.ACTIVE);  // 类型安全
```

### 4.2 代码可读性

```java
// ❌ 之前：需要记住 0 和 1 的含义
if (user.getStatus() == 1) { ... }

// ✅ 之后：语义清晰
if (user.getStatus() == Status.ACTIVE) { ... }
```

### 4.3 IDE 支持

- 自动补全
- 重构安全
- 查找引用

---

## 5. 受影响的实体

所有继承 `BaseEntity` 的实体都受影响：

- `SysUser` - 系统用户
- `SysAddress` - 地址库
- `Customer` - 客户
- `Ledger` - 账本
- `LedgerItem` - 账本明细
- `Product` - 商品
- `ProductCategory` - 商品分类
- `PaymentRecord` - 支付流水

---

## 6. 注意事项

### 6.1 PredicateBuilder 使用

使用 `PredicateBuilder` 时，直接传入枚举：

```java
Specification<Customer> spec = PredicateBuilder.<Customer>and()
    .equal("status", Status.ACTIVE)  // 直接使用枚举
    .like("name", keyword)
    .build();
```

### 6.2 JSON 序列化

枚举会自动序列化为数值：

```json
{
  "id": 1,
  "name": "张三",
  "status": 1  // 自动转换为数值
}
```

### 6.3 MyBatis 映射

如果使用 MyBatis，需要使用 TypeHandler：

```xml
<result column="status" property="status" 
        typeHandler="com.coreledger.config.mybatis.StatusTypeHandler"/>
```

---

## 7. 测试建议

### 7.1 单元测试

```java
@Test
void testStatusEnum() {
    SysUser user = new SysUser();
    user.setStatus(Status.ACTIVE);
    
    assertThat(user.getStatus()).isEqualTo(Status.ACTIVE);
    assertThat(user.getStatus().getValue()).isEqualTo(1);
}
```

### 7.2 集成测试

```java
@Test
void testFindByStatus() {
    List<SysUser> users = sysUserRepository
        .findByStatus(Status.ACTIVE);
    
    assertThat(users).isNotEmpty();
    assertThat(users).allMatch(u -> u.getStatus() == Status.ACTIVE);
}
```

---

## 8. 常见问题

### Q1: 旧代码中的 Integer 比较会报错吗？

是的，需要全部改为枚举比较：

```java
// ❌ 错误
if (user.getStatus() == 1) { ... }

// ✅ 正确
if (user.getStatus() == Status.ACTIVE) { ... }
```

### Q2: 数据库中的数据需要更新吗？

不需要。枚举值与数据库存储值完全一致。

### Q3: 如何在 SQL 中使用？

直接使用数值：

```sql
SELECT * FROM sys_user WHERE status = 1;  -- 查询有效用户
```

---

## 9. 完整示例

### 9.1 创建用户

```java
@Service
public class UserService {
    
    public SysUser createUser(String phone) {
        SysUser user = new SysUser();
        user.setPhone(phone);
        user.setStatus(Status.ACTIVE);  // 使用枚举
        
        return sysUserRepository.save(user);
    }
}
```

### 9.2 查询用户

```java
@Service
public class UserService {
    
    public List<SysUser> getActiveUsers() {
        // 使用 PredicateBuilder
        Specification<SysUser> spec = PredicateBuilder.<SysUser>and()
            .equal("status", Status.ACTIVE)
            .build();
        
        return sysUserRepository.findAll(spec);
    }
}
```

### 9.3 禁用用户

```java
@Service
public class UserService {
    
    public void disableUser(Long userId) {
        SysUser user = sysUserRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("用户不存在"));
        
        user.setStatus(Status.INACTIVE);  // 禁用
        sysUserRepository.save(user);
    }
}
```

---

**迁移完成后，所有状态相关的代码都更加类型安全和易读！** ✅
