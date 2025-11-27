# 商品模块实现说明文档

## 已完成的工作

### 1. ✅ BusinessCode错误码
- 文件：`com.coreledger.enums.BusinessCode`
- 添加了商品模块完整错误码（3000-3999）

### 2. ✅ Repository层
已创建并补充自定义查询方法：
- `ProductCategoryRepository` - 商品分类
- `ProductRepository` - 商品
- `ProductAttrRepository` - 商品属性
- `ProductAttrValueRepository` - 商品属性值
- `ProductSkuRepository` - 商品SKU
- `ProductSkuAttrRepository` - 商品SKU属性

### 3. ✅ DTO类
- `CategoryCreateDTO` / `CategoryUpdateDTO` - 分类创建/修改
- `ProductCreateDTO` / `ProductUpdateDTO` - 商品创建/修改
- `ProductAttrCreateDTO` - 属性创建
- `ProductAttrValueCreateDTO` - 属性值创建
- `SkuPriceUpdateDTO` - SKU批量定价

### 4. ✅ VO类
- `CategoryVO` / `CategoryTreeVO` - 分类信息/分类树
- `ProductVO` - 商品详情
- `ProductAttrVO` / `ProductAttrValueVO` - 属性/属性值
- `ProductSkuVO` / `ProductSkuAttrVO` - SKU/SKU属性

### 5. ✅ MapStruct Converter
- `ProductCategoryConverter` - 分类转换
- `ProductConverter` - 商品转换
- `ProductAttrConverter` - 属性转换
- `ProductAttrValueConverter` - 属性值转换
- `ProductSkuConverter` - SKU转换
- `ProductSkuAttrConverter` - SKU属性转换

## 需要实现的Service和Controller

### Service层（需要创建）

#### 1. ProductCategoryService
核心方法：
- `createCategory()` - 创建分类（自动计算level）
- `updateCategory()` - 修改分类
- `deleteCategory()` - 删除分类（检查子分类和商品）
- `getCategory()` - 获取分类详情
- `listCategories()` - 分类列表（分页）
- `getCategoryTree()` - 获取分类树
- `getChildren()` - 获取子分类
- `moveCategory()` - 移动分类（重新计算level）
- `updateStatus()` - 启用/禁用分类

#### 2. ProductService
核心方法：
- `createProduct()` - 创建商品
- `updateProduct()` - 修改商品
- `deleteProduct()` - 删除商品（级联删除属性和SKU）
- `getProduct()` - 获取商品详情（含属性和SKU）
- `listProducts()` - 商品列表（支持分类筛选、关键词搜索）
- `updateStatus()` - 启用/禁用商品

#### 3. ProductAttrService
核心方法：
- `addAttr()` - 添加属性
- `updateAttr()` - 修改属性
- `deleteAttr()` - 删除属性（触发SKU重新生成）
- `getProductAttrs()` - 获取商品所有属性及其值
- `addAttrValue()` - 添加属性值（触发SKU生成）
- `updateAttrValue()` - 修改属性值
- `deleteAttrValue()` - 删除属性值（触发SKU重新生成）
- `regenerateSkus()` - 手动重新生成SKU

#### 4. ProductSkuService
核心方法：
- `generateSkus()` - 根据属性生成SKU（笛卡尔积算法）
- `getProductSkus()` - 获取商品SKU列表
- `getPricedSkus()` - 获取已定价SKU（业务专用）
- `getUnpricedSkus()` - 获取未定价SKU（定价管理专用）
- `getSkuDetail()` - 获取SKU详情
- `updateSkuPrice()` - 修改单个SKU价格
- `batchUpdatePrice()` - 批量定价
- `updateSkuStatus()` - 启用/禁用SKU
- `deleteSku()` - 删除SKU

### Controller层（需要创建）

#### 1. ProductCategoryController
路径：`/api/categories`

#### 2. ProductController  
路径：`/api/products`

#### 3. ProductSkuController
路径：`/api/skus` 和 `/api/products/{productId}/skus`

## SKU自动生成逻辑（核心）

### 生成算法
使用**笛卡尔积**算法，将所有属性的属性值进行组合。

```java
示例：
属性1：重量 [5斤, 10斤]
属性2：等级 [一级, 二级]

生成SKU：
- 5斤 × 一级 → SKU1
- 5斤 × 二级 → SKU2
- 10斤 × 一级 → SKU3
- 10斤 × 二级 → SKU4
```

### SKU命名规则
```
格式：{商品名称}-{属性值1}-{属性值2}-{属性值3}-...
示例：红富士苹果-5斤-一级
```

### 触发时机
- 添加属性值 → 增量生成新SKU
- 删除属性值 → 删除包含该值的SKU
- 删除属性 → 删除所有SKU，基于剩余属性重新生成
- 手动重新生成 → 清空并重新生成全部SKU

## 实现建议

### 1. Service层实现要点
- 使用`@Transactional(readOnly = true)`在类级别
- 写操作方法使用`@Transactional`覆盖
- 所有异常必须使用`BusinessCode`枚举
- 单表查询使用`PredicateBuilder`
- 多表查询使用MyBatis（如果需要）

### 2. SKU生成Service实现
```java
@Service
public class ProductSkuService {
    
    @Transactional
    public void generateSkus(Long productId) {
        // 1. 查询商品所有有效属性
        // 2. 查询每个属性的有效属性值
        // 3. 笛卡尔积计算生成SKU组合
        // 4. 保存SKU和SKU属性关联
    }
}
```

### 3. Controller层实现要点
- 返回`Result<T>`统一响应
- 不捕获异常（由全局异常处理器处理）
- 使用`@Valid`校验参数
- 接口参数不超过3个
- 添加Swagger注解

## 下一步工作

请按以下顺序完成剩余代码：

1. **ProductCategoryService** - 商品分类服务
2. **ProductService** - 商品服务
3. **ProductAttrService** - 属性服务（含SKU生成逻辑）
4. **ProductSkuService** - SKU服务
5. **ProductCategoryController** - 分类控制器
6. **ProductController** - 商品控制器
7. **ProductSkuController** - SKU控制器

每个Service约200-300行代码，每个Controller约150-200行代码。

## 注意事项

1. **严格遵循最佳实践文档**
2. **所有状态字段使用枚举**
3. **异常处理使用BusinessCode**
4. **枚举比较使用`常量.equals(变量)`**
5. **编译错误是临时IDE缓存问题，完整编译后会消失**
