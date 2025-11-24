# 统一异常处理与响应格式

## 文档说明
本文档说明系统的统一异常处理机制和响应格式规范。

**创建日期**: 2025-11-24  
**维护团队**: Core Ledger Team  
**文档版本**: 1.0.0

---

## 1. 统一响应格式

### 1.1 Result 类结构

```java
public class Result<T> {
    private Integer code;      // 响应码
    private String message;    // 响应消息
    private T data;           // 响应数据
    private Long timestamp;   // 时间戳
}
```

### 1.2 成功响应示例

**无数据**:
```java
return Result.success();
```
```json
{
  "code": 200,
  "message": "操作成功",
  "data": null,
  "timestamp": 1700000000000
}
```

**带数据**:
```java
return Result.success(ledgerDTO);
```
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "customerName": "张三",
    "totalAmount": 100.00
  },
  "timestamp": 1700000000000
}
```

**自定义消息**:
```java
return Result.success("创建成功", ledgerDTO);
```

### 1.3 失败响应示例

**使用错误码枚举**:
```java
return Result.error(ErrorCode.LEDGER_NOT_FOUND);
```
```json
{
  "code": 2001,
  "message": "账本不存在",
  "data": null,
  "timestamp": 1700000000000
}
```

**自定义错误消息**:
```java
return Result.error(ErrorCode.LEDGER_NOT_FOUND, "账本ID=123不存在");
```

**直接使用错误码和消息**:
```java
return Result.error(400, "参数错误");
```

---

## 2. 异常体系

### 2.1 异常类层次结构

```
RuntimeException
    └── BusinessException (业务异常基类)
            ├── NotFoundException (资源不存在)
            ├── UnauthorizedException (未授权)
            └── ForbiddenException (无权限)
```

### 2.2 BusinessException（业务异常）

**用途**: 所有业务逻辑异常的基类

**使用示例**:
```java
// 使用错误码枚举
throw new BusinessException(ErrorCode.LEDGER_STATUS_NOT_ALLOWED);

// 使用错误码枚举 + 自定义消息
throw new BusinessException(ErrorCode.LEDGER_STATUS_NOT_ALLOWED, "当前状态为已结清，无法收款");

// 使用错误码 + 消息
throw new BusinessException(2002, "账本状态不允许此操作");
```

### 2.3 NotFoundException（资源不存在）

**用途**: 查询资源不存在时抛出

**使用示例**:
```java
// 使用默认错误码 404
throw new NotFoundException("账本不存在");

// 使用自定义错误码
throw new NotFoundException(ErrorCode.LEDGER_NOT_FOUND);

// 使用自定义错误码 + 消息
throw new NotFoundException(ErrorCode.LEDGER_NOT_FOUND, "账本ID=123不存在");
```

**Service 层示例**:
```java
public LedgerDTO getLedgerById(Long id) {
    Ledger ledger = ledgerRepository.findById(id)
        .orElseThrow(() -> new NotFoundException(ErrorCode.LEDGER_NOT_FOUND));
    return ledgerConverter.toDTO(ledger);
}
```

### 2.4 UnauthorizedException（未授权）

**用途**: 用户未登录或登录已过期

**使用示例**:
```java
// 使用默认错误码和消息
throw new UnauthorizedException();

// 自定义消息
throw new UnauthorizedException("登录已过期，请重新登录");
```

### 2.5 ForbiddenException（无权限）

**用途**: 用户无权限访问资源

**使用示例**:
```java
// 使用默认错误码和消息
throw new ForbiddenException();

// 自定义消息
throw new ForbiddenException("仅管理员可执行此操作");

// 使用自定义错误码
throw new ForbiddenException(ErrorCode.ADMIN_ONLY);
```

---

## 3. 全局异常处理器

### 3.1 处理的异常类型

| 异常类型 | HTTP 状态码 | 说明 |
|---------|-----------|------|
| `BusinessException` | 200 | 业务异常 |
| `NotFoundException` | 404 | 资源不存在 |
| `UnauthorizedException` | 401 | 未授权 |
| `ForbiddenException` | 403 | 无权限 |
| `MethodArgumentNotValidException` | 400 | 参数校验失败 |
| `ConstraintViolationException` | 400 | 约束校验失败 |
| `MissingServletRequestParameterException` | 400 | 缺少请求参数 |
| `HttpMessageNotReadableException` | 400 | 请求体格式错误 |
| `ObjectOptimisticLockingFailureException` | 409 | 乐观锁冲突 |
| `DataIntegrityViolationException` | 400 | 数据完整性违反 |
| `Exception` | 500 | 未知异常 |

### 3.2 自动处理

全局异常处理器会自动捕获所有异常并返回统一格式的响应，无需在 Controller 中手动处理。

---

## 4. Service 层使用规范

### 4.1 资源查询

**推荐写法**:
```java
public LedgerDTO getLedgerById(Long id) {
    Ledger ledger = ledgerRepository.findById(id)
        .orElseThrow(() -> new NotFoundException(ErrorCode.LEDGER_NOT_FOUND));
    return ledgerConverter.toDTO(ledger);
}
```

**不推荐写法**:
```java
public LedgerDTO getLedgerById(Long id) {
    Ledger ledger = ledgerRepository.findById(id).orElse(null);
    if (ledger == null) {
        return null;  // ❌ 不要返回 null
    }
    return ledgerConverter.toDTO(ledger);
}
```

### 4.2 业务规则校验

```java
@Transactional
public void receivePayment(Long ledgerId, BigDecimal amount) {
    // 1. 查询账本
    Ledger ledger = ledgerRepository.findById(ledgerId)
        .orElseThrow(() -> new NotFoundException(ErrorCode.LEDGER_NOT_FOUND));
    
    // 2. 校验状态
    if (!ledger.getLedgerStatus().canReceivePayment()) {
        throw new BusinessException(ErrorCode.LEDGER_STATUS_NOT_ALLOWED, 
            "当前状态不允许收款");
    }
    
    // 3. 校验金额
    BigDecimal remainingAmount = ledger.getRemainingAmount();
    if (amount.compareTo(remainingAmount) > 0) {
        throw new BusinessException(ErrorCode.LEDGER_PAYMENT_EXCEED, 
            String.format("支付金额%.2f超过应收金额%.2f", amount, remainingAmount));
    }
    
    // 4. 执行业务逻辑
    // ...
}
```

### 4.3 权限校验

```java
@Transactional
public void discountSettle(Long ledgerId, BigDecimal discountAmount) {
    // 1. 校验权限
    if (!currentUser.isAdmin()) {
        throw new ForbiddenException(ErrorCode.ADMIN_ONLY);
    }
    
    // 2. 执行业务逻辑
    // ...
}
```

---

## 5. Controller 层使用规范

### 5.1 标准写法

**推荐写法**:
```java
@RestController
@RequestMapping("/api/ledgers")
@RequiredArgsConstructor
public class LedgerController {

    private final LedgerService ledgerService;

    @GetMapping("/{id}")
    @Operation(summary = "查询账本详情")
    public Result<LedgerDetailVO> getLedgerById(@PathVariable Long id) {
        LedgerDTO ledgerDTO = ledgerService.getLedgerById(id);
        LedgerDetailVO vo = ledgerConverter.toDetailVO(ledgerDTO);
        return Result.success(vo);
    }

    @PostMapping
    @Operation(summary = "创建账本")
    public Result<LedgerDetailVO> createLedger(@Valid @RequestBody CreateLedgerDTO dto) {
        LedgerDTO ledgerDTO = ledgerService.createLedger(dto);
        LedgerDetailVO vo = ledgerConverter.toDetailVO(ledgerDTO);
        return Result.success("创建成功", vo);
    }
}
```

**不推荐写法**:
```java
@GetMapping("/{id}")
public Result<LedgerDetailVO> getLedgerById(@PathVariable Long id) {
    try {
        LedgerDTO ledgerDTO = ledgerService.getLedgerById(id);
        return Result.success(ledgerConverter.toDetailVO(ledgerDTO));
    } catch (Exception e) {
        return Result.error("查询失败");  // ❌ 不要在 Controller 中捕获异常
    }
}
```

### 5.2 参数校验

使用 `@Valid` 或 `@Validated` 注解进行参数校验，校验失败会自动抛出异常并被全局异常处理器捕获。

```java
@PostMapping
public Result<LedgerDetailVO> createLedger(@Valid @RequestBody CreateLedgerDTO dto) {
    // 参数校验失败会自动抛出 MethodArgumentNotValidException
    // 全局异常处理器会自动处理并返回 400 错误
    return Result.success(ledgerService.createLedger(dto));
}
```

---

## 6. 参数校验

### 6.1 DTO 校验注解

```java
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
```

### 6.2 常用校验注解

| 注解 | 说明 |
|------|------|
| `@NotNull` | 不能为 null |
| `@NotEmpty` | 不能为 null 且不能为空（字符串、集合） |
| `@NotBlank` | 不能为 null 且去除空格后不能为空（字符串） |
| `@Size(min, max)` | 长度范围 |
| `@Min(value)` | 最小值 |
| `@Max(value)` | 最大值 |
| `@DecimalMin(value)` | 最小值（BigDecimal） |
| `@DecimalMax(value)` | 最大值（BigDecimal） |
| `@Positive` | 必须为正数 |
| `@PositiveOrZero` | 必须为正数或 0 |
| `@Email` | 必须为邮箱格式 |
| `@Pattern(regexp)` | 必须匹配正则表达式 |

---

## 7. 乐观锁冲突处理

### 7.1 自动处理

当发生乐观锁冲突时，全局异常处理器会自动捕获 `ObjectOptimisticLockingFailureException` 并返回友好的错误提示。

```json
{
  "code": 9001,
  "message": "数据已被其他用户修改，请刷新后重试",
  "timestamp": 1700000000000
}
```

### 7.2 前端处理建议

前端收到 9001 错误码时，应提示用户刷新页面后重试。

---

## 8. 最佳实践

### 8.1 异常抛出原则

1. ✅ **Service 层抛出异常**，Controller 层不捕获
2. ✅ **使用错误码枚举**，保持错误码统一
3. ✅ **提供清晰的错误消息**，方便排查问题
4. ✅ **记录日志**，重要异常记录 ERROR 级别日志

### 8.2 错误消息规范

1. ✅ **面向用户**：错误消息应该让用户能理解
2. ✅ **提供上下文**：包含关键信息（如 ID、状态等）
3. ✅ **避免技术细节**：不要暴露堆栈信息给用户

**推荐**:
```java
throw new BusinessException(ErrorCode.LEDGER_STATUS_NOT_ALLOWED, 
    "账本状态为已结清，无法继续收款");
```

**不推荐**:
```java
throw new BusinessException(ErrorCode.LEDGER_STATUS_NOT_ALLOWED, 
    "LedgerStatus=CLEARED not allowed for payment operation");
```

### 8.3 日志记录

```java
@Slf4j
@Service
public class LedgerService {
    
    public void receivePayment(Long ledgerId, BigDecimal amount) {
        try {
            // 业务逻辑
            log.info("收款成功: ledgerId={}, amount={}", ledgerId, amount);
        } catch (BusinessException e) {
            log.warn("收款失败: ledgerId={}, amount={}, error={}", 
                ledgerId, amount, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("收款异常: ledgerId={}, amount={}", ledgerId, amount, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "收款失败");
        }
    }
}
```

---

## 9. 测试示例

### 9.1 单元测试

```java
@Test
void testGetLedgerById_NotFound() {
    // Given
    Long ledgerId = 999L;
    when(ledgerRepository.findById(ledgerId)).thenReturn(Optional.empty());
    
    // When & Then
    assertThrows(NotFoundException.class, () -> {
        ledgerService.getLedgerById(ledgerId);
    });
}
```

### 9.2 集成测试

```java
@Test
void testGetLedgerById_NotFound_ReturnsErrorResponse() throws Exception {
    // When & Then
    mockMvc.perform(get("/api/ledgers/999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value(2001))
        .andExpect(jsonPath("$.message").value("账本不存在"));
}
```

---

## 10. 常见问题

### Q1: Controller 层是否需要 try-catch？

**A**: 不需要。全局异常处理器会自动捕获所有异常，Controller 层只需要调用 Service 并返回 Result 即可。

### Q2: 如何自定义错误码？

**A**: 在 `ErrorCode` 枚举中添加新的错误码即可。

### Q3: 如何处理第三方接口调用异常？

**A**: 捕获第三方异常后，转换为自定义的 `BusinessException` 抛出。

```java
try {
    // 调用第三方接口
    wechatService.login(code);
} catch (WechatException e) {
    log.error("微信登录失败", e);
    throw new BusinessException(ErrorCode.WECHAT_LOGIN_FAILED, e.getMessage());
}
```

---

**文档维护**: 本文档应随系统演进及时更新  
**最后更新**: 2025-11-24  
**维护团队**: Core Ledger Team
