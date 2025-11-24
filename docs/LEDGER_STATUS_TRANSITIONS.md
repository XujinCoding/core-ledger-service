# 账本状态转换规则

## 状态定义

核芯账本系统定义了 5 种账本状态，用于管理账单的完整生命周期：

| 状态码 | 枚举值 | 中文名称 | 说明 | 主页面显示 |
|-------|--------|---------|------|-----------|
| 1 | IN_PROGRESS | 进行中 | 账单创建，未收款 | ✅ 显示 |
| 2 | PARTIAL | 部分缴费 | 已收部分款项，继续收款 | ✅ 显示 |
| 3 | CLEARED | 已结清 | 完全缴费或抹零结清 | ❌ 不显示 |
| 4 | ON_CREDIT | 赊账中 | 客户赊账，暂不催收 | ❌ 不显示 |
| 5 | CLOSED | 已关闭 | 作废的账单 | ❌ 不显示 |

---

## 核心业务规则

### 主页面查询规则
**主页面只显示"正在处理中"的账单**：
- ✅ `IN_PROGRESS (1)` - 进行中
- ✅ `PARTIAL (2)` - 部分缴费

**不显示**：
- ❌ `ON_CREDIT (4)` - 赊账中（暂不催收）
- ❌ `CLEARED (3)` - 已结清
- ❌ `CLOSED (5)` - 已关闭

### 客户详情页查询规则
**显示该客户的所有账单**，包括：
- 赊账中的账单（ON_CREDIT）
- 已结清的账单（CLEARED）
- 正在处理的账单（IN_PROGRESS, PARTIAL）

---

## 状态转换图

```
                    创建账单
                       │
                       ▼
                ┌─────────────┐
          ┌────▶│IN_PROGRESS │◀────┐
          │     │  (进行中)   │     │
          │     └──────┬──────┘     │
          │            │            │
          │      ┌─────┼─────┐      │
          │      │     │     │      │
          │   [赊账]  [部分] [全额] │
          │      │   缴费   缴费    │
          │      │     │     │      │
          │      ▼     ▼     │      │
          │  ┌───────────┐   │      │
          │  │ON_CREDIT  │   │      │
          │  │ (赊账中)  │   │      │
          │  └─────┬─────┘   │      │
          │        │         │      │
          │   [来缴费]   [来全部]   │
          │        │      缴清      │
          │        ▼         │      │
          │  ┌───────────┐   │      │
          └──│  PARTIAL  │   │      │
             │(部分缴费) │───┘      │
             └─────┬─────┘          │
                   │                │
             ┌─────┼─────┐          │
             │     │     │          │
          [赊账] [继续] [抹零/     │
                  缴清   全额]      │
             │     │     │          │
             │     │     ▼          │
             │     │ ┌───────────┐  │
             │     │ │ CLEARED   │◀─┘
             │     │ │(已结清)   │
             │     │ └───────────┘
             │     │
             │     └──[全部缴清]───┐
             │                     │
             └────────────────────►│
                                   ▼
                              ┌───────────┐
                              │ CLEARED   │
                              │(已结清)   │
                              └───────────┘

                              ┌───────────┐
                              │  CLOSED   │ (管理员作废)
                              │(已关闭)   │
                              └───────────┘
```

---

## 状态转换规则详解

### 1. 创建账单 → IN_PROGRESS

**触发操作**: `createLedger()`
**业务场景**: 新建账单
**默认状态**: `IN_PROGRESS`

---

### 2. IN_PROGRESS → PARTIAL (首次部分缴费)

**触发操作**: `receivePayment(amount)`
**业务场景**: 客户支付了部分款项
**前置条件**:
- 账单状态为 `IN_PROGRESS`
- `0 < amount < (total_amount - paid_amount)`

**状态变更**:
```java
ledger.setPaidAmount(ledger.getPaidAmount().add(amount));
ledger.setLedgerStatus(LedgerStatus.PARTIAL);
```

**主页面影响**: 仍显示在主页面（继续催收）

---

### 3. IN_PROGRESS → CLEARED (全额缴清)

**触发操作**: `receivePayment(amount)`
**业务场景**: 客户一次性付清所有款项
**前置条件**:
- 账单状态为 `IN_PROGRESS`
- `amount >= (total_amount - paid_amount)`

**状态变更**:
```java
ledger.setPaidAmount(ledger.getTotalAmount());
ledger.setLedgerStatus(LedgerStatus.CLEARED);
```

**主页面影响**: 从主页面移除

---

### 4. IN_PROGRESS → ON_CREDIT (赊账)

**触发操作**: `moveToCredit()`
**业务场景**: 客户说先欠着，暂不收款
**前置条件**:
- 账单状态为 `IN_PROGRESS`

**状态变更**:
```java
ledger.setLedgerStatus(LedgerStatus.ON_CREDIT);
```

**主页面影响**: 从主页面移除（不催收）

---

### 5. PARTIAL → PARTIAL (继续部分缴费)

**触发操作**: `receivePayment(amount)`
**业务场景**: 继续收取部分款项，但未结清
**前置条件**:
- 账单状态为 `PARTIAL`
- 收款后仍有欠款：`paid_amount + amount < total_amount`

**状态变更**:
```java
ledger.setPaidAmount(ledger.getPaidAmount().add(amount));
// 状态保持 PARTIAL
```

**主页面影响**: 仍显示在主页面

---

### 6. PARTIAL → CLEARED (缴清)

**触发操作**: `receivePayment(amount)`
**业务场景**: 收齐所有款项
**前置条件**:
- 账单状态为 `PARTIAL`
- 收款后无欠款：`paid_amount + amount >= total_amount`

**状态变更**:
```java
ledger.setPaidAmount(ledger.getTotalAmount());
ledger.setLedgerStatus(LedgerStatus.CLEARED);
```

**主页面影响**: 从主页面移除

---

### 7. PARTIAL → ON_CREDIT (部分缴费后赊账)

**触发操作**: `moveToCredit()`
**业务场景**: 已收部分款，剩余款项客户说先欠着
**前置条件**:
- 账单状态为 `PARTIAL`
- `paid_amount < total_amount`

**状态变更**:
```java
ledger.setLedgerStatus(LedgerStatus.ON_CREDIT);
// paid_amount 保持不变
```

**主页面影响**: 从主页面移除（不催收剩余款项）

---

### 8. ON_CREDIT → PARTIAL (赊账后来缴费)

**触发操作**: `receivePayment(amount)`
**业务场景**: 之前赊账的账单，客户来还部分款
**前置条件**:
- 账单状态为 `ON_CREDIT`
- `0 < amount < (total_amount - paid_amount)`

**状态变更**:
```java
ledger.setPaidAmount(ledger.getPaidAmount().add(amount));
ledger.setLedgerStatus(LedgerStatus.PARTIAL);
```

**主页面影响**: 重新显示在主页面（继续催收剩余款项）

---

### 9. ON_CREDIT → CLEARED (赊账后全部缴清)

**触发操作**: `receivePayment(amount)`
**业务场景**: 之前赊账的账单，客户全部还清
**前置条件**:
- 账单状态为 `ON_CREDIT`
- `amount >= (total_amount - paid_amount)`

**状态变更**:
```java
ledger.setPaidAmount(ledger.getTotalAmount());
ledger.setLedgerStatus(LedgerStatus.CLEARED);
```

**主页面影响**: 不显示在主页面

---

### 10. IN_PROGRESS / PARTIAL / ON_CREDIT → CLEARED (抹零结清)

**触发操作**: `discountSettle(discountAmount)` ⚠️ **仅管理员**
**业务场景**: 抹零结清（如 192 元收了 190，抹掉 2 元）
**前置条件**:
- 账单状态为 `IN_PROGRESS`、`PARTIAL` 或 `ON_CREDIT`
- `discount_amount + paid_amount + discountAmount >= total_amount`

**状态变更**:
```java
BigDecimal remainingAmount = ledger.getTotalAmount()
    .subtract(ledger.getPaidAmount())
    .subtract(ledger.getDiscountAmount());
ledger.setDiscountAmount(ledger.getDiscountAmount().add(discountAmount));
ledger.setLedgerStatus(LedgerStatus.CLEARED);
```

**主页面影响**: 从主页面移除

---

## 查询示例

### 主页面查询（正在处理的账单）

**SQL**:
```sql
SELECT * FROM ledger
WHERE ledger_status IN (1, 2)  -- IN_PROGRESS, PARTIAL
  AND status = 1
ORDER BY create_instant DESC;
```

**Java**:
```java
public Page<Ledger> getActiveLedgers(Pageable pageable) {
    return ledgerRepository.findByLedgerStatusInAndStatus(
        List.of(LedgerStatus.IN_PROGRESS, LedgerStatus.PARTIAL),
        1,
        pageable
    );
}

// 或使用枚举方法
public boolean shouldShowOnMainPage(Ledger ledger) {
    return ledger.getLedgerStatus().isActive();
}
```

---

### 客户详情页查询（该客户所有账单）

**SQL**:
```sql
SELECT * FROM ledger
WHERE customer_id = ?
  AND status = 1
ORDER BY create_instant DESC;
```

**Java**:
```java
public List<Ledger> getCustomerAllLedgers(Long customerId) {
    return ledgerRepository.findByCustomerIdAndStatus(customerId, 1);
}
```

---

### 赊账管理查询（所有赊账账单）

**SQL**:
```sql
SELECT l.*, c.name AS customer_name
FROM ledger l
LEFT JOIN customer c ON l.customer_id = c.id
WHERE l.ledger_status = 4  -- ON_CREDIT
  AND l.status = 1
ORDER BY l.create_instant DESC;
```

**Java**:
```java
public List<Ledger> getCreditLedgers() {
    return ledgerRepository.findByLedgerStatusAndStatus(
        LedgerStatus.ON_CREDIT,
        1
    );
}
```

---

## 业务方法对照表

| 方法名 | 说明 | 返回 true 的状态 |
|-------|------|-----------------|
| `isActive()` | 是否在主页面显示 | IN_PROGRESS, PARTIAL |
| `canReceivePayment()` | 是否可以收款 | IN_PROGRESS, PARTIAL, ON_CREDIT |
| `canMoveToCredit()` | 是否可以转赊账 | IN_PROGRESS, PARTIAL |
| `canDiscountSettle()` | 是否可以抹零结清 | IN_PROGRESS, PARTIAL, ON_CREDIT |
| `isFinalState()` | 是否为终态 | CLEARED, CLOSED |

---

## 终态说明

以下两种状态为**终态**（不可再转换）：
- **CLEARED** - 已结清，业务正常完成
- **CLOSED** - 已关闭，账单作废

终态账单不再参与任何状态转换，也不允许接收新的支付记录。

---

## 异常场景处理

### 1. 终态账单收款
**场景**: 对 `CLEARED` 或 `CLOSED` 状态的账单调用 `receivePayment()`
**处理**: 抛出 `IllegalStateException`，提示"账单已完结，无法收款"

### 2. 超额支付
**场景**: 收款金额超过剩余应收
**处理**: 自动调整为剩余金额，状态变为 `CLEARED`

### 3. 非法状态转换
**场景**: 尝试将 `CLEARED` 或 `CLOSED` 转为其他状态
**处理**: 抛出 `IllegalStateException`，提示"终态账单无法修改"

### 4. 非管理员抹零
**场景**: 普通用户尝试调用 `discountSettle()`
**处理**: 抛出 `AccessDeniedException`，提示"仅管理员可执行抹零操作"

---

## 状态机代码示例

```java
@Service
@RequiredArgsConstructor
public class LedgerStateMachine {

    /**
     * 收款操作
     */
    @Transactional
    public void receivePayment(Ledger ledger, BigDecimal amount, PaymentMethod method) {
        if (!ledger.getLedgerStatus().canReceivePayment()) {
            throw new IllegalStateException("当前状态不允许收款");
        }

        BigDecimal newPaidAmount = ledger.getPaidAmount().add(amount);
        BigDecimal totalDue = ledger.getTotalAmount().subtract(ledger.getDiscountAmount());

        if (newPaidAmount.compareTo(totalDue) >= 0) {
            // 已结清
            ledger.setPaidAmount(totalDue);
            ledger.setLedgerStatus(LedgerStatus.CLEARED);
        } else {
            // 部分缴费
            ledger.setPaidAmount(newPaidAmount);
            ledger.setLedgerStatus(LedgerStatus.PARTIAL);
        }

        // 记录支付流水
        PaymentRecord record = new PaymentRecord();
        record.setLedgerId(ledger.getId());
        record.setAmount(amount);
        record.setPaymentMethod(method);
        paymentRecordRepository.save(record);
    }

    /**
     * 转为赊账
     */
    @Transactional
    public void moveToCredit(Ledger ledger) {
        if (!ledger.getLedgerStatus().canMoveToCredit()) {
            throw new IllegalStateException("当前状态不允许转为赊账");
        }
        ledger.setLedgerStatus(LedgerStatus.ON_CREDIT);
    }

    /**
     * 抹零结清（仅管理员）
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void discountSettle(Ledger ledger) {
        if (!ledger.getLedgerStatus().canDiscountSettle()) {
            throw new IllegalStateException("当前状态不允许抹零结清");
        }

        BigDecimal remainingAmount = ledger.getTotalAmount()
            .subtract(ledger.getPaidAmount())
            .subtract(ledger.getDiscountAmount());

        ledger.setDiscountAmount(ledger.getDiscountAmount().add(remainingAmount));
        ledger.setLedgerStatus(LedgerStatus.CLEARED);
    }
}
```

---

## 业务场景示例

### 场景 1：完全缴费
```
1. 创建账单 192 元 → IN_PROGRESS (显示在主页面)
2. 客户支付 192 元 → CLEARED (从主页面移除)
```

### 场景 2：部分缴费后缴清
```
1. 创建账单 192 元 → IN_PROGRESS (显示在主页面)
2. 客户支付 100 元 → PARTIAL (仍在主页面)
3. 客户支付 92 元 → CLEARED (从主页面移除)
```

### 场景 3：抹零结清
```
1. 创建账单 192 元 → IN_PROGRESS (显示在主页面)
2. 客户支付 190 元 → PARTIAL (仍在主页面)
3. 管理员抹零 2 元 → CLEARED (discount_amount=2, 从主页面移除)
```

### 场景 4：先赊账，后缴费
```
1. 创建账单 192 元 → IN_PROGRESS (显示在主页面)
2. 客户说赊账 → ON_CREDIT (从主页面移除)
3. 客户来还 100 元 → PARTIAL (重新显示在主页面)
4. 客户还清 92 元 → CLEARED (从主页面移除)
```

### 场景 5：部分缴费后赊账
```
1. 创建账单 192 元 → IN_PROGRESS (显示在主页面)
2. 客户支付 100 元 → PARTIAL (仍在主页面)
3. 客户说剩余的先欠着 → ON_CREDIT (从主页面移除)
4. 客户来还剩余 92 元 → CLEARED (不显示在主页面)
```

---

**文档版本**: 2.0.0
**最后更新**: 2025-11-24
**维护团队**: Core Ledger Team
