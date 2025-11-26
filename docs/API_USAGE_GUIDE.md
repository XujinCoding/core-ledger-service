# Core Ledger 前端接口使用指南

## 目录
- [1. 接口概览](#1-接口概览)
- [2. 通用规范](#2-通用规范)
- [3. 客户模块接口](#3-客户模块接口)
- [4. 商品模块接口](#4-商品模块接口)
- [5. 账单模块接口](#5-账单模块接口)
- [6. 典型业务流程](#6-典型业务流程)
- [7. 错误处理](#7-错误处理)

---

## 1. 接口概览

### 1.1 基础信息
- **Base URL**: `http://localhost:8080/api`
- **API文档**: `http://localhost:8080/doc.html` (Knife4j)
- **Content-Type**: `application/json`
- **字符编码**: `UTF-8`

### 1.2 模块清单

| 模块 | 接口数 | 说明 |
|-----|-------|------|
| 客户管理 | 5 | 客户CRUD、欠款统计 |
| 商品管理 | 8 | SPU/SKU管理、动态属性 |
| 账单管理 | 10 | 开单、收款、赊账、结清 |

---

## 2. 通用规范

### 2.1 统一响应格式

**所有接口都返回以下格式：**
```json
{
  "code": 200,              // 响应码：200=成功, 其他=失败
  "message": "操作成功",     // 响应消息
  "data": {},               // 响应数据（可能为null）
  "timestamp": 1700000000000 // 时间戳
}
```

**成功示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "name": "张三"
  },
  "timestamp": 1700000000000
}
```

**失败示例：**
```json
{
  "code": 400,
  "message": "手机号已存在",
  "data": null,
  "timestamp": 1700000000000
}
```

### 2.2 分页响应格式

**分页查询接口返回Spring Data的Page对象：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "content": [            // 当前页数据
      { "id": 1, "name": "张三" },
      { "id": 2, "name": "李四" }
    ],
    "pageable": {
      "pageNumber": 0,      // 当前页码（从0开始）
      "pageSize": 20,       // 每页大小
      "sort": {
        "sorted": true,
        "unsorted": false,
        "empty": false
      },
      "offset": 0,
      "paged": true,
      "unpaged": false
    },
    "totalElements": 100,   // 总记录数
    "totalPages": 5,        // 总页数
    "size": 20,             // 每页大小
    "number": 0,            // 当前页码（从0开始）
    "numberOfElements": 20, // 当前页记录数
    "first": true,          // 是否第一页
    "last": false,          // 是否最后一页
    "empty": false          // 是否为空
  }
}
```

**前端处理示例：**
```javascript
// Vue 3 示例
const fetchCustomers = async (page = 0, size = 20) => {
  const response = await api.get('/customers', {
    params: { page, size }
  });

  if (response.data.code === 200) {
    const pageData = response.data.data;
    customers.value = pageData.content;          // 当前页数据
    totalRecords.value = pageData.totalElements;  // 总记录数
    currentPage.value = pageData.number;          // 当前页码
  }
};
```

### 2.3 分页参数

**所有列表接口都支持以下查询参数：**
- `page`: 页码（从0开始，默认0）
- `size`: 每页大小（默认20）
- `sort`: 排序字段（如：`createInstant,desc`）

**示例：**
```http
GET /api/customers?page=0&size=20&sort=createInstant,desc
```

### 2.4 常见HTTP状态码

| 状态码 | 说明 | 前端处理建议 |
|-------|------|------------|
| 200 | 成功 | 正常处理data |
| 400 | 请求参数错误 | 提示用户，检查表单 |
| 401 | 未登录/Token过期 | 跳转登录页 |
| 403 | 无权限 | 提示权限不足 |
| 404 | 资源不存在 | 提示数据不存在 |
| 500 | 服务器错误 | 提示系统错误，联系管理员 |

---

## 3. 客户模块接口

### 3.1 创建客户

**接口：** `POST /api/customers`

**请求示例：**
```json
{
  "name": "张三",
  "phone": "13800138000",
  "gender": 1,                // 1=男, 2=女
  "addressId": 1001,          // 地址ID（村级）
  "addressDetail": "XX小区1栋2单元301",
  "memo": "VIP客户"
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "name": "张三",
    "phone": "13800138000",
    "gender": 1,
    "genderDesc": "男",
    "addressId": 1001,
    "addressDetail": "XX小区1栋2单元301",
    "fullAddress": "广东省-深圳市-南山区-XX街道-XX村",  // TODO: 待实现
    "customerType": 1,
    "customerTypeDesc": "活跃客户",
    "memo": "VIP客户",
    "ledgerSummary": {         // 账单统计（新客户为0）
      "inProgressCount": 0,
      "inProgressAmount": 0,
      "partialCount": 0,
      "partialAmount": 0,
      "partialPaidAmount": 0,
      "creditCount": 0,
      "creditAmount": 0,
      "clearedCount": 0,
      "totalDebt": 0,
      "totalCredit": 0
    },
    "createInstant": "2025-01-25 10:00:00",
    "modifyInstant": "2025-01-25 10:00:00"
  }
}
```

**字段说明：**
- `customerType`: 客户类型 (0=潜在客户, 1=活跃客户)
- `ledgerSummary`: 账单统计
  - `inProgressCount`: 进行中账单数
  - `partialCount`: 部分缴费账单数
  - `creditCount`: 赊账账单数
  - `clearedCount`: 已结清账单数
  - `totalDebt`: 总欠款（进行中+部分缴费）
  - `totalCredit`: 总赊账

**前端实现示例（Vue 3）：**
```vue
<script setup>
import { ref } from 'vue';
import { ElMessage } from 'element-plus';

const form = ref({
  name: '',
  phone: '',
  gender: 1,
  addressId: null,
  addressDetail: '',
  memo: ''
});

const submitForm = async () => {
  try {
    const response = await api.post('/customers', form.value);

    if (response.data.code === 200) {
      ElMessage.success('客户创建成功');
      // 跳转到客户详情页或列表页
      router.push(`/customers/${response.data.data.id}`);
    } else {
      ElMessage.error(response.data.message);
    }
  } catch (error) {
    ElMessage.error('创建失败，请重试');
  }
};
</script>
```

### 3.2 查询客户列表

**接口：** `GET /api/customers`

**查询参数：**
- `keyword`: 关键词（姓名/手机号模糊查询）
- `addressId`: 地址ID筛选
- `customerType`: 客户类型 (0=潜在客户, 1=活跃客户)
- `page`: 页码（从0开始）
- `size`: 每页大小

**请求示例：**
```http
GET /api/customers?keyword=张&customerType=1&page=0&size=20&sort=createInstant,desc
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "张三",
        "phone": "13800138000",
        "gender": 1,
        "genderDesc": "男",
        "addressId": 1001,
        "fullAddress": "广东省-深圳市-南山区-XX街道-XX村",
        "addressDetail": "XX小区1栋2单元301",
        "customerType": 1,
        "customerTypeDesc": "活跃客户",
        "debtAmount": 500.00,       // 欠款总额
        "creditAmount": 200.00,     // 赊账总额
        "activeLedgerCount": 3,     // 活跃账单数
        "createInstant": "2025-01-25 10:00:00"
      }
    ],
    "totalElements": 100,
    "totalPages": 5,
    "size": 20,
    "number": 0
  }
}
```

**前端实现示例（Vue 3）：**
```vue
<script setup>
import { ref, onMounted } from 'vue';

const customers = ref([]);
const total = ref(0);
const currentPage = ref(0);
const pageSize = ref(20);
const searchForm = ref({
  keyword: '',
  customerType: null
});

const fetchCustomers = async () => {
  const response = await api.get('/customers', {
    params: {
      ...searchForm.value,
      page: currentPage.value,
      size: pageSize.value,
      sort: 'createInstant,desc'
    }
  });

  if (response.data.code === 200) {
    customers.value = response.data.data.content;
    total.value = response.data.data.totalElements;
  }
};

const handlePageChange = (page) => {
  currentPage.value = page - 1;  // Element Plus从1开始，API从0开始
  fetchCustomers();
};

onMounted(() => {
  fetchCustomers();
});
</script>

<template>
  <div>
    <!-- 搜索表单 -->
    <el-form :inline="true" :model="searchForm">
      <el-form-item label="关键词">
        <el-input v-model="searchForm.keyword" placeholder="姓名/手机号" />
      </el-form-item>
      <el-form-item label="客户类型">
        <el-select v-model="searchForm.customerType" clearable>
          <el-option label="潜在客户" :value="0" />
          <el-option label="活跃客户" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="fetchCustomers">搜索</el-button>
      </el-form-item>
    </el-form>

    <!-- 客户列表 -->
    <el-table :data="customers">
      <el-table-column prop="name" label="姓名" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column prop="genderDesc" label="性别" />
      <el-table-column prop="customerTypeDesc" label="类型" />
      <el-table-column label="欠款">
        <template #default="{ row }">
          <span style="color: red">{{ row.debtAmount || 0 }}元</span>
        </template>
      </el-table-column>
      <el-table-column label="赊账">
        <template #default="{ row }">
          <span style="color: orange">{{ row.creditAmount || 0 }}元</span>
        </template>
      </el-table-column>
      <el-table-column prop="activeLedgerCount" label="活跃账单" />
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button text @click="viewDetail(row.id)">详情</el-button>
          <el-button text @click="editCustomer(row.id)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      :current-page="currentPage + 1"
      :page-size="pageSize"
      :total="total"
      @current-change="handlePageChange"
    />
  </div>
</template>
```

### 3.3 查询客户详情

**接口：** `GET /api/customers/{id}`

**请求示例：**
```http
GET /api/customers/1
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "name": "张三",
    "phone": "13800138000",
    "gender": 1,
    "genderDesc": "男",
    "addressId": 1001,
    "fullAddress": "广东省-深圳市-南山区-XX街道-XX村",
    "addressDetail": "XX小区1栋2单元301",
    "customerType": 1,
    "customerTypeDesc": "活跃客户",
    "memo": "VIP客户",
    "ledgerSummary": {
      "inProgressCount": 2,        // 进行中账单数
      "inProgressAmount": 500.00,  // 进行中金额
      "partialCount": 1,           // 部分缴费账单数
      "partialAmount": 200.00,     // 部分缴费剩余金额
      "partialPaidAmount": 100.00, // 部分缴费已付金额
      "creditCount": 1,            // 赊账账单数
      "creditAmount": 300.00,      // 赊账金额
      "clearedCount": 10,          // 已结清账单数
      "totalDebt": 700.00,         // 总欠款（进行中+部分缴费）
      "totalCredit": 300.00        // 总赊账
    },
    "createInstant": "2025-01-25 10:00:00",
    "modifyInstant": "2025-01-25 11:00:00"
  }
}
```

### 3.4 更新客户信息

**接口：** `PUT /api/customers/{id}`

**请求示例：**
```json
{
  "name": "张三三",
  "phone": "13800138001",
  "addressDetail": "新地址"
}
```

**说明：** 只更新提供的字段，未提供的字段保持不变。

### 3.5 删除客户

**接口：** `DELETE /api/customers/{id}`

**请求示例：**
```http
DELETE /api/customers/1
```

**响应示例：**
```json
{
  "code": 200,
  "message": "客户删除成功"
}
```

**错误示例（有未结清账单）：**
```json
{
  "code": 400,
  "message": "该客户有3笔未结清账单，无法删除"
}
```

**前端实现示例：**
```javascript
const deleteCustomer = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该客户吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });

    const response = await api.delete(`/customers/${id}`);

    if (response.data.code === 200) {
      ElMessage.success('删除成功');
      fetchCustomers();  // 刷新列表
    } else {
      ElMessage.error(response.data.message);
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
};
```

---

## 4. 商品模块接口

### 4.1 商品管理流程

**完整流程：**
```
1. 创建商品SPU
   ↓
2. 设置商品属性（可选，如果有多规格）
   ↓
3. 生成SKU（如果没有属性，直接创建一个默认SKU）
```

### 4.2 创建商品（SPU）

**接口：** `POST /api/products`

**请求示例：**
```json
{
  "categoryId": 1,
  "name": "红富士苹果",
  "imageUrl": "https://example.com/apple.jpg",
  "description": "新鲜红富士苹果，口感脆甜",
  "unit": "斤",
  "location": "A区货架1",
  "memo": "热销商品"
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "categoryId": 1,
    "categoryName": "水果",  // TODO: 待实现
    "name": "红富士苹果",
    "imageUrl": "https://example.com/apple.jpg",
    "description": "新鲜红富士苹果，口感脆甜",
    "unit": "斤",
    "location": "A区货架1",
    "memo": "热销商品",
    "attributes": [],       // 属性列表（初始为空）
    "skus": [],             // SKU列表（初始为空）
    "createInstant": "2025-01-25 10:00:00",
    "modifyInstant": "2025-01-25 10:00:00"
  }
}
```

### 4.3 设置商品属性

**接口：** `POST /api/products/{id}/attributes`

**请求示例：**
```json
{
  "attributes": [
    {
      "attrName": "重量",
      "attrValues": ["5斤", "10斤", "20斤"]
    },
    {
      "attrName": "等级",
      "attrValues": ["一级", "二级"]
    }
  ]
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "属性设置成功"
}
```

**说明：**
- 会覆盖原有属性
- 如果商品没有多规格，可以跳过这一步

### 4.4 生成SKU

**接口：** `POST /api/products/{id}/skus`

#### 4.4.1 策略A：统一价格（自动生成所有组合）

**请求示例：**
```json
{
  "priceStrategy": "UNIFORM",
  "uniformPrice": 50.00
}
```

**说明：** 会根据属性自动生成所有笛卡尔积组合，所有SKU使用相同价格。

**结果：** 生成6个SKU（3种重量 × 2种等级）
- 红富士苹果-5斤-一级（50元）
- 红富士苹果-5斤-二级（50元）
- 红富士苹果-10斤-一级（50元）
- 红富士苹果-10斤-二级（50元）
- 红富士苹果-20斤-一级（50元）
- 红富士苹果-20斤-二级（50元）

#### 4.4.2 策略B：手动指定（只生成指定的SKU）

**请求示例：**
```json
{
  "priceStrategy": "MANUAL",
  "skus": [
    {
      "attrValueMap": {
        "重量": "5斤",
        "等级": "一级"
      },
      "price": 30.00,
      "imageUrl": "https://example.com/apple-5-1.jpg"
    },
    {
      "attrValueMap": {
        "重量": "10斤",
        "等级": "一级"
      },
      "price": 55.00,
      "imageUrl": "https://example.com/apple-10-1.jpg"
    }
  ]
}
```

**说明：** 只生成指定的SKU，每个SKU可以单独定价。

**响应示例：**
```json
{
  "code": 200,
  "message": "SKU生成成功"
}
```

**前端实现示例（Vue 3）：**
```vue
<script setup>
import { ref } from 'vue';

const productId = ref(1);
const priceStrategy = ref('UNIFORM');
const uniformPrice = ref(null);
const manualSkus = ref([]);

// 策略A：统一价格
const generateUniformSkus = async () => {
  const response = await api.post(`/products/${productId.value}/skus`, {
    priceStrategy: 'UNIFORM',
    uniformPrice: uniformPrice.value
  });

  if (response.data.code === 200) {
    ElMessage.success('SKU生成成功');
  }
};

// 策略B：手动指定
const generateManualSkus = async () => {
  const response = await api.post(`/products/${productId.value}/skus`, {
    priceStrategy: 'MANUAL',
    skus: manualSkus.value
  });

  if (response.data.code === 200) {
    ElMessage.success('SKU生成成功');
  }
};
</script>

<template>
  <el-radio-group v-model="priceStrategy">
    <el-radio label="UNIFORM">统一价格</el-radio>
    <el-radio label="MANUAL">手动指定</el-radio>
  </el-radio-group>

  <!-- 统一价格表单 -->
  <div v-if="priceStrategy === 'UNIFORM'">
    <el-input-number v-model="uniformPrice" :precision="2" :min="0" />
    <el-button @click="generateUniformSkus">生成SKU</el-button>
  </div>

  <!-- 手动指定表单 -->
  <div v-else>
    <!-- SKU列表编辑器 -->
    <el-button @click="generateManualSkus">生成SKU</el-button>
  </div>
</template>
```

### 4.5 查询商品列表

**接口：** `GET /api/products`

**查询参数：**
- `keyword`: 关键词（名称模糊查询）
- `categoryId`: 分类ID筛选
- `page`: 页码
- `size`: 每页大小

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "content": [
      {
        "id": 1,
        "categoryId": 1,
        "categoryName": "水果",
        "name": "红富士苹果",
        "imageUrl": "https://example.com/apple.jpg",
        "unit": "斤",
        "location": "A区货架1",
        "skuCount": 6,          // SKU数量
        "createInstant": "2025-01-25 10:00:00"
      }
    ],
    "totalElements": 50,
    "totalPages": 3
  }
}
```

### 4.6 查询商品详情

**接口：** `GET /api/products/{id}`

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "categoryId": 1,
    "categoryName": "水果",
    "name": "红富士苹果",
    "imageUrl": "https://example.com/apple.jpg",
    "description": "新鲜红富士苹果",
    "unit": "斤",
    "location": "A区货架1",
    "memo": "热销商品",
    "attributes": [         // 属性列表
      {
        "id": 1,
        "attrName": "重量",
        "attrValues": ["5斤", "10斤", "20斤"]
      },
      {
        "id": 2,
        "attrName": "等级",
        "attrValues": ["一级", "二级"]
      }
    ],
    "skus": [               // SKU列表
      {
        "id": 1,
        "skuName": "红富士苹果-5斤-一级",
        "attrValueMap": {
          "重量": "5斤",
          "等级": "一级"
        },
        "price": 30.00,
        "imageUrl": "https://example.com/apple-5-1.jpg"
      },
      {
        "id": 2,
        "skuName": "红富士苹果-10斤-一级",
        "attrValueMap": {
          "重量": "10斤",
          "等级": "一级"
        },
        "price": 55.00,
        "imageUrl": "https://example.com/apple-10-1.jpg"
      }
    ],
    "createInstant": "2025-01-25 10:00:00",
    "modifyInstant": "2025-01-25 10:00:00"
  }
}
```

### 4.7 查询SKU列表（开单用）

**接口：** `GET /api/products/skus`

**查询参数：**
- `keyword`: 关键词（SKU名称模糊查询）
- `page`: 页码
- `size`: 每页大小

**请求示例：**
```http
GET /api/products/skus?keyword=苹果&page=0&size=20
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "content": [
      {
        "skuId": 1,
        "productId": 1,
        "productName": "红富士苹果",
        "skuName": "红富士苹果-5斤-一级",
        "attrValueMap": {
          "重量": "5斤",
          "等级": "一级"
        },
        "price": 30.00,
        "imageUrl": "https://example.com/apple-5-1.jpg",
        "unit": "斤",
        "location": "A区货架1"
      }
    ],
    "totalElements": 20
  }
}
```

**前端实现示例（开单选择商品）：**
```vue
<script setup>
import { ref } from 'vue';

const keyword = ref('');
const skus = ref([]);

const searchSkus = async () => {
  const response = await api.get('/products/skus', {
    params: { keyword: keyword.value, size: 50 }
  });

  if (response.data.code === 200) {
    skus.value = response.data.data.content;
  }
};

const selectSku = (sku) => {
  // 添加到账单明细
  addLedgerItem({
    skuId: sku.skuId,
    productId: sku.productId,
    productName: sku.productName,
    skuName: sku.skuName,
    attrValueMap: sku.attrValueMap,
    price: sku.price,
    quantity: 1
  });
};
</script>

<template>
  <el-input
    v-model="keyword"
    placeholder="搜索商品"
    @input="searchSkus"
    clearable
  />

  <el-table :data="skus" @row-click="selectSku">
    <el-table-column prop="skuName" label="商品" />
    <el-table-column prop="price" label="价格" />
    <el-table-column prop="unit" label="单位" />
    <el-table-column prop="location" label="位置" />
  </el-table>
</template>
```

### 4.8 更新商品信息

**接口：** `PUT /api/products/{id}`

**请求示例：**
```json
{
  "name": "新名称",
  "location": "B区货架2"
}
```

### 4.9 删除商品

**接口：** `DELETE /api/products/{id}`

**说明：** 会同时软删除该商品的所有SKU和属性。

---

## 5. 账单模块接口

### 5.1 账单状态说明

**状态枚举：**
- `1` - IN_PROGRESS（进行中）：刚创建，未收款
- `2` - PARTIAL（部分缴费）：已收部分款项
- `3` - CLEARED（已结清）：全额收款或优惠结清
- `4` - ON_CREDIT（赊账中）：客户赊账
- `5` - CLOSED（已关闭）：取消的账单

**状态流转：**
```
IN_PROGRESS → PARTIAL → CLEARED
     ↓
ON_CREDIT → CLEARED
     ↓
  CLOSED
```

### 5.2 创建账单

**接口：** `POST /api/ledgers`

**请求示例：**
```json
{
  "customerId": 1,
  "items": [
    {
      "skuId": 1,
      "productId": 1,
      "productName": "红富士苹果",
      "skuName": "红富士苹果-5斤-一级",
      "attrValueMap": {
        "重量": "5斤",
        "等级": "一级"
      },
      "price": 30.00,
      "quantity": 2
    },
    {
      "skuId": 2,
      "productId": 1,
      "productName": "红富士苹果",
      "skuName": "红富士苹果-10斤-一级",
      "attrValueMap": {
        "重量": "10斤",
        "等级": "一级"
      },
      "price": 55.00,
      "quantity": 1
    }
  ],
  "memo": "客户要求多送一些"
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "customerId": 1,
    "customerName": "张三",
    "customerPhone": "13800138000",
    "customerAddress": "广东省-深圳市-南山区-XX街道-XX村",
    "customerAddressDetail": "XX小区1栋2单元301",
    "totalAmount": 115.00,      // 应收总金额（30*2 + 55*1）
    "paidAmount": 0.00,         // 实收金额
    "discountAmount": 0.00,     // 优惠金额
    "remainingAmount": 115.00,  // 剩余应收
    "ledgerStatus": 1,          // 进行中
    "ledgerStatusDesc": "进行中",
    "items": [                  // 账单明细
      {
        "id": 1,
        "skuId": 1,
        "skuName": "红富士苹果-5斤-一级",
        "attrValueMap": {
          "重量": "5斤",
          "等级": "一级"
        },
        "price": 30.00,
        "quantity": 2,
        "subtotal": 60.00
      },
      {
        "id": 2,
        "skuId": 2,
        "skuName": "红富士苹果-10斤-一级",
        "attrValueMap": {
          "重量": "10斤",
          "等级": "一级"
        },
        "price": 55.00,
        "quantity": 1,
        "subtotal": 55.00
      }
    ],
    "paymentRecords": [],       // 支付记录（初始为空）
    "memo": "客户要求多送一些",
    "createInstant": "2025-01-25 10:00:00",
    "modifyInstant": "2025-01-25 10:00:00",
    "version": 0
  }
}
```

**前端实现示例（Vue 3）：**
```vue
<script setup>
import { ref } from 'vue';

const ledgerForm = ref({
  customerId: null,
  items: [],
  memo: ''
});

// 添加明细项
const addItem = (sku) => {
  ledgerForm.value.items.push({
    skuId: sku.skuId,
    productId: sku.productId,
    productName: sku.productName,
    skuName: sku.skuName,
    attrValueMap: sku.attrValueMap,
    price: sku.price,
    quantity: 1
  });
};

// 计算总金额
const totalAmount = computed(() => {
  return ledgerForm.value.items.reduce((sum, item) => {
    return sum + (item.price * item.quantity);
  }, 0);
});

// 创建账单
const createLedger = async () => {
  if (!ledgerForm.value.customerId) {
    ElMessage.warning('请选择客户');
    return;
  }

  if (ledgerForm.value.items.length === 0) {
    ElMessage.warning('请添加商品');
    return;
  }

  const response = await api.post('/ledgers', ledgerForm.value);

  if (response.data.code === 200) {
    ElMessage.success('账单创建成功');
    router.push(`/ledgers/${response.data.data.id}`);
  } else {
    ElMessage.error(response.data.message);
  }
};
</script>

<template>
  <div>
    <!-- 选择客户 -->
    <el-select v-model="ledgerForm.customerId" placeholder="选择客户">
      <el-option
        v-for="customer in customers"
        :key="customer.id"
        :label="`${customer.name} (${customer.phone})`"
        :value="customer.id"
      />
    </el-select>

    <!-- 商品列表 -->
    <el-table :data="ledgerForm.items">
      <el-table-column prop="skuName" label="商品" />
      <el-table-column prop="price" label="单价" />
      <el-table-column label="数量">
        <template #default="{ row }">
          <el-input-number v-model="row.quantity" :min="1" />
        </template>
      </el-table-column>
      <el-table-column label="小计">
        <template #default="{ row }">
          {{ (row.price * row.quantity).toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column label="操作">
        <template #default="{ $index }">
          <el-button text @click="ledgerForm.items.splice($index, 1)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 合计 -->
    <div>应收总金额：{{ totalAmount.toFixed(2) }} 元</div>

    <!-- 备注 -->
    <el-input v-model="ledgerForm.memo" type="textarea" placeholder="备注" />

    <!-- 提交 -->
    <el-button type="primary" @click="createLedger">创建账单</el-button>
  </div>
</template>
```

### 5.3 修改账单明细

**接口：** `PUT /api/ledgers/{id}/items`

**限制：** 只有进行中(1)和部分缴费(2)状态可以修改。

**请求示例：**
```json
{
  "addItems": [              // 新增明细
    {
      "skuId": 3,
      "productId": 2,
      "productName": "香蕉",
      "skuName": "香蕉-5斤",
      "attrValueMap": {
        "重量": "5斤"
      },
      "price": 20.00,
      "quantity": 1
    }
  ],
  "updateItems": [           // 修改明细（只能改价格和数量）
    {
      "id": 1,               // 明细项ID
      "price": 28.00,        // 新价格
      "quantity": 3          // 新数量
    }
  ],
  "deleteItemIds": [2]       // 删除明细（明细项ID列表）
}
```

**说明：**
- `addItems`: 新增的明细项
- `updateItems`: 修改的明细项（只能修改价格和数量，不能修改商品）
- `deleteItemIds`: 要删除的明细项ID列表
- 三个字段都是可选的，可以只提供需要操作的部分

**响应示例：** 返回更新后的账单详情（格式同创建账单）

**前端实现示例：**
```javascript
const updateLedgerItems = async (ledgerId) => {
  const response = await api.put(`/ledgers/${ledgerId}/items`, {
    addItems: newItems.value,
    updateItems: modifiedItems.value,
    deleteItemIds: deletedItemIds.value
  });

  if (response.data.code === 200) {
    ElMessage.success('明细修改成功');
    // 刷新账单详情
    fetchLedgerDetail(ledgerId);
  } else {
    ElMessage.error(response.data.message);
  }
};
```

### 5.4 收款

**接口：** `POST /api/ledgers/{id}/receive-payment`

**请求示例：**
```json
{
  "amount": 50.00,           // 收款金额
  "paymentMethod": 2,        // 支付方式: 1=现金, 2=微信, 3=支付宝, 4=银行转账
  "memo": "微信收款"
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "totalAmount": 115.00,
    "paidAmount": 50.00,      // 实收金额更新
    "discountAmount": 0.00,
    "remainingAmount": 65.00, // 剩余应收
    "ledgerStatus": 2,        // 状态变为部分缴费
    "ledgerStatusDesc": "部分缴费",
    "paymentRecords": [       // 新增支付记录
      {
        "id": 1,
        "amount": 50.00,
        "paymentMethod": 2,
        "paymentMethodDesc": "微信",
        "memo": "微信收款",
        "createInstant": "2025-01-25 11:00:00"
      }
    ]
  }
}
```

**说明：**
- 不允许超额收款
- 收款后自动更新状态：
  - 剩余金额 > 0 → PARTIAL（部分缴费）
  - 剩余金额 = 0 → CLEARED（已结清）

**前端实现示例：**
```vue
<script setup>
import { ref } from 'vue';

const paymentForm = ref({
  amount: null,
  paymentMethod: 1,
  memo: ''
});

const receivePayment = async (ledgerId, remainingAmount) => {
  // 校验金额
  if (paymentForm.value.amount > remainingAmount) {
    ElMessage.warning(`收款金额不能超过剩余应收金额（${remainingAmount}元）`);
    return;
  }

  const response = await api.post(`/ledgers/${ledgerId}/receive-payment`, paymentForm.value);

  if (response.data.code === 200) {
    ElMessage.success('收款成功');
    // 刷新账单详情
    fetchLedgerDetail(ledgerId);
  } else {
    ElMessage.error(response.data.message);
  }
};
</script>

<template>
  <el-form :model="paymentForm">
    <el-form-item label="收款金额">
      <el-input-number
        v-model="paymentForm.amount"
        :precision="2"
        :min="0"
        :max="remainingAmount"
      />
      <span>（剩余应收：{{ remainingAmount }} 元）</span>
    </el-form-item>

    <el-form-item label="支付方式">
      <el-select v-model="paymentForm.paymentMethod">
        <el-option label="现金" :value="1" />
        <el-option label="微信" :value="2" />
        <el-option label="支付宝" :value="3" />
        <el-option label="银行转账" :value="4" />
      </el-select>
    </el-form-item>

    <el-form-item label="备注">
      <el-input v-model="paymentForm.memo" />
    </el-form-item>

    <el-button type="primary" @click="receivePayment">确认收款</el-button>
  </el-form>
</template>
```

### 5.5 优惠结清

**接口：** `POST /api/ledgers/{id}/discount-settle`

**请求示例：**
```json
{
  "discountAmount": 5.00,    // 优惠金额（抹零）
  "memo": "抹零5元"
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "totalAmount": 115.00,
    "paidAmount": 50.00,
    "discountAmount": 5.00,   // 优惠金额更新
    "remainingAmount": 0.00,  // 剩余为0
    "ledgerStatus": 3,        // 状态变为已结清
    "ledgerStatusDesc": "已结清"
  }
}
```

**说明：**
- 优惠结清后，账单状态变为CLEARED（已结清）
- 优惠金额不能超过剩余应收金额

### 5.6 赊账

**接口：** `POST /api/ledgers/{id}/on-credit`

**请求示例：**
```http
POST /api/ledgers/1/on-credit
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "ledgerStatus": 4,        // 状态变为赊账中
    "ledgerStatusDesc": "赊账中"
  }
}
```

**说明：**
- 只有进行中(1)的账单可以转为赊账
- 赊账后仍可以收款

### 5.7 关闭账单

**接口：** `POST /api/ledgers/{id}/close`

**说明：** 关闭账单（取消），已结清和已关闭的账单不能再关闭。

### 5.8 查询账单列表

**接口：** `GET /api/ledgers`

**查询参数：**
- `customerId`: 客户ID筛选
- `ledgerStatus`: 账单状态筛选 (1-5)
- `page`: 页码
- `size`: 每页大小

**请求示例：**
```http
GET /api/ledgers?customerId=1&ledgerStatus=1&page=0&size=20&sort=createInstant,desc
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "content": [
      {
        "id": 1,
        "customerId": 1,
        "customerName": "张三",
        "customerPhone": "13800138000",
        "totalAmount": 115.00,
        "paidAmount": 50.00,
        "remainingAmount": 65.00,
        "ledgerStatus": 2,
        "ledgerStatusDesc": "部分缴费",
        "createInstant": "2025-01-25 10:00:00"
      }
    ],
    "totalElements": 10
  }
}
```

**前端实现示例：**
```vue
<script setup>
import { ref } from 'vue';

const ledgers = ref([]);
const searchForm = ref({
  customerId: null,
  ledgerStatus: null
});

const fetchLedgers = async () => {
  const response = await api.get('/ledgers', {
    params: {
      ...searchForm.value,
      page: 0,
      size: 20,
      sort: 'createInstant,desc'
    }
  });

  if (response.data.code === 200) {
    ledgers.value = response.data.data.content;
  }
};

// 状态颜色
const getStatusColor = (status) => {
  const colorMap = {
    1: 'info',     // 进行中
    2: 'warning',  // 部分缴费
    3: 'success',  // 已结清
    4: 'danger',   // 赊账中
    5: 'info'      // 已关闭
  };
  return colorMap[status] || 'info';
};
</script>

<template>
  <div>
    <!-- 搜索表单 -->
    <el-form :inline="true" :model="searchForm">
      <el-form-item label="客户">
        <el-select v-model="searchForm.customerId" clearable>
          <!-- 客户选项 -->
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.ledgerStatus" clearable>
          <el-option label="进行中" :value="1" />
          <el-option label="部分缴费" :value="2" />
          <el-option label="已结清" :value="3" />
          <el-option label="赊账中" :value="4" />
          <el-option label="已关闭" :value="5" />
        </el-select>
      </el-form-item>
      <el-button type="primary" @click="fetchLedgers">搜索</el-button>
    </el-form>

    <!-- 账单列表 -->
    <el-table :data="ledgers">
      <el-table-column prop="id" label="账单号" />
      <el-table-column prop="customerName" label="客户" />
      <el-table-column prop="totalAmount" label="应收金额" />
      <el-table-column prop="paidAmount" label="实收金额" />
      <el-table-column label="剩余应收">
        <template #default="{ row }">
          <span :style="{ color: row.remainingAmount > 0 ? 'red' : 'green' }">
            {{ row.remainingAmount.toFixed(2) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="状态">
        <template #default="{ row }">
          <el-tag :type="getStatusColor(row.ledgerStatus)">
            {{ row.ledgerStatusDesc }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createInstant" label="创建时间" />
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button text @click="viewDetail(row.id)">详情</el-button>
          <el-button
            v-if="[1, 2, 4].includes(row.ledgerStatus)"
            text
            @click="showPaymentDialog(row)"
          >
            收款
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
```

### 5.9 查询账单详情

**接口：** `GET /api/ledgers/{id}`

**响应示例：** 参考"创建账单"的响应格式。

### 5.10 删除账单

**接口：** `DELETE /api/ledgers/{id}`

**说明：** 已结清的账单不允许删除。

---

## 6. 典型业务流程

### 6.1 完整开单流程

```javascript
// 1. 选择客户
const selectCustomer = async () => {
  // 打开客户选择器，或新建客户
  const customer = await showCustomerPicker();
  ledgerForm.value.customerId = customer.id;
};

// 2. 搜索并添加商品
const searchAndAddProduct = async (keyword) => {
  // 搜索SKU
  const response = await api.get('/products/skus', {
    params: { keyword, size: 50 }
  });

  const skus = response.data.data.content;

  // 显示商品列表供用户选择
  const selectedSku = await showSkuPicker(skus);

  // 添加到账单明细
  ledgerForm.value.items.push({
    skuId: selectedSku.skuId,
    productId: selectedSku.productId,
    productName: selectedSku.productName,
    skuName: selectedSku.skuName,
    attrValueMap: selectedSku.attrValueMap,
    price: selectedSku.price,
    quantity: 1
  });
};

// 3. 修改数量和价格
const updateItem = (item, quantity, price) => {
  item.quantity = quantity;
  item.price = price;
};

// 4. 创建账单
const createLedger = async () => {
  const response = await api.post('/ledgers', ledgerForm.value);

  if (response.data.code === 200) {
    const ledgerId = response.data.data.id;

    // 询问用户是否立即收款
    const shouldPay = await confirm('是否立即收款？');

    if (shouldPay) {
      // 跳转到收款页面
      router.push(`/ledgers/${ledgerId}/payment`);
    } else {
      // 询问是否赊账
      const shouldCredit = await confirm('是否赊账？');

      if (shouldCredit) {
        await api.post(`/ledgers/${ledgerId}/on-credit`);
      }

      // 跳转到账单详情
      router.push(`/ledgers/${ledgerId}`);
    }
  }
};
```

### 6.2 收款流程

```javascript
// 1. 查询账单详情
const fetchLedgerDetail = async (ledgerId) => {
  const response = await api.get(`/ledgers/${ledgerId}`);

  if (response.data.code === 200) {
    ledger.value = response.data.data;
  }
};

// 2. 收款
const receivePayment = async (amount, paymentMethod) => {
  // 校验金额
  if (amount > ledger.value.remainingAmount) {
    ElMessage.warning('收款金额不能超过剩余应收');
    return;
  }

  const response = await api.post(`/ledgers/${ledger.value.id}/receive-payment`, {
    amount,
    paymentMethod,
    memo: ''
  });

  if (response.data.code === 200) {
    ElMessage.success('收款成功');

    // 刷新账单详情
    fetchLedgerDetail(ledger.value.id);

    // 如果已结清，提示用户
    if (response.data.data.ledgerStatus === 3) {
      ElMessage.success('账单已结清');
    }
  }
};

// 3. 优惠结清（抹零）
const discountSettle = async () => {
  const remainingAmount = ledger.value.remainingAmount;

  // 询问优惠金额
  const discountAmount = await prompt(`剩余应收: ${remainingAmount} 元，优惠多少？`);

  if (discountAmount > remainingAmount) {
    ElMessage.warning('优惠金额不能超过剩余应收');
    return;
  }

  const response = await api.post(`/ledgers/${ledger.value.id}/discount-settle`, {
    discountAmount: parseFloat(discountAmount),
    memo: `优惠${discountAmount}元`
  });

  if (response.data.code === 200) {
    ElMessage.success('账单已结清');
    fetchLedgerDetail(ledger.value.id);
  }
};
```

### 6.3 客户欠款查询流程

```javascript
// 1. 查询客户列表（含欠款统计）
const fetchCustomersWithDebt = async () => {
  const response = await api.get('/customers', {
    params: {
      page: 0,
      size: 20,
      sort: 'createInstant,desc'
    }
  });

  if (response.data.code === 200) {
    customers.value = response.data.data.content;

    // 筛选出有欠款的客户
    const debtCustomers = customers.value.filter(c =>
      c.debtAmount > 0 || c.creditAmount > 0
    );

    // 按欠款金额排序
    debtCustomers.sort((a, b) =>
      (b.debtAmount + b.creditAmount) - (a.debtAmount + a.creditAmount)
    );

    return debtCustomers;
  }
};

// 2. 查询客户的所有未结清账单
const fetchCustomerUnpaidLedgers = async (customerId) => {
  const response = await api.get('/ledgers', {
    params: {
      customerId,
      // 不传ledgerStatus，查询所有状态，然后前端过滤
      size: 100
    }
  });

  if (response.data.code === 200) {
    const allLedgers = response.data.data.content;

    // 过滤出未结清的账单（进行中、部分缴费、赊账中）
    const unpaidLedgers = allLedgers.filter(l =>
      [1, 2, 4].includes(l.ledgerStatus)
    );

    return unpaidLedgers;
  }
};
```

---

## 7. 错误处理

### 7.1 常见错误码

| code | message | 说明 | 前端处理 |
|------|---------|------|---------|
| 200 | 操作成功 | 成功 | 正常处理 |
| 400 | 手机号已存在 | 业务错误 | 提示用户 |
| 400 | 该客户有N笔未结清账单，无法删除 | 业务规则限制 | 提示用户先结清账单 |
| 400 | 账单状态不允许修改明细 | 状态错误 | 提示用户账单已结清 |
| 400 | 收款金额不能超过剩余应收金额 | 金额校验失败 | 提示用户并修正金额 |
| 404 | 客户不存在 | 资源不存在 | 提示用户数据不存在 |
| 500 | 系统错误 | 服务器错误 | 提示用户联系管理员 |

### 7.2 统一错误处理

```javascript
// axios拦截器
axios.interceptors.response.use(
  response => {
    // 2xx范围的状态码都会触发该函数
    const data = response.data;

    if (data.code !== 200) {
      // 业务错误
      ElMessage.error(data.message);
      return Promise.reject(data);
    }

    return response;
  },
  error => {
    // 超出2xx范围的状态码都会触发该函数
    if (error.response) {
      switch (error.response.status) {
        case 401:
          // 未登录
          ElMessage.error('请先登录');
          router.push('/login');
          break;
        case 403:
          // 无权限
          ElMessage.error('无权限访问');
          break;
        case 404:
          // 资源不存在
          ElMessage.error('资源不存在');
          break;
        case 500:
          // 服务器错误
          ElMessage.error('服务器错误，请联系管理员');
          break;
        default:
          ElMessage.error('网络错误，请稍后重试');
      }
    } else {
      // 网络错误
      ElMessage.error('网络错误，请检查网络连接');
    }

    return Promise.reject(error);
  }
);
```

### 7.3 表单校验错误处理

```vue
<script setup>
import { ref } from 'vue';
import { ElMessage } from 'element-plus';

const form = ref({
  name: '',
  phone: ''
});

const rules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
};

const formRef = ref();

const submitForm = async () => {
  try {
    // 前端校验
    await formRef.value.validate();

    // 提交表单
    const response = await api.post('/customers', form.value);

    if (response.data.code === 200) {
      ElMessage.success('创建成功');
    }
  } catch (error) {
    // 校验失败
    if (error instanceof Error) {
      ElMessage.warning('请检查表单输入');
    }
  }
};
</script>

<template>
  <el-form ref="formRef" :model="form" :rules="rules">
    <el-form-item label="姓名" prop="name">
      <el-input v-model="form.name" />
    </el-form-item>
    <el-form-item label="手机号" prop="phone">
      <el-input v-model="form.phone" />
    </el-form-item>
    <el-button type="primary" @click="submitForm">提交</el-button>
  </el-form>
</template>
```

---

## 附录

### A. 完整的枚举值

**客户类型 (customerType):**
- `0` - 潜在客户
- `1` - 活跃客户

**性别 (gender):**
- `0` - 未知
- `1` - 男
- `2` - 女

**账单状态 (ledgerStatus):**
- `1` - 进行中
- `2` - 部分缴费
- `3` - 已结清
- `4` - 赊账中
- `5` - 已关闭

**支付方式 (paymentMethod):**
- `1` - 现金
- `2` - 微信
- `3` - 支付宝
- `4` - 银行转账

### B. API测试工具

推荐使用以下工具测试API：
1. **Knife4j文档**：`http://localhost:8080/doc.html`
2. **Postman**：导入接口集合
3. **curl命令**：
```bash
# 创建客户
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d '{"name":"张三","phone":"13800138000","gender":1}'

# 查询客户列表
curl -X GET "http://localhost:8080/api/customers?page=0&size=20"

# 创建账单
curl -X POST http://localhost:8080/api/ledgers \
  -H "Content-Type: application/json" \
  -d '{"customerId":1,"items":[{"skuId":1,"productId":1,"productName":"苹果","skuName":"苹果-5斤","price":30,"quantity":2}]}'
```

---

**文档版本：** v1.0
**最后更新：** 2025-01-25
**联系方式：** Core Ledger Team
