# 商品属性批量更新API文档

## 📋 接口概览

### 新增推荐接口 ⭐
**接口地址**：`PUT /api/products/{id}/attrs/batch`

**功能说明**：
- ✅ 一次性提交商品的所有属性和属性值
- ✅ 后端自动识别新增、修改、删除操作
- ✅ **增量更新SKU**（保留未变化的SKU ID和定价）
- ✅ 只需一次HTTP请求

---

## 🎯 核心优势

### 优化前的问题
```bash
# ❌ 需要多次请求
POST /api/products/1/attrs                      # 添加属性
POST /api/products/1/attrs/1/values             # 添加属性值
POST /api/products/1/attrs/1/values             # 添加属性值
POST /api/products/1/attrs/2/values             # 添加属性值
DELETE /api/products/1/attrs/3/values/5         # 删除属性值
# 每次操作都会触发SKU全删全加，导致SKU ID变化
```

### 优化后的方案
```bash
# ✅ 只需一次请求
PUT /api/products/1/attrs/batch
# 一次性提交所有属性和属性值
# 后端智能识别变化并增量更新SKU
```

### 增量更新SKU的好处
1. **保留SKU ID**：未变化的SKU保持原有ID
2. **保留定价信息**：已定价的SKU价格不受影响
3. **避免业务中断**：其他模块引用的SKU ID不会失效
4. **性能更好**：只处理变化的部分

---

## 📝 API详细说明

### 请求方法
```
PUT /api/products/{id}/attrs/batch
```

### 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 商品ID（路径参数） |
| attrs | List | 是 | 属性列表（请求体） |

### 请求体结构

```json
{
  "attrs": [
    {
      "id": null或Long,           // null=新增，非null=修改
      "attrName": "属性名称",
      "sortOrder": 0,
      "values": [
        {
          "id": null或Long,       // null=新增，非null=修改
          "value": "属性值",
          "sortOrder": 0
        }
      ]
    }
  ]
}
```

### 判断规则

| 场景 | ID状态 | 后端行为 |
|------|--------|----------|
| **新增属性** | `id: null` | 创建新属性 |
| **修改属性** | `id: 存在` 且数据变化 | 更新属性 |
| **删除属性** | 数据库有但未传入 | 逻辑删除 |
| **不变** | `id: 存在` 且数据未变化 | 跳过 |
| **新增属性值** | `id: null` | 创建新属性值 |
| **修改属性值** | `id: 存在` 且数据变化 | 更新属性值 |
| **删除属性值** | 数据库有但未传入 | 逻辑删除 |

---

## 💡 使用示例

### 场景1：初次创建属性和属性值

**请求示例**：
```json
PUT /api/products/1/attrs/batch

{
  "attrs": [
    {
      "id": null,
      "attrName": "重量",
      "sortOrder": 0,
      "values": [
        { "id": null, "value": "5斤", "sortOrder": 0 },
        { "id": null, "value": "10斤", "sortOrder": 1 }
      ]
    },
    {
      "id": null,
      "attrName": "等级",
      "sortOrder": 1,
      "values": [
        { "id": null, "value": "一级", "sortOrder": 0 },
        { "id": null, "value": "二级", "sortOrder": 1 }
      ]
    }
  ]
}
```

**后端行为**：
- ✅ 创建2个属性：重量、等级
- ✅ 创建4个属性值：5斤、10斤、一级、二级
- ✅ 自动生成4个SKU（2×2笛卡尔积）

**生成的SKU**：
```
SKU1 (ID=1): 红富士苹果-5斤-一级  (UNPRICED, ¥0.00)
SKU2 (ID=2): 红富士苹果-5斤-二级  (UNPRICED, ¥0.00)
SKU3 (ID=3): 红富士苹果-10斤-一级 (UNPRICED, ¥0.00)
SKU4 (ID=4): 红富士苹果-10斤-二级 (UNPRICED, ¥0.00)
```

---

### 场景2：新增属性值（增量生成SKU）

假设用户给SKU1、SKU2定价后，想增加"15斤"的规格。

**请求示例**：
```json
PUT /api/products/1/attrs/batch

{
  "attrs": [
    {
      "id": 1,
      "attrName": "重量",
      "sortOrder": 0,
      "values": [
        { "id": 1, "value": "5斤", "sortOrder": 0 },
        { "id": 2, "value": "10斤", "sortOrder": 1 },
        { "id": null, "value": "15斤", "sortOrder": 2 }  // ← 新增
      ]
    },
    {
      "id": 2,
      "attrName": "等级",
      "sortOrder": 1,
      "values": [
        { "id": 3, "value": "一级", "sortOrder": 0 },
        { "id": 4, "value": "二级", "sortOrder": 1 }
      ]
    }
  ]
}
```

**后端行为**：
- ✅ 保留原有4个SKU（SKU1-4的ID和定价不变）
- ✅ 新增2个SKU（15斤×2个等级）

**SKU状态**：
```
SKU1 (ID=1): 红富士苹果-5斤-一级   (PRICED, ¥25.00) ← 保留
SKU2 (ID=2): 红富士苹果-5斤-二级   (PRICED, ¥24.00) ← 保留
SKU3 (ID=3): 红富士苹果-10斤-一级  (UNPRICED, ¥0.00) ← 保留
SKU4 (ID=4): 红富士苹果-10斤-二级  (UNPRICED, ¥0.00) ← 保留
SKU5 (ID=5): 红富士苹果-15斤-一级  (UNPRICED, ¥0.00) ← 新增
SKU6 (ID=6): 红富士苹果-15斤-二级  (UNPRICED, ¥0.00) ← 新增
```

**关键点**：
- ✅ SKU1和SKU2的ID保持不变（ID=1, ID=2）
- ✅ SKU1和SKU2的定价保留（¥25.00, ¥24.00）
- ✅ 只新增了2个未定价的SKU

---

### 场景3：删除属性值（删除对应SKU）

假设用户不再销售"二级"品质的苹果。

**请求示例**：
```json
PUT /api/products/1/attrs/batch

{
  "attrs": [
    {
      "id": 1,
      "attrName": "重量",
      "sortOrder": 0,
      "values": [
        { "id": 1, "value": "5斤", "sortOrder": 0 },
        { "id": 2, "value": "10斤", "sortOrder": 1 },
        { "id": 5, "value": "15斤", "sortOrder": 2 }
      ]
    },
    {
      "id": 2,
      "attrName": "等级",
      "sortOrder": 1,
      "values": [
        { "id": 3, "value": "一级", "sortOrder": 0 }
        // ← "二级"未传入，会被删除
      ]
    }
  ]
}
```

**后端行为**：
- ✅ 删除属性值：二级（ID=4）
- ✅ 删除包含"二级"的SKU（SKU2, SKU4, SKU6）
- ✅ 保留包含"一级"的SKU（SKU1, SKU3, SKU5）

**SKU状态**：
```
SKU1 (ID=1): 红富士苹果-5斤-一级   (PRICED, ¥25.00)   ← 保留
SKU2 (ID=2): [已删除]                                 ← 删除
SKU3 (ID=3): 红富士苹果-10斤-一级  (UNPRICED, ¥0.00) ← 保留
SKU4 (ID=4): [已删除]                                 ← 删除
SKU5 (ID=5): 红富士苹果-15斤-一级  (UNPRICED, ¥0.00) ← 保留
SKU6 (ID=6): [已删除]                                 ← 删除
```

---

### 场景4：修改属性名称（更新SKU名称）

假设用户想把"重量"改为"规格"。

**请求示例**：
```json
PUT /api/products/1/attrs/batch

{
  "attrs": [
    {
      "id": 1,
      "attrName": "规格",  // ← 修改了属性名称
      "sortOrder": 0,
      "values": [
        { "id": 1, "value": "5斤", "sortOrder": 0 },
        { "id": 2, "value": "10斤", "sortOrder": 1 },
        { "id": 5, "value": "15斤", "sortOrder": 2 }
      ]
    },
    {
      "id": 2,
      "attrName": "等级",
      "sortOrder": 1,
      "values": [
        { "id": 3, "value": "一级", "sortOrder": 0 }
      ]
    }
  ]
}
```

**后端行为**：
- ✅ 更新属性名称：重量 → 规格
- ✅ 更新所有SKU的名称（但ID不变）

**SKU状态**：
```
SKU1 (ID=1): 红富士苹果-5斤-一级   (PRICED, ¥25.00) ← ID和定价不变
SKU3 (ID=3): 红富士苹果-10斤-一级  (UNPRICED, ¥0.00)
SKU5 (ID=5): 红富士苹果-15斤-一级  (UNPRICED, ¥0.00)
```

---

### 场景5：删除整个属性

假设用户决定不再区分"等级"。

**请求示例**：
```json
PUT /api/products/1/attrs/batch

{
  "attrs": [
    {
      "id": 1,
      "attrName": "规格",
      "sortOrder": 0,
      "values": [
        { "id": 1, "value": "5斤", "sortOrder": 0 },
        { "id": 2, "value": "10斤", "sortOrder": 1 },
        { "id": 5, "value": "15斤", "sortOrder": 2 }
      ]
    }
    // ← "等级"属性未传入，会被删除
  ]
}
```

**后端行为**：
- ✅ 删除属性：等级（ID=2）
- ✅ 删除该属性的所有属性值
- ✅ 重新生成SKU（因为属性结构变化）

**SKU状态**：
```
SKU1 (ID=1): 红富士苹果-5斤   (PRICED, ¥25.00) ← 保留，价格保留
SKU3 (ID=3): 红富士苹果-10斤  (UNPRICED, ¥0.00) ← 保留
SKU5 (ID=5): 红富士苹果-15斤  (UNPRICED, ¥0.00) ← 保留
```

---

## 🔄 响应示例

### 成功响应

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "productId": 1,
      "attrName": "规格",
      "sortOrder": 0,
      "values": [
        { "id": 1, "value": "5斤", "sortOrder": 0 },
        { "id": 2, "value": "10斤", "sortOrder": 1 },
        { "id": 5, "value": "15斤", "sortOrder": 2 }
      ]
    }
  ],
  "timestamp": 1732682400000
}
```

### 错误响应

```json
{
  "code": 3001,
  "message": "商品不存在",
  "timestamp": 1732682400000
}
```

---

## 📊 增量更新SKU算法

### 核心原理

1. **构建SKU唯一键**：`属性ID1:属性值ID1,属性ID2:属性值ID2`
   - 示例：`1:1,2:3` 表示"重量:5斤,等级:一级"

2. **对比现有SKU**：
   - 现有SKU也构建相同格式的键
   - 通过键匹配来判断SKU是否已存在

3. **处理策略**：
   - 键匹配 → SKU已存在 → 保留ID，更新名称（如需要）
   - 键不匹配 → 新SKU → 创建新记录
   - 现有键在新组合中不存在 → SKU已废弃 → 逻辑删除

### 示例

**现有SKU**：
```
SKU1: key="1:1,2:3" (5斤-一级)
SKU2: key="1:1,2:4" (5斤-二级)
SKU3: key="1:2,2:3" (10斤-一级)
SKU4: key="1:2,2:4" (10斤-二级)
```

**新增属性值后**：
```
新增属性值：15斤 (id=5)
```

**生成的新组合**：
```
key="1:1,2:3" → 匹配SKU1 → 保留
key="1:1,2:4" → 匹配SKU2 → 保留
key="1:2,2:3" → 匹配SKU3 → 保留
key="1:2,2:4" → 匹配SKU4 → 保留
key="1:5,2:3" → 无匹配 → 创建SKU5
key="1:5,2:4" → 无匹配 → 创建SKU6
```

**结果**：
- ✅ 保留4个原有SKU（ID不变）
- ✅ 新增2个SKU
- ✅ 总计6个SKU

---

## ⚠️ 注意事项

### 1. 属性值必填
每个属性必须至少有一个属性值，否则无法生成SKU。

### 2. ID的含义
- `id: null` → 新增
- `id: 存在` → 修改
- 不传递 → 删除（数据库中存在但请求中不存在）

### 3. 变更检测
只有真正有变化的数据才会触发数据库操作和SKU更新。

### 4. 定价保留
已定价的SKU即使经过增量更新，价格和定价状态也会保留。

### 5. 事务保证
整个批量更新在一个事务中执行，要么全部成功，要么全部回滚。

---

## 🔧 旧接口迁移指南

### 已废弃的接口（仍可用但不推荐）

| 旧接口 | 新接口 | 说明 |
|--------|--------|------|
| `POST /api/products/{id}/attrs` | `PUT /api/products/{id}/attrs/batch` | 添加属性 |
| `PUT /api/products/{id}/attrs/{attrId}` | `PUT /api/products/{id}/attrs/batch` | 修改属性 |
| `DELETE /api/products/{id}/attrs/{attrId}` | `PUT /api/products/{id}/attrs/batch` | 删除属性 |
| `POST /api/products/{id}/attrs/{attrId}/values` | `PUT /api/products/{id}/attrs/batch` | 添加属性值 |
| `PUT /api/products/{id}/attrs/{attrId}/values/{valueId}` | `PUT /api/products/{id}/attrs/batch` | 修改属性值 |
| `DELETE /api/products/{id}/attrs/{attrId}/values/{valueId}` | `PUT /api/products/{id}/attrs/batch` | 删除属性值 |
| `POST /api/products/{id}/attrs/regenerate-skus` | `PUT /api/products/{id}/attrs/batch` | 重新生成SKU |

### 迁移步骤

1. **获取当前属性数据**：
   ```bash
   GET /api/products/{id}/attrs
   ```

2. **修改前端代码**：
   - 改为调用批量更新接口
   - 一次性提交所有属性和属性值

3. **测试验证**：
   - 验证SKU ID是否保留
   - 验证定价信息是否保留

---

## 🎉 总结

### 核心优势
1. ✅ **一次请求完成**：减少HTTP请求次数
2. ✅ **智能识别变化**：自动判断新增/修改/删除
3. ✅ **增量更新SKU**：保留未变化的SKU ID和定价
4. ✅ **事务保证**：保证数据一致性
5. ✅ **性能优化**：只处理变化的部分

### 推荐使用场景
- ✅ 商品编辑页面：一次性提交所有属性
- ✅ 批量导入：批量更新多个商品的属性
- ✅ 属性管理：统一的属性管理界面

### 不推荐场景
- ❌ 只查询属性：使用 `GET /api/products/{id}/attrs`
- ❌ 单个商品CRUD：使用商品的基础接口

**建议**：前端全部迁移到新的批量更新接口，旧接口将在未来版本中移除。
