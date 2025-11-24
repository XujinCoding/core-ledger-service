# Core Ledger Backend (核芯账本系统)

## 项目简介

Core Ledger 是一个基于 Spring Boot 3.x 的单体应用，用于管理客户账本、订单和支付流水。采用 JPA + MyBatis 混合 ORM 策略，使用 Flyway 进行数据库版本管理。

## 技术栈

- **Java**: 17
- **Spring Boot**: 3.2.0
- **ORM**: Spring Data JPA + MyBatis 3.0.3
- **Database**: MySQL 8.0
- **Migration**: Flyway 9.22.3
- **Tools**: Lombok, MapStruct, Knife4j
- **Build**: Maven 3.6+

## 项目结构

```
core-ledger-backend/
├── src/main/java/com/coreledger/
│   ├── entity/          # JPA实体类
│   ├── repository/      # JPA Repository接口
│   ├── mapper/          # MyBatis Mapper接口
│   ├── service/         # 业务逻辑层
│   ├── controller/      # REST API控制器
│   ├── dto/             # 数据传输对象
│   ├── config/          # 配置类
│   ├── enums/           # 枚举类
│   └── exception/       # 异常处理
├── src/main/resources/
│   ├── db/migration/    # Flyway SQL脚本
│   ├── mapper/          # MyBatis XML映射文件
│   └── application.yml  # 主配置文件
└── pom.xml
```

## 快速开始

### 1. 环境准备

- JDK 17+
- Maven 3.6+
- MySQL 8.0+

### 2. 创建数据库

```sql
CREATE DATABASE core_ledger CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. 配置数据库

编辑 `src/main/resources/application.yml`，修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/core_ledger?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

### 4. 运行应用

```bash
mvn clean install
mvn spring-boot:run
```

### 5. 访问 API 文档

应用启动后，访问：

- Knife4j UI: http://localhost:8080/api/doc.html
- Swagger UI: http://localhost:8080/api/swagger-ui.html

## 数据库表设计

### 核心表结构

1. **sys_user** - 用户表
2. **sys_address** - 行政区划地址库
3. **customer** - 客户信息表
4. **product_category** - 商品分类表
5. **product** - 商品信息表
6. **ledger** - 账本主表（核心）
7. **ledger_item** - 账本明细表
8. **payment_record** - 支付流水表

### 账本状态说明

账本（Ledger）有 5 种状态：

| 状态码 | 枚举值 | 中文名称 | 说明 | 主页面显示 |
|-------|--------|---------|------|-----------|
| 1 | IN_PROGRESS | 进行中 | 账单创建，未收款 | ✅ 显示 |
| 2 | PARTIAL | 部分缴费 | 已收部分款项，继续收款 | ✅ 显示 |
| 3 | CLEARED | 已结清 | 完全缴费或抹零结清 | ❌ 不显示 |
| 4 | ON_CREDIT | 赊账中 | 客户赊账，暂不催收 | ❌ 不显示 |
| 5 | CLOSED | 已关闭 | 作废的账单 | ❌ 不显示 |

**主页面查询规则**：只显示"正在处理中"的账单（IN_PROGRESS 和 PARTIAL）

**客户详情页**：显示该客户所有状态的账单（包括赊账和已结清）

详细的状态转换规则和业务逻辑请参考：[账本状态转换规则](docs/LEDGER_STATUS_TRANSITIONS.md)

### 通用字段（BaseEntity）

所有表包含以下基础字段：

- `id`: 主键（自增）
- `memo`: 备注
- `status`: 状态（1=启用，0=禁用）
- `create_instant`: 创建时间
- `modify_instant`: 修改时间

## ORM 策略

### JPA (写操作)

- 所有实体定义
- CRUD 操作（增删改）
- 简单查询（findById, findByXxx）

### MyBatis (复杂查询)

- 联表查询
- 动态 SQL
- 报表统计

## 默认管理员账号

- 用户名: `admin`
- 密码: `admin123`
- 手机号: `13800138000`

## 开发规范

### 代码风格

- 使用 Lombok 简化代码
- 使用 MapStruct 进行 DTO 转换
- 遵循 RESTful API 设计规范

### 数据库变更

所有数据库变更必须通过 Flyway 脚本管理，命名规范：

```
V{version}__{description}.sql

示例:
V1.0.0__init_schema.sql
V1.0.1__add_user_avatar.sql
```

## License

Apache 2.0
