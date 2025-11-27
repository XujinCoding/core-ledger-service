# core-ledger 实体相关设计

## **1. sys_user (系统用户表)**

| **属性名称** | **属性定义** | **数据域** | **缺省/可空** | **主/外键** | **索引** | **描述** |
| --- | --- | --- | --- | --- | --- | --- |
| id | 主键ID | BIGINT | NOT NULL | 主键 | - | 自增主键 |
| username | 用户名 | VARCHAR(50) | DEFAULT NULL | - | uk_username | 可选，管理员必填 |
| password | 密码 | VARCHAR(100) | DEFAULT NULL | - | - | BCrypt加密，管理员必填 |
| phone | 手机号 | VARCHAR(20) | NOT NULL | - | uk_phone | 唯一标识 |
| role | 角色 | TINYINT | DEFAULT 0 | - | - | 0=普通用户,1=管理员 |
| wx_openid | 微信OpenID | VARCHAR(100) | DEFAULT NULL | - | uk_wx_openid | 微信唯一标识 |
| wx_nickname | 微信昵称 | VARCHAR(100) | DEFAULT NULL | - | - | 微信用户昵称 |
| wx_avatar_url | 微信头像URL | VARCHAR(500) | DEFAULT NULL | - | - | 头像链接地址 |
| memo | 备注 | VARCHAR(255) | DEFAULT NULL | - | - | 用户备注信息 |
| status | 状态 | INT | DEFAULT 1 | - | - | 1=启用,0=禁用 |
| create_instant | 创建时间 | DATETIME | DEFAULT CURRENT_TIMESTAMP | - | - | 记录创建时间 |
| modify_instant | 修改时间 | DATETIME | DEFAULT CURRENT_TIMESTAMP | - | - | 自动更新时间 |
| version | 乐观锁版本号 | INT | DEFAULT 0 | - | - | 并发控制 |

## **2. sys_address (行政区划地址库)**

| **属性名称** | **属性定义** | **数据域** | **缺省/可空** | **主/外键** | **索引** | **描述** |
| --- | --- | --- | --- | --- | --- | --- |
| id | 主键ID | BIGINT | NOT NULL | 主键 | - | 自增主键 |
| parent_id | 父级ID | BIGINT | DEFAULT 0 | - | idx_parent_id | 0表示顶级 |
| name | 地址名称 | VARCHAR(100) | NOT NULL | - | - | 地址名称 |
| level | 层级 | TINYINT | NOT NULL | - | idx_level | 1=省,2=市,3=区县,4=镇/乡,5=村 |
| merger_name | 全称路径 | VARCHAR(500) | DEFAULT NULL | - | - | 完整地址路径 |
| memo | 备注 | VARCHAR(255) | DEFAULT NULL | - | - | 地址备注信息 |
| status | 状态 | INT | DEFAULT 1 | - | - | 1=启用,0=禁用 |
| create_instant | 创建时间 | DATETIME | DEFAULT CURRENT_TIMESTAMP | - | - | 记录创建时间 |
| modify_instant | 修改时间 | DATETIME | DEFAULT CURRENT_TIMESTAMP | - | - | 自动更新时间 |
| version | 乐观锁版本号 | INT | DEFAULT 0 | - | - | 并发控制 |

## **3. customer (客户信息表)**

| **属性名称** | **属性定义** | **数据域** | **缺省/可空** | **主/外键** | **索引** | **描述** |
| --- | --- | --- | --- | --- | --- | --- |
| id | 主键ID | BIGINT | NOT NULL | 主键 | - | 自增主键 |
| name | 客户姓名 | VARCHAR(50) | NOT NULL | - | idx_name | 客户真实姓名 |
| phone | 手机号 | VARCHAR(20) | NOT NULL | - | uk_phone | 唯一手机号 |
| alias | 别名/昵称 | VARCHAR(50) | DEFAULT NULL | - | - | 客户昵称 |
| gender | 性别 | TINYINT | DEFAULT 0 | - | - | 0=未知,1=男,2=女 |
| age | 年龄 | INT | DEFAULT NULL | - | - | 客户年龄 |
| address_id | 关联地址ID | BIGINT | NOT NULL |  | idx_address_id | 关联sys_address.id |
| address_detail | 详细地址 | VARCHAR(255) | DEFAULT NULL | - | - | 门牌号等详细信息 |
| memo | 备注 | VARCHAR(255) | DEFAULT NULL | - | - | 客户备注信息 |
| customer_type | 客户类型 | TINYINT | DEFAULT 1 | - | idx_customer_type | 0=潜在客户,1=活跃客户 |
| status | 状态 | INT | DEFAULT 1 | - | - | 1=启用,0=禁用 |
| create_instant | 创建时间 | DATETIME | DEFAULT CURRENT_TIMESTAMP | - | - | 记录创建时间 |
| modify_instant | 修改时间 | DATETIME | DEFAULT CURRENT_TIMESTAMP | - | - | 自动更新时间 |
| version | 乐观锁版本号 | INT | DEFAULT 0 | - | - | 并发控制 |

## **5. ledger (账本主表)**

| **属性名称** | **属性定义** | **数据域** | **缺省/可空** | **主/外键** | **索引** | **描述** |
| --- | --- | --- | --- | --- | --- | --- |
| id | 主键ID | BIGINT | NOT NULL | 主键 | - | 自增主键 |
| customer_id | 客户ID | BIGINT | NOT NULL |  | idx_customer_id | 关联customer.id |
| total_amount | 应收总金额 | DECIMAL(10,2) | DEFAULT 0.00 | - | - | 应收取的总金额 |
| paid_amount | 实收金额 | DECIMAL(10,2) | DEFAULT 0.00 | - | - | 实际已收金额 |
| discount_amount | 抹零/优惠金额 | DECIMAL(10,2) | DEFAULT 0.00 | - | - | 优惠或抹零金额 |
| ledger_status | 账本状态 | TINYINT | DEFAULT 1 | - | idx_ledger_status | 1=进行中,2=部分缴费,3=已结清,4=赊账中,5=已关闭 |
| memo | 备注 | VARCHAR(255) | DEFAULT NULL | - | - | 账本备注信息 |
| status | 状态 | INT | DEFAULT 1 | - | - | 1=启用,0=禁用 |
| create_instant | 创建时间 | DATETIME | DEFAULT CURRENT_TIMESTAMP | - | idx_create_instant | 记录创建时间 |
| modify_instant | 修改时间 | DATETIME | DEFAULT CURRENT_TIMESTAMP | - | - | 自动更新时间 |
| version | 乐观锁版本号 | INT | DEFAULT 0 | - | - | 并发控制 |

## **6. ledger_item (账本明细表)**

| **属性名称** | **属性定义** | **数据域** | **缺省/可空** | **主/外键** | **索引** | **描述** |
| --- | --- | --- | --- | --- | --- | --- |
| id | 主键ID | BIGINT | NOT NULL | 主键 | - | 自增主键 |
| ledger_id | 账本ID | BIGINT | NOT NULL |  | idx_ledger_id | 关联ledger.id |
| product_id | 商品ID | BIGINT | NOT NULL |  | idx_product_id | 关联product.id |
| product_name | 商品名称 | VARCHAR(100) | NOT NULL |  | - | 冗余存储商品名称 |
| sku_id | SKU ID | BIGINT | DEFAULT NULL |  | idx_sku_id | 关联product_sku.id |
| sku_name | SKU名称 | VARCHAR(150) | DEFAULT NULL | - | - | 冗余存储SKU名称 |
| price | 实际售价 | DECIMAL(10,2) | NOT NULL | - | - | 实际销售价格(单价) |
| quantity | 数量 | INT | DEFAULT 1 | - | - | 购买数量 |
| memo | 备注 | VARCHAR(255) | DEFAULT NULL | - | - | 明细备注信息 |
| status | 状态 | INT | DEFAULT 1 | - | - | 1=有效,0=已删除(软删除) |
| create_instant | 创建时间 | DATETIME | DEFAULT CURRENT_TIMESTAMP | - | - | 记录创建时间 |
| modify_instant | 修改时间 | DATETIME | DEFAULT CURRENT_TIMESTAMP | - | - | 自动更新时间 |
| version | 乐观锁版本号 | INT | DEFAULT 0 | - | - | 并发控制 |

## **7. payment_record (支付流水表)**

| **属性名称** | **属性定义** | **数据域** | **缺省/可空** | **主/外键** | **索引** | **描述** |
| --- | --- | --- | --- | --- | --- | --- |
| id | 主键ID | BIGINT | NOT NULL | 主键 | - | 自增主键 |
| ledger_id | 账本ID | BIGINT | NOT NULL | 外键 | idx_ledger_id | 关联ledger.id |
| amount | 支付金额 | DECIMAL(10,2) | NOT NULL | - | - | 单笔支付金额 |
| payment_method | 支付方式 | TINYINT | DEFAULT 1 | - | idx_payment_method | 1=现金,2=微信,3=支付宝,4=银行转账 |
| memo | 备注 | VARCHAR(255) | DEFAULT NULL | - | - | 支付备注信息 |
| status | 状态 | INT | DEFAULT 1 | - | - | 1=有效,0=已删除(软删除) |
| create_instant | 创建时间 | DATETIME | DEFAULT CURRENT_TIMESTAMP | - | idx_create_instant | 支付时间 |
| modify_instant | 修改时间 | DATETIME | DEFAULT CURRENT_TIMESTAMP | - | - | 自动更新时间 |
| version | 乐观锁版本号 | INT | DEFAULT 0 | - | - | 并发控制 |

## **4. product_category (商品分类表)**

| **属性名称** | **属性定义** | **数据域** | **缺省/可空** | **主/外键** | **索引** | **描述** |
| --- | --- | --- | --- | --- | --- | --- |
| id | 主键ID | BIGINT | NOT NULL | 主键 | - | 自增主键 |
| parent_id | 父分类ID | BIGINT | DEFAULT 0 | - | idx_parent_id | 0表示顶级分类 |
| name | 分类名称 | VARCHAR(50) | NOT NULL | - | - | 分类名称 |
| level | 层级 | TINYINT | DEFAULT 1 | - | - | 分类层级:1-5 |
| sort_order | 排序序号 | INT | DEFAULT 0 | - | - | 分类排序 |
| icon_url | 分类图标URL | VARCHAR(500) | DEFAULT NULL | - | - | 分类图标链接 |
| memo | 备注 | VARCHAR(255) | DEFAULT NULL | - | - | 分类备注信息 |
| status | 状态 | INT | DEFAULT 1 | - | - | 1=启用,0=禁用 |
| create_instant | 创建时间 | DATETIME | DEFAULT CURRENT_TIMESTAMP | - | - | 记录创建时间 |
| modify_instant | 修改时间 | DATETIME | DEFAULT CURRENT_TIMESTAMP | - | - | 自动更新时间 |
| version | 乐观锁版本号 | INT | DEFAULT 0 | - | - | 并发控制 |

## **8. product (商品信息表)**

| **属性名称** | **属性定义** | **数据域** | **缺省/可空** | **主/外键** | **索引** | **描述** |
| --- | --- | --- | --- | --- | --- | --- |
| id | 主键ID | BIGINT | NOT NULL | 主键 | - | 自增主键 |
| category_id | 分类ID | BIGINT | NOT NULL |  | idx_category_id | 关联product_category.id |
| name | 商品名称 | VARCHAR(100) | NOT NULL | - | idx_name | 商品名称 |
| image_url | 商品主图URL | VARCHAR(500) | DEFAULT NULL | - | - | 商品主图链接 |
| description | 商品描述 | VARCHAR(500) | DEFAULT NULL | - | - | 商品详细描述 |
| price | 标准价格 | DECIMAL(10,2) | DEFAULT 0.00 | - | - | 商品标准售价 |
| spec | 规格型号 | VARCHAR(100) | DEFAULT NULL | - | - | 商品规格信息 |
| unit | 单位 | VARCHAR(20) | DEFAULT '件' | - | - | 件/箱/斤/公斤等 |
| location | 存放位置 | VARCHAR(100) | DEFAULT NULL | - | - | A区3排5列等 |
| memo | 备注 | VARCHAR(255) | DEFAULT NULL | - | - | 商品备注信息 |
| status | 状态 | INT | DEFAULT 1 | - | - | 1=启用,0=禁用 |
| create_instant | 创建时间 | DATETIME | DEFAULT CURRENT_TIMESTAMP | - | - | 记录创建时间 |
| modify_instant | 修改时间 | DATETIME | DEFAULT CURRENT_TIMESTAMP | - | - | 自动更新时间 |
| version | 乐观锁版本号 | INT | DEFAULT 0 | - | - | 并发控制 |

## **10. product_attr (商品属性表)**

| **属性名称** | **属性定义** | **数据域** | **缺省/可空** | **主/外键** | **索引** | **描述** |
| --- | --- | --- | --- | --- | --- | --- |
| id | 主键ID | BIGINT | NOT NULL | 主键 | - | 自增主键 |
| product_id | 商品ID | BIGINT | NOT NULL |  | idx_product_id | 关联product.id |
| attr_name | 属性名称 | VARCHAR(50) | NOT NULL | - | idx_attr_name | 如:重量 |
| sort_order | 排序序号 | INT | DEFAULT 0 | - | - | 属性排序 |
| memo | 备注 | VARCHAR(255) | DEFAULT NULL | - | - | 属性备注信息 |
| status | 状态 | INT | DEFAULT 1 | - | - | 1=启用,0=禁用 |
| create_instant | 创建时间 | DATETIME | DEFAULT CURRENT_TIMESTAMP | - | - | 记录创建时间 |
| modify_instant | 修改时间 | DATETIME | DEFAULT CURRENT_TIMESTAMP | - | - | 自动更新时间 |
| version | 乐观锁版本号 | INT | DEFAULT 0 | - | - | 并发控制 |

## **9. product_attr_value (商品属性值)**

**product_attr < 1 — N> product_attr_value** 

| **属性名称** | **属性定义** | **数据域** | **缺省/可空** | **主/外键** | **索引** | **描述** |
| --- | --- | --- | --- | --- | --- | --- |
| id | 主键ID | BIGINT | NOT NULL | 主键 | - | 自增主键 |
| **product_attr_id** | 关联分类ID | BIGINT | NOT NULL |  | idx_category_id | 关联product_category.id |
| sort_order | 排序序号 | INT | DEFAULT 0 | - | - | 属性排序 |
| value | 属性值 | VARCHAR(255) | DEFAULT 0 | - | - | 1=必填,0=可选 |
| memo | 备注 | VARCHAR(255) | DEFAULT NULL | - | - | 模板备注信息 |
| status | 状态 | INT | DEFAULT 1 | - | - | 1=启用,0=禁用 |
| create_instant | 创建时间 | DATETIME | DEFAULT CURRENT_TIMESTAMP | - | - | 记录创建时间 |
| modify_instant | 修改时间 | DATETIME | DEFAULT CURRENT_TIMESTAMP | - | - | 自动更新时间 |
| version | 乐观锁版本号 | INT | DEFAULT 0 | - | - | 并发控制 |

## **11. product_sku (商品SKU表)**

需要对商品sku提供定价功能

| **属性名称** | **属性定义** | **数据域** | **缺省/可空** | **主/外键** | **索引** | **描述** |
| --- | --- | --- | --- | --- | --- | --- |
| id | 主键ID | BIGINT | NOT NULL | 主键 | - | 自增主键 |
| product_id | 商品ID | BIGINT | NOT NULL |  | idx_product_id | 关联product.id |
| sku_name | SKU名称 | VARCHAR(150) | NOT NULL | - | idx_sku_name | 自动生成,如:红富士苹果-5斤-一级 |
| price_status | 定价状态 | INT | NOT NULL | - |  | 定价状态 0 : 未定价, 1 已定价
定价之后的SKU才能被使用, sku一旦被修改定价状态变为未定价 |
| price | 销售价格 | DECIMAL(10,2) | DEFAULT 0.00 | - | idx_price | SKU销售价格 |
| image_url | SKU图片URL | VARCHAR(500) | DEFAULT NULL | - | - | SKU专属图片 |
| sort_order | 排序序号 | INT | DEFAULT 0 | - | - | SKU排序 |
| memo | 备注 | VARCHAR(255) | DEFAULT NULL | - | - | SKU备注信息 |
| status | 状态 | INT | DEFAULT 1 | - | - | 1=启用,0=禁用 |
| create_instant | 创建时间 | DATETIME | DEFAULT CURRENT_TIMESTAMP | - | - | 记录创建时间 |
| modify_instant | 修改时间 | DATETIME | DEFAULT CURRENT_TIMESTAMP | - | - | 自动更新时间 |
| version | 乐观锁版本号 | INT | DEFAULT 0 | - | - | 并发控制 |

## **product_sku_attr (商品SKU属性值表)**

| **属性名称** | **属性定义** | **数据域** | **缺省/可空** | **主/外键** | **索引** | **描述** |
| --- | --- | --- | --- | --- | --- | --- |
| id | 主键ID | BIGINT | NOT NULL | 主键 | - | 自增主键 |
| sku_id | sku标识 | BIGINT | NOT NULL | - | - | 关联sku_id |
| product_attr_id | 产品属性标识 | VARCHAR(150) | NOT NULL | - | - |  |
| product_attr_name | 产品属性名称 | DECIMAL(10,2) | DEFAULT 0.00 | - |  |  |
| product_attr_value_id | 产品属性值标识 | DECIMAL(10,2) | DEFAULT NULL | - | - |  |
| product_attr_value_name | 产品属性值名称 | VARCHAR(500) | DEFAULT NULL | - | - |  |
| sort_order | 排序序号 | INT | DEFAULT 0 | - | - |  |
| memo | 备注 | VARCHAR(255) | DEFAULT NULL | - | - | SKU备注信息 |
| status | 状态 | INT | DEFAULT 1 | - | - | 1=启用,0=禁用 |
| create_instant | 创建时间 | DATETIME | DEFAULT CURRENT_TIMESTAMP | - | - | 记录创建时间 |
| modify_instant | 修改时间 | DATETIME | DEFAULT CURRENT_TIMESTAMP | - | - | 自动更新时间 |
| version | 乐观锁版本号 | INT | DEFAULT 0 | - | - | 并发控制 |