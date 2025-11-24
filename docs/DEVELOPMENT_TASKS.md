# Core Ledger 开发任务清单

## 文档说明
本文档列出了系统开发的所有任务，按模块和优先级组织。

**创建日期**: 2025-11-24  
**维护团队**: Core Ledger Team  
**文档版本**: 1.0.0

---

## 任务优先级说明

- **P0**: 核心功能，必须优先完成
- **P1**: 重要功能，第一阶段完成
- **P2**: 常规功能，第二阶段完成
- **P3**: 优化功能，后续迭代

---

## 1. 基础设施 (P0)

### 1.1 项目初始化
- [x] ✅ 创建 Spring Boot 项目
- [x] ✅ 配置 pom.xml / build.gradle
- [x] ✅ 配置 application.yml
- [x] ✅ 配置 Flyway 数据库迁移
- [x] ✅ 创建 BaseEntity 基类（含 version 字段）
- [x] ✅ 创建数据库初始化脚本（含 version 字段）
- [x] ✅ 创建 ErrorCode 统一错误码枚举

### 1.2 配置类
- [ ] 创建 JPA 配置（启用审计）
- [ ] 创建 MyBatis 配置
- [ ] 创建 Jackson 配置（日期格式化）
- [ ] 创建 Knife4j 配置（API 文档）
- [ ] 创建全局异常处理器
- [ ] 创建统一响应格式 Result 类

### 1.3 枚举类
- [ ] 创建 LedgerStatus 枚举（账本状态）
- [ ] 创建 PaymentMethod 枚举（支付方式）
- [ ] 创建 Gender 枚举（性别）
- [ ] 创建 UserRole 枚举（用户角色）
- [ ] 创建对应的 JPA Converter
- [ ] 创建对应的 MyBatis TypeHandler

---

## 2. 实体层 (P0)

### 2.1 核心实体
- [ ] 创建 SysUser 实体（用户表）
- [ ] 创建 Customer 实体（客户表）
- [ ] 创建 Ledger 实体（账本主表）
- [ ] 创建 LedgerItem 实体（账本明细表）
- [ ] 创建 PaymentRecord 实体（支付流水表）

### 2.2 辅助实体
- [ ] 创建 Product 实体（商品表）
- [ ] 创建 ProductCategory 实体（商品分类表）
- [ ] 创建 SysAddress 实体（地址库表）

---

## 3. Repository 层 (P0)

### 3.1 JPA Repository
- [ ] 创建 LedgerRepository
- [ ] 创建 LedgerItemRepository
- [ ] 创建 PaymentRecordRepository
- [ ] 创建 CustomerRepository
- [ ] 创建 ProductRepository
- [ ] 创建 ProductCategoryRepository
- [ ] 创建 SysAddressRepository
- [ ] 创建 SysUserRepository

### 3.2 MyBatis Mapper
- [ ] 创建 LedgerMapper（复杂查询）
- [ ] 创建 CustomerMapper（统计查询）
- [ ] 创建 PaymentRecordMapper（流水查询）
- [ ] 创建对应的 XML 文件

---

## 4. DTO/VO 层 (P0)

### 4.1 账本相关
- [ ] 创建 LedgerDTO
- [ ] 创建 CreateLedgerDTO
- [ ] 创建 UpdateLedgerDTO
- [ ] 创建 LedgerListVO
- [ ] 创建 LedgerDetailVO
- [ ] 创建 LedgerItemDTO

### 4.2 客户相关
- [ ] 创建 CustomerDTO
- [ ] 创建 CreateCustomerDTO
- [ ] 创建 UpdateCustomerDTO
- [ ] 创建 CustomerListVO
- [ ] 创建 CustomerDetailVO

### 4.3 商品相关
- [ ] 创建 ProductDTO
- [ ] 创建 CreateProductDTO
- [ ] 创建 ProductListVO
- [ ] 创建 ProductCategoryDTO

### 4.4 支付相关
- [ ] 创建 PaymentRecordDTO
- [ ] 创建 CreatePaymentDTO
- [ ] 创建 PaymentRecordListVO

---

## 5. Converter 层 (P0)

### 5.1 MapStruct 转换器
- [ ] 创建 LedgerConverter
- [ ] 创建 CustomerConverter
- [ ] 创建 ProductConverter
- [ ] 创建 PaymentRecordConverter

---

## 6. Service 层 (P0)

### 6.1 账本服务
- [ ] 创建 LedgerService
  - [ ] createLedger() - 创建账本
  - [ ] getLedgerById() - 查询账本详情
  - [ ] listActiveLedgers() - 查询活跃账单（主页面）
  - [ ] listCustomerLedgers() - 查询客户所有账单
  - [ ] listCreditLedgers() - 查询赊账账单
  - [ ] receivePayment() - 收款操作
  - [ ] moveToCredit() - 转为赊账
  - [ ] discountSettle() - 抹零结清（管理员）
  - [ ] closeLedger() - 关闭账单（管理员）
  - [ ] updateLedgerItems() - 修改账本明细

### 6.2 客户服务
- [ ] 创建 CustomerService
  - [ ] createCustomer() - 创建客户
  - [ ] getCustomerById() - 查询客户详情
  - [ ] listCustomers() - 查询客户列表
  - [ ] updateCustomer() - 修改客户信息
  - [ ] deleteCustomer() - 删除客户（软删除）

### 6.3 商品服务
- [ ] 创建 ProductService
  - [ ] createProduct() - 创建商品
  - [ ] getProductById() - 查询商品详情
  - [ ] listProducts() - 查询商品列表
  - [ ] updateProduct() - 修改商品
  - [ ] deleteProduct() - 删除商品（软删除）

### 6.4 商品分类服务
- [ ] 创建 ProductCategoryService
  - [ ] createCategory() - 创建分类
  - [ ] listCategories() - 查询分类树
  - [ ] updateCategory() - 修改分类
  - [ ] deleteCategory() - 删除分类

### 6.5 支付流水服务
- [ ] 创建 PaymentRecordService
  - [ ] listPaymentRecords() - 查询支付流水
  - [ ] deletePaymentRecord() - 删除支付记录（软删除，管理员）

### 6.6 地址服务
- [ ] 创建 SysAddressService
  - [ ] listAddressByParentId() - 查询子级地址
  - [ ] getFullAddressPath() - 获取完整地址路径

---

## 7. Controller 层 (P0)

### 7.1 账本控制器
- [ ] 创建 LedgerController
  - [ ] POST /api/ledgers - 创建账本
  - [ ] GET /api/ledgers/active - 查询活跃账单
  - [ ] GET /api/ledgers/customer/{customerId} - 查询客户账单
  - [ ] GET /api/ledgers/credit - 查询赊账账单
  - [ ] GET /api/ledgers/{id} - 查询账本详情
  - [ ] POST /api/ledgers/{id}/payment - 收款操作
  - [ ] POST /api/ledgers/{id}/move-to-credit - 转为赊账
  - [ ] POST /api/ledgers/{id}/discount-settle - 抹零结清
  - [ ] POST /api/ledgers/{id}/close - 关闭账单
  - [ ] PUT /api/ledgers/{id}/items - 修改账本明细

### 7.2 客户控制器
- [ ] 创建 CustomerController
  - [ ] POST /api/customers - 创建客户
  - [ ] GET /api/customers - 查询客户列表
  - [ ] GET /api/customers/{id} - 查询客户详情
  - [ ] PUT /api/customers/{id} - 修改客户
  - [ ] DELETE /api/customers/{id} - 删除客户

### 7.3 商品控制器
- [ ] 创建 ProductController
  - [ ] POST /api/products - 创建商品
  - [ ] GET /api/products - 查询商品列表
  - [ ] GET /api/products/{id} - 查询商品详情
  - [ ] PUT /api/products/{id} - 修改商品
  - [ ] DELETE /api/products/{id} - 删除商品

### 7.4 商品分类控制器
- [ ] 创建 ProductCategoryController
  - [ ] POST /api/product-categories - 创建分类
  - [ ] GET /api/product-categories - 查询分类树
  - [ ] PUT /api/product-categories/{id} - 修改分类
  - [ ] DELETE /api/product-categories/{id} - 删除分类

### 7.5 支付流水控制器
- [ ] 创建 PaymentRecordController
  - [ ] GET /api/payment-records - 查询支付流水

### 7.6 地址控制器
- [ ] 创建 SysAddressController
  - [ ] GET /api/addresses - 查询子级地址

---

## 8. 统计报表 (P1)

### 8.1 统计服务
- [ ] 创建 StatisticsService
  - [ ] getLedgerStatistics() - 账本统计
  - [ ] getCustomerDebtRanking() - 客户欠款排行
  - [ ] getTodayStatistics() - 今日统计

### 8.2 统计控制器
- [ ] 创建 StatisticsController
  - [ ] GET /api/statistics/ledger - 账本统计
  - [ ] GET /api/statistics/customer-debt - 客户欠款排行

---

## 9. 认证与权限 (P1)

### 9.1 微信登录（待讨论）
- [ ] 设计微信登录流程
- [ ] 实现微信登录接口
- [ ] 实现 Token 管理
- [ ] 实现用户注册绑定

### 9.2 权限控制
- [ ] 实现管理员权限校验
- [ ] 实现接口鉴权拦截器
- [ ] 实现用户上下文管理

---

## 10. 数据导入 (P2)

### 10.1 地址库导入
- [ ] 准备地址库数据（省市区街道村）
- [ ] 编写数据导入脚本
- [ ] 执行数据导入

### 10.2 测试数据
- [ ] 创建测试客户数据
- [ ] 创建测试商品数据
- [ ] 创建测试账本数据

---

## 11. 单元测试 (P2)

### 11.1 Service 层测试
- [ ] LedgerService 测试
- [ ] CustomerService 测试
- [ ] ProductService 测试
- [ ] PaymentRecordService 测试

### 11.2 Repository 层测试
- [ ] JPA Repository 测试
- [ ] MyBatis Mapper 测试

### 11.3 枚举映射测试
- [ ] JPA Converter 测试
- [ ] MyBatis TypeHandler 测试
- [ ] JSON 序列化测试

---

## 12. 集成测试 (P2)

### 12.1 API 测试
- [ ] 账本管理 API 测试
- [ ] 客户管理 API 测试
- [ ] 商品管理 API 测试
- [ ] 支付流水 API 测试

### 12.2 业务流程测试
- [ ] 完整缴费流程测试
- [ ] 部分缴费流程测试
- [ ] 赊账流程测试
- [ ] 抹零结清流程测试

---

## 13. 性能优化 (P3)

### 13.1 数据库优化
- [ ] 添加必要的索引
- [ ] 优化慢查询
- [ ] 配置数据库连接池

### 13.2 缓存优化
- [ ] 地址库缓存
- [ ] 商品分类缓存
- [ ] 用户信息缓存

---

## 14. 部署与运维 (P3)

### 14.1 部署准备
- [ ] 编写 Dockerfile
- [ ] 编写 docker-compose.yml
- [ ] 配置生产环境配置文件

### 14.2 监控告警
- [ ] 配置日志收集
- [ ] 配置性能监控
- [ ] 配置异常告警

---

## 15. 文档完善 (P2)

### 15.1 开发文档
- [x] ✅ ARCHITECTURE.md - 架构设计文档
- [x] ✅ API_DESIGN.md - API 接口设计
- [x] ✅ ENUM_MAPPING.md - 枚举映射规范
- [x] ✅ LEDGER_STATUS_TRANSITIONS.md - 账本状态转换
- [x] ✅ CODING_STANDARDS.md - 编码规范
- [x] ✅ REQUIREMENTS_CONFIRMED.md - 需求确认文档
- [x] ✅ DEVELOPMENT_TASKS.md - 开发任务清单
- [ ] DATABASE_DESIGN.md - 数据库设计文档（可选）

### 15.2 部署文档
- [ ] DEPLOYMENT.md - 部署指南
- [ ] TROUBLESHOOTING.md - 故障排查

---

## 任务进度统计

### 已完成
- ✅ 项目初始化
- ✅ 数据库设计
- ✅ BaseEntity 基类
- ✅ ErrorCode 错误码枚举
- ✅ 核心文档编写

### 进行中
- 🔄 实体层开发
- 🔄 Repository 层开发

### 待开始
- ⏳ Service 层开发
- ⏳ Controller 层开发
- ⏳ 认证与权限
- ⏳ 单元测试

---

## 下一步行动

### 优先级 P0 任务（本周完成）
1. 完成所有枚举类及其转换器
2. 完成所有实体类
3. 完成所有 Repository
4. 完成配置类（JPA、MyBatis、Jackson、Knife4j）
5. 完成全局异常处理器

### 优先级 P1 任务（下周完成）
1. 完成 DTO/VO 层
2. 完成 Converter 层
3. 完成核心 Service 层（Ledger、Customer）
4. 完成核心 Controller 层

---

**文档维护**: 本文档应随开发进度及时更新  
**最后更新**: 2025-11-24  
**维护团队**: Core Ledger Team
