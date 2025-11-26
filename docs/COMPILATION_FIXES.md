# 编译错误修复汇总

本文档记录了所有已修复的编译错误及其解决方案。

## 修复时间
2025-11-25

## 修复汇总

### 第一轮修复：MapStruct 和类型不匹配错误

#### 1. Gender 枚举映射问题
**错误**: `Can't map property "Integer gender" to "Gender gender"`

**修复**: 在 `CustomerConverter.java` 中添加自定义映射方法：
```java
default Gender mapGender(Integer value) {
    if (value == null) return Gender.UNKNOWN;
    return Gender.fromValue(value);
}

default Integer mapGenderToInt(Gender gender) {
    if (gender == null) return 0;
    return gender.getValue();
}
```

**位置**: `src/main/java/com/coreledger/converter/CustomerConverter.java:80-95`

---

#### 2. 缺少 ProductListVO 和 UpdateProductDTO
**错误**: `找不到符号 - 类 ProductListVO, UpdateProductDTO`

**修复**: 创建了两个缺失的文件：
- `src/main/java/com/coreledger/vo/product/ProductListVO.java`
- `src/main/java/com/coreledger/dto/product/UpdateProductDTO.java`

---

#### 3. ProductSku 映射错误
**错误**: `No property named "product.id" exists in source parameter`

**修复**: 在 `ProductConverter.java` 中修改映射：
```java
// 修复前
@Mapping(target = "productId", source = "product.id")

// 修复后
@Mapping(target = "productId", source = "productId")
```

**位置**: `src/main/java/com/coreledger/converter/ProductConverter.java:76`

---

#### 4. BaseEntity 字段未映射
**错误**: `Unmapped target properties: "id, status, createInstant, modifyInstant, version"`

**修复**: 在所有 Converter 的 DTO → Entity 映射方法中添加：
```java
@Mapping(target = "id", ignore = true)
@Mapping(target = "status", ignore = true)
@Mapping(target = "createInstant", ignore = true)
@Mapping(target = "modifyInstant", ignore = true)
@Mapping(target = "version", ignore = true)
```

**影响文件**:
- `CustomerConverter.java`
- `ProductConverter.java`
- `LedgerConverter.java`

---

#### 5. CreateLedgerDTO.ItemDTO 类名错误
**错误**: `找不到符号 - 类 ItemDTO`

**修复**: 内部类实际名为 `LedgerItemDTO`，更新了所有引用：
```java
// 修复前
CreateLedgerDTO.ItemDTO

// 修复后
CreateLedgerDTO.LedgerItemDTO
```

同时添加了完整的产品信息字段：
```java
public static class LedgerItemDTO {
    private Long skuId;
    private Long productId;        // 新增
    private String productName;    // 新增
    private String skuName;        // 新增
    private Map<String, String> attrValueMap;  // 新增
    private BigDecimal price;
    private Integer quantity;
}
```

**位置**: `src/main/java/com/coreledger/dto/ledger/CreateLedgerDTO.java:38-68`

---

#### 6. UpdateLedgerItemsDTO 结构问题
**错误**: 字段命名不一致

**修复**: 重构字段命名：
```java
// 修复前
private List<AddItemDTO> newItems;
private List<UpdateItemDTO> updatedItems;
private List<Long> deletedItemIds;

// 修复后
private List<AddItemDTO> addItems;
private List<UpdateItemDTO> updateItems;
private List<Long> deleteItemIds;
```

**位置**: `src/main/java/com/coreledger/dto/ledger/UpdateLedgerItemsDTO.java`

---

#### 7. GenerateSkusDTO.SkuDTO 缺失
**错误**: `找不到符号 - 类 SkuDTO`

**修复**: 将 `SkuPriceDTO` 重命名为 `SkuDTO`，并添加 `imageUrl` 字段：
```java
public static class SkuDTO {
    private Map<String, String> attrValueMap;
    private BigDecimal price;
    private String imageUrl;  // 新增
}
```

**位置**: `src/main/java/com/coreledger/dto/product/GenerateSkusDTO.java:47-60`

---

### 第二轮修复：Status 枚举和 PredicateBuilder 问题

#### 8. Status 类型不匹配
**错误**: `不兼容的类型: int无法转换为com.coreledger.enums.Status`

**修复**: 使用 sed 脚本批量替换所有 Service 文件：
```bash
# 替换模式
.setStatus(1) → .setStatus(Status.ACTIVE)
.setStatus(0) → .setStatus(Status.INACTIVE)
findByIdAndStatus(..., 1) → findByIdAndStatus(..., Status.ACTIVE)
findByStatus(1, ...) → findByStatus(Status.ACTIVE, ...)
```

**影响文件**:
- `ProductService.java` - 添加了 Status import
- `CustomerService.java` - 添加了 Status import
- `LedgerService.java` - 添加了 Status import
- `AuthService.java` - 添加了 Status import

---

#### 9. PredicateBuilder.or() 方法不存在
**错误**: `无法将类 PredicateBuilder<T>中的方法 or应用到给定类型`

**修复**: PredicateBuilder 不支持 .or() 方法，改用手动 Specification：
```java
// 修复前（无效）
Specification<Customer> spec = PredicateBuilder.<Customer>and()
    .equal("status", Status.ACTIVE)
    .or()  // 此方法不存在
    .like("name", keyword)
    .like("phone", keyword)
    .build();

// 修复后
Specification<Customer> spec = PredicateBuilder.<Customer>and()
    .equal("status", Status.ACTIVE)
    .build();

if (StrUtil.isNotBlank(keyword)) {
    Specification<Customer> keywordSpec = (root, query, cb) -> cb.or(
        cb.like(root.get("name"), "%" + keyword + "%"),
        cb.like(root.get("phone"), "%" + keyword + "%")
    );
    spec = spec.and(keywordSpec);
}
```

**位置**: `src/main/java/com/coreledger/service/CustomerService.java:99-112`

---

#### 10. PredicateBuilder.like() 参数顺序错误
**错误**: `对于like(...), 找不到合适的方法`

**修复**: 修正参数顺序：
```java
// 修复前（错误）
.like(StrUtil::isNotBlank, keyword, "name", keyword)

// 修复后（正确）
.like(StrUtil::isNotBlank, "name", keyword)
```

**正确签名**: `like(Predicate<String> condition, String attribute, String value)`

**位置**: `src/main/java/com/coreledger/service/ProductService.java:236`

---

### 第三轮修复：BusinessException 构造函数问题

#### 11. BusinessException String 构造函数缺失
**错误**: `java.lang.String无法转换为com.coreledger.enums.BusinessCode`

**修复**: 在 BusinessException 类中添加接受 String 的便捷构造函数：
```java
/**
 * 构造函数（仅使用自定义消息，默认使用 BUSINESS_ERROR 错误码）
 *
 * @param message 错误消息
 */
public BusinessException(String message) {
    super(message);
    this.code = BusinessCode.BUSINESS_ERROR.getCode();
    this.message = message;
}
```

同时更新所有 Service 类使用合适的 BusinessCode：

**CustomerService.java**:
```java
// 修复前
throw new BusinessException("手机号已存在: " + dto.getPhone());

// 修复后
throw new BusinessException(BusinessCode.CUSTOMER_PHONE_EXISTS, "手机号已存在: " + dto.getPhone());
```

**ProductService.java**:
```java
// 修复前
throw new BusinessException("请先设置商品属性");

// 修复后
throw new BusinessException(BusinessCode.BUSINESS_ERROR, "请先设置商品属性");
```

**LedgerService.java**:
```java
// 修复前
throw new BusinessException("账单状态为【" + ledger.getLedgerStatus().getDescription() + "】，不允许修改明细");

// 修复后
throw new BusinessException(BusinessCode.LEDGER_STATUS_NOT_ALLOWED, "账单状态为【" + ledger.getLedgerStatus().getDescription() + "】，不允许修改明细");
```

**影响文件**:
- `BusinessException.java` - 添加了 String 构造函数
- `CustomerService.java` - 添加了 BusinessCode import，更新了 3 处异常
- `ProductService.java` - 添加了 BusinessCode import，更新了 1 处异常
- `LedgerService.java` - 添加了 BusinessCode import，更新了 9 处异常

**位置**:
- `src/main/java/com/coreledger/exception/BusinessException.java:69-73`
- `src/main/java/com/coreledger/service/CustomerService.java:12,65,210,247`
- `src/main/java/com/coreledger/service/ProductService.java:14,120`
- `src/main/java/com/coreledger/service/LedgerService.java:17,112,160,192,198,248,254,287,316,428`

---

## 验证清单

✅ 所有 Service 类都导入了 `com.coreledger.enums.Status`
✅ 不再使用硬编码的状态值（1 或 0）
✅ 所有 Converter 正确映射 BaseEntity 字段
✅ Gender 枚举双向转换正确实现
✅ 所有 DTO 类名和字段名正确
✅ PredicateBuilder 使用正确（不使用不存在的 .or() 方法）
✅ 批量查询模式正确实现（避免 N+1 问题）
✅ BusinessException 支持 String 构造函数
✅ 所有 Service 类都导入了 `com.coreledger.enums.BusinessCode`
✅ 所有业务异常都使用了合适的 BusinessCode

## 技术要点

### 1. 枚举转换模式
在 MapStruct Converter 中使用 default 方法进行双向转换：
```java
default EnumType mapEnum(Integer value) { ... }
default Integer mapEnumToInt(EnumType enumValue) { ... }
```

### 2. 批量查询模式
避免 N+1 问题的标准流程：
1. 分页查询主实体
2. 提取关联 ID
3. 单次批量查询关联实体
4. 转换为 Map 进行 O(1) 查找
5. 填充数据到 VO

### 3. 动态查询模式
- 简单 AND 条件：使用 `PredicateBuilder`
- 复杂 OR 条件：手动创建 `Specification`
- 组合：使用 `spec.and(manualSpec)`

### 4. Status 枚举使用
```java
// 正确用法
entity.setStatus(Status.ACTIVE);    // value = 1
entity.setStatus(Status.INACTIVE);  // value = 0
repository.findByIdAndStatus(id, Status.ACTIVE);

// 错误用法
entity.setStatus(1);  // 编译错误
```

### 5. BusinessException 使用模式
```java
// 方式1：使用 BusinessCode 枚举（推荐）
throw new BusinessException(BusinessCode.CUSTOMER_PHONE_EXISTS, "手机号已存在: " + phone);

// 方式2：仅使用 BusinessCode
throw new BusinessException(BusinessCode.USER_NOT_FOUND);

// 方式3：使用默认错误码和自定义消息（不推荐）
throw new BusinessException("自定义错误消息");  // 默认使用 BUSINESS_ERROR (9004)

// 方式4：使用自定义错误码
throw new BusinessException(1001, "自定义错误消息");
```

**最佳实践**:
- 优先使用方式1，明确指定 BusinessCode 和自定义消息
- 如果消息与 BusinessCode 默认消息一致，使用方式2
- 避免使用方式3，应该明确指定业务错误码
- 仅在特殊场景使用方式4

## 后续建议

1. **单元测试**: 为所有 Service 方法编写单元测试
2. **集成测试**: 测试完整的业务流程
3. **性能测试**: 验证批量查询优化效果
4. **代码审查**: 确保所有枚举转换正确
5. **文档更新**: 保持 API 文档与代码同步

## 相关文档

- [系统设计文档](./SYSTEM_DESIGN.md)
- [API 使用指南](./API_USAGE_GUIDE.md)
- [数据库设计](../src/main/resources/db/migration/)

---

**最后更新**: 2025-11-25
**修复人员**: Claude Code
**状态**: ✅ 所有编译错误已修复
