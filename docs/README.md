# Core Ledger 项目文档

## 目录

### 架构设计文档
- [系统架构设计](ARCHITECTURE.md) - 分层架构、ORM策略、对象转换规范
- [API 接口设计](API_DESIGN.md) - RESTful API 详细设计文档

### 业务规则文档
- [账本状态转换规则](LEDGER_STATUS_TRANSITIONS.md) - 详细说明账本的 5 种状态及其转换规则

### 技术规范文档
- [编码规范](CODING_STANDARDS.md) - 命名规范、注释规范、代码风格、数据访问规范
- [枚举映射规范](ENUM_MAPPING.md) - JPA 和 MyBatis 枚举映射详细说明
- [PredicateBuilder 使用指南](PREDICATE_BUILDER_GUIDE.md) - JPA 单表条件查询规范
- [JPA 实体最佳实践](JPA_BEST_PRACTICES.md) - JPA 实体类开发规范
- [异常处理指南](EXCEPTION_HANDLING.md) - 统一异常处理和响应格式
- [需求确认文档](REQUIREMENTS_CONFIRMED.md) - 已确认的技术决策和业务规则

### 功能模块文档
- [登录认证模块](AUTH_MODULE.md) - 登录、注册、Token 管理
- [微信小程序对接指南](WECHAT_INTEGRATION.md) - 微信登录对接详细步骤

### API 文档
启动应用后访问：
- Knife4j UI: http://localhost:8080/api/doc.html
- Swagger UI: http://localhost:8080/api/swagger-ui.html

---

## 文档快速索引

### 新人入门
1. 阅读 [README.md](../README.md) 了解项目概况
2. 阅读 [ARCHITECTURE.md](ARCHITECTURE.md) 了解系统架构
3. 阅读 [CODING_STANDARDS.md](CODING_STANDARDS.md) 了解编码规范
4. 阅读 [ENUM_MAPPING.md](ENUM_MAPPING.md) 了解枚举映射

### 业务开发
1. 查看 [LEDGER_STATUS_TRANSITIONS.md](LEDGER_STATUS_TRANSITIONS.md) 了解核心业务逻辑
2. 查看 [API_DESIGN.md](API_DESIGN.md) 了解接口设计
3. 参考 [ARCHITECTURE.md](ARCHITECTURE.md) 进行分层开发

### 技术实现
1. [ARCHITECTURE.md](ARCHITECTURE.md) - DTO/VO/Entity 转换
2. [ENUM_MAPPING.md](ENUM_MAPPING.md) - 枚举类型处理
3. [PREDICATE_BUILDER_GUIDE.md](PREDICATE_BUILDER_GUIDE.md) - 单表条件查询
4. [JPA_BEST_PRACTICES.md](JPA_BEST_PRACTICES.md) - JPA 实体开发
5. [EXCEPTION_HANDLING.md](EXCEPTION_HANDLING.md) - 异常处理
6. [CODING_STANDARDS.md](CODING_STANDARDS.md) - 代码质量保证

---

**维护团队**: Core Ledger Team
**最后更新**: 2025-11-24
