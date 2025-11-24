# Core Ledger API 接口设计文档

## 1. 接口规范

### 1.1 基础约定

- **Base URL**: `/api`
- **Content-Type**: `application/json`
- **字符编码**: `UTF-8`
- **时间格式**: `yyyy-MM-dd HH:mm:ss`
- **时区**: `Asia/Shanghai`

### 1.2 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1700000000000
}
```

### 1.3 HTTP 状态码

| 状态码 | 说明 |
|-------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未授权 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器错误 |

---

## 2. 账本管理 API

### 2.1 创建账本

**接口**: `POST /api/ledgers`

**描述**: 创建新账本（默认状态为 IN_PROGRESS）

**请求体**:
```json
{
  "customerId": 1,
  "items": [
    {
      "productId": 10,
      "price": 50.00,
      "quantity": 2
    },
    {
      "productId": 11,
      "price": 45.00,
      "quantity": 1
    }
  ],
  "memo": "备注信息"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 100,
    "customerId": 1,
    "customerName": "张三",
    "totalAmount": 145.00,
    "paidAmount": 0.00,
    "discountAmount": 0.00,
    "ledgerStatus": 1,
    "ledgerStatusDesc": "进行中",
    "items": [
      {
        "id": 1,
        "productId": 10,
        "productName": "商品A",
        "price": 50.00,
        "quantity": 2
      }
    ],
    "createInstant": "2025-11-24 10:00:00"
  },
  "timestamp": 1700000000000
}
```

---

### 2.2 查询主页面账本列表（活跃账单）

**接口**: `GET /api/ledgers/active`

**描述**: 查询主页面显示的账单（状态为 IN_PROGRESS 或 PARTIAL）

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| page | Integer | 否 | 页码，默认 0 |
| size | Integer | 否 | 每页数量，默认 20 |
| customerName | String | 否 | 客户姓名（模糊查询） |

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "id": 100,
        "customerName": "张三",
        "customerPhone": "13800138000",
        "totalAmount": 145.00,
        "paidAmount": 50.00,
        "remainingAmount": 95.00,
        "ledgerStatus": 2,
        "ledgerStatusDesc": "部分缴费",
        "createInstant": "2025-11-24 10:00:00"
      }
    ],
    "totalElements": 50,
    "totalPages": 3,
    "number": 0,
    "size": 20
  },
  "timestamp": 1700000000000
}
```

---

### 2.3 查询客户账本列表（所有状态）

**接口**: `GET /api/ledgers/customer/{customerId}`

**描述**: 查询指定客户的所有账单（包括赊账和已结清）

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| customerId | Long | 是 | 客户ID |

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| page | Integer | 否 | 页码，默认 0 |
| size | Integer | 否 | 每页数量，默认 20 |

**响应**: 同 2.2

---

### 2.4 查询赊账账单列表

**接口**: `GET /api/ledgers/credit`

**描述**: 查询所有赊账中的账单（状态为 ON_CREDIT）

**请求参数**: 同 2.2

**响应**: 同 2.2

---

### 2.5 查询账本详情

**接口**: `GET /api/ledgers/{id}`

**描述**: 查询账本详细信息（含明细和支付记录）

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| id | Long | 是 | 账本ID |

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 100,
    "customerId": 1,
    "customerName": "张三",
    "customerPhone": "13800138000",
    "totalAmount": 145.00,
    "paidAmount": 50.00,
    "discountAmount": 0.00,
    "remainingAmount": 95.00,
    "ledgerStatus": 2,
    "ledgerStatusDesc": "部分缴费",
    "items": [
      {
        "id": 1,
        "productId": 10,
        "productName": "商品A",
        "price": 50.00,
        "quantity": 2,
        "subtotal": 100.00
      },
      {
        "id": 2,
        "productId": 11,
        "productName": "商品B",
        "price": 45.00,
        "quantity": 1,
        "subtotal": 45.00
      }
    ],
    "paymentRecords": [
      {
        "id": 1,
        "amount": 50.00,
        "paymentMethod": 1,
        "paymentMethodDesc": "现金",
        "createInstant": "2025-11-24 11:00:00"
      }
    ],
    "memo": "备注信息",
    "createInstant": "2025-11-24 10:00:00",
    "modifyInstant": "2025-11-24 11:00:00"
  },
  "timestamp": 1700000000000
}
```

---

### 2.6 收款操作

**接口**: `POST /api/ledgers/{id}/payment`

**描述**: 对账单进行收款（支持 IN_PROGRESS, PARTIAL, ON_CREDIT 状态）

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| id | Long | 是 | 账本ID |

**请求体**:
```json
{
  "amount": 50.00,
  "paymentMethod": 1,
  "memo": "现金支付"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "收款成功",
  "data": {
    "id": 100,
    "paidAmount": 100.00,
    "remainingAmount": 45.00,
    "ledgerStatus": 2,
    "ledgerStatusDesc": "部分缴费"
  },
  "timestamp": 1700000000000
}
```

---

### 2.7 转为赊账

**接口**: `POST /api/ledgers/{id}/move-to-credit`

**描述**: 将账单转为赊账状态（支持 IN_PROGRESS 和 PARTIAL 状态）

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| id | Long | 是 | 账本ID |

**响应**:
```json
{
  "code": 200,
  "message": "已转为赊账",
  "data": {
    "id": 100,
    "ledgerStatus": 4,
    "ledgerStatusDesc": "赊账中"
  },
  "timestamp": 1700000000000
}
```

---

### 2.8 抹零结清（管理员）

**接口**: `POST /api/ledgers/{id}/discount-settle`

**描述**: 管理员抹零结清账单（如 192 元收 190，抹掉 2 元）

**权限**: 仅管理员

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| id | Long | 是 | 账本ID |

**请求体**:
```json
{
  "discountAmount": 2.00,
  "memo": "抹零2元"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "抹零结清成功",
  "data": {
    "id": 100,
    "totalAmount": 192.00,
    "paidAmount": 190.00,
    "discountAmount": 2.00,
    "ledgerStatus": 3,
    "ledgerStatusDesc": "已结清"
  },
  "timestamp": 1700000000000
}
```

---

### 2.9 关闭账单（管理员）

**接口**: `POST /api/ledgers/{id}/close`

**描述**: 管理员作废账单

**权限**: 仅管理员

**响应**:
```json
{
  "code": 200,
  "message": "账单已关闭",
  "data": {
    "id": 100,
    "ledgerStatus": 5,
    "ledgerStatusDesc": "已关闭"
  },
  "timestamp": 1700000000000
}
```

---

## 3. 客户管理 API

### 3.1 创建客户

**接口**: `POST /api/customers`

**请求体**:
```json
{
  "name": "张三",
  "phone": "13800138000",
  "alias": "老张",
  "gender": 1,
  "age": 35,
  "addressId": 12345,
  "addressDetail": "某某小区1栋101",
  "memo": "备注"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "张三",
    "phone": "13800138000",
    "alias": "老张",
    "gender": 1,
    "genderDesc": "男",
    "age": 35,
    "addressId": 12345,
    "addressDetail": "某某小区1栋101",
    "fullAddress": "广东省-深圳市-南山区-西丽街道-留仙村-某某小区1栋101",
    "createInstant": "2025-11-24 10:00:00"
  },
  "timestamp": 1700000000000
}
```

---

### 3.2 查询客户列表

**接口**: `GET /api/customers`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| page | Integer | 否 | 页码，默认 0 |
| size | Integer | 否 | 每页数量，默认 20 |
| keyword | String | 否 | 姓名/手机号（模糊查询） |
| addressId | Long | 否 | 地址ID（查询某村的客户） |

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "张三",
        "phone": "13800138000",
        "alias": "老张",
        "gender": 1,
        "genderDesc": "男",
        "debtAmount": 95.00,
        "creditLedgerCount": 2
      }
    ],
    "totalElements": 100,
    "totalPages": 5,
    "number": 0,
    "size": 20
  },
  "timestamp": 1700000000000
}
```

---

### 3.3 查询客户详情

**接口**: `GET /api/customers/{id}`

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "张三",
    "phone": "13800138000",
    "alias": "老张",
    "gender": 1,
    "genderDesc": "男",
    "age": 35,
    "fullAddress": "广东省-深圳市-南山区-西丽街道-留仙村-某某小区1栋101",
    "totalDebt": 95.00,
    "totalCredit": 200.00,
    "ledgerSummary": {
      "inProgressCount": 5,
      "partialCount": 3,
      "creditCount": 2,
      "clearedCount": 10
    },
    "createInstant": "2025-11-24 10:00:00"
  },
  "timestamp": 1700000000000
}
```

---

## 4. 商品管理 API

### 4.1 创建商品

**接口**: `POST /api/products`

**请求体**:
```json
{
  "categoryId": 1,
  "name": "商品A",
  "price": 50.00,
  "spec": "规格说明",
  "unit": "件",
  "memo": "备注"
}
```

---

### 4.2 查询商品列表

**接口**: `GET /api/products`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| page | Integer | 否 | 页码，默认 0 |
| size | Integer | 否 | 每页数量，默认 20 |
| categoryId | Long | 否 | 分类ID |
| keyword | String | 否 | 商品名称（模糊查询） |

---

### 4.3 查询商品详情

**接口**: `GET /api/products/{id}`

---

## 5. 统计报表 API

### 5.1 账本统计

**接口**: `GET /api/statistics/ledger`

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalLedgers": 100,
    "inProgressCount": 30,
    "partialCount": 20,
    "creditCount": 15,
    "clearedCount": 30,
    "closedCount": 5,
    "totalDebt": 50000.00,
    "totalCredit": 30000.00,
    "todayLedgers": 10,
    "todayAmount": 5000.00
  },
  "timestamp": 1700000000000
}
```

---

### 5.2 客户欠款排行

**接口**: `GET /api/statistics/customer-debt`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| limit | Integer | 否 | 返回数量，默认 10 |

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "customerId": 1,
      "customerName": "张三",
      "totalDebt": 5000.00,
      "ledgerCount": 5
    }
  ],
  "timestamp": 1700000000000
}
```

---

## 6. 地址库 API

### 6.1 查询子级地址

**接口**: `GET /api/addresses`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| parentId | Long | 否 | 父级ID，默认 0（查询省级） |

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "广东省",
      "level": 1,
      "hasChildren": true
    }
  ],
  "timestamp": 1700000000000
}
```

---

## 7. 支付流水 API

### 7.1 查询支付流水

**接口**: `GET /api/payment-records`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| page | Integer | 否 | 页码，默认 0 |
| size | Integer | 否 | 每页数量，默认 20 |
| ledgerId | Long | 否 | 账本ID |
| customerId | Long | 否 | 客户ID |
| startDate | String | 否 | 开始日期 |
| endDate | String | 否 | 结束日期 |

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "ledgerId": 100,
        "customerName": "张三",
        "amount": 50.00,
        "paymentMethod": 1,
        "paymentMethodDesc": "现金",
        "memo": "现金支付",
        "createInstant": "2025-11-24 11:00:00"
      }
    ],
    "totalElements": 50,
    "totalPages": 3,
    "number": 0,
    "size": 20
  },
  "timestamp": 1700000000000
}
```

---

## 8. 错误码说明

| 错误码 | 说明 |
|-------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未登录 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 1001 | 客户不存在 |
| 1002 | 手机号已存在 |
| 2001 | 账本不存在 |
| 2002 | 账本状态不允许此操作 |
| 2003 | 支付金额超过应收金额 |
| 3001 | 商品不存在 |
| 3002 | 商品分类不存在 |
| 5001 | 仅管理员可操作 |

---

**文档版本**: 1.0.0
**最后更新**: 2025-11-24
**维护团队**: Core Ledger Team
