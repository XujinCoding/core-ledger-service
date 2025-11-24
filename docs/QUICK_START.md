# Core Ledger 快速启动指南

## 1. 环境准备

### 1.1 必需软件

- **JDK 17+**
- **Maven 3.8+**
- **MySQL 8.0+**
- **Redis 6.0+**
- **IDEA / Eclipse**（推荐 IDEA）

### 1.2 检查环境

```bash
# 检查 Java 版本
java -version

# 检查 Maven 版本
mvn -version

# 检查 MySQL
mysql --version

# 检查 Redis
redis-cli --version
```

---

## 2. 数据库准备

### 2.1 创建数据库

```sql
CREATE DATABASE core_ledger 
  DEFAULT CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;
```

### 2.2 配置数据库连接

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/core_ledger?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root        # 修改为你的数据库用户名
    password: root        # 修改为你的数据库密码
```

---

## 3. Redis 准备

### 3.1 启动 Redis

```bash
# Windows
redis-server.exe

# Linux/Mac
redis-server
```

### 3.2 配置 Redis 连接

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password:           # 如果有密码，填写这里
```

---

## 4. 微信配置（可选）

如果需要测试微信登录，配置微信小程序信息：

```yaml
wechat:
  miniapp:
    appid: your_appid_here      # 替换为你的 AppID
    secret: your_secret_here    # 替换为你的 AppSecret
```

**注意**: 如果暂时不测试微信登录，可以跳过此步骤。

---

## 5. 启动项目

### 5.1 使用 IDEA 启动

1. 打开项目
2. 等待 Maven 依赖下载完成
3. 找到 `CoreLedgerApplication.java`
4. 右键 → Run 'CoreLedgerApplication'

### 5.2 使用 Maven 启动

```bash
# 进入项目目录
cd core-ledger-service

# 编译项目
mvn clean package -DskipTests

# 启动项目
java -jar target/core-ledger-backend-1.0.0-SNAPSHOT.jar
```

### 5.3 验证启动成功

看到以下日志表示启动成功：

```
Started CoreLedgerApplication in 5.123 seconds
```

访问 API 文档：
- Knife4j: http://localhost:8080/api/doc.html
- Swagger: http://localhost:8080/api/swagger-ui.html

---

## 6. 测试接口

### 6.1 创建管理员账号

由于首次启动数据库为空，需要手动创建管理员账号：

```sql
-- 密码: admin123 (BCrypt 加密后)
INSERT INTO sys_user (username, password, phone, role, status, create_instant, modify_instant, version) 
VALUES (
  'admin', 
  '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH',  -- admin123
  '13900139000', 
  1,  -- 管理员
  1,  -- 启用
  NOW(), 
  NOW(), 
  0
);
```

### 6.2 测试登录

使用 Postman 或 curl 测试：

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "13900139000",
    "password": "admin123"
  }'
```

响应示例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "a1b2c3d4e5f6g7h8i9j0",
    "userInfo": {
      "id": 1,
      "username": "admin",
      "phone": "13900139000",
      "role": 1,
      "roleDesc": "管理员"
    },
    "expireTime": "2025-12-01 12:00:00"
  }
}
```

### 6.3 测试需要登录的接口

```bash
# 获取当前用户信息
curl -X GET http://localhost:8080/api/auth/current-user \
  -H "Authorization: Bearer {your_token}"
```

---

## 7. 常见问题

### Q1: 数据库连接失败

**错误**: `Communications link failure`

**解决**:
1. 检查 MySQL 是否启动
2. 检查用户名密码是否正确
3. 检查数据库是否已创建

### Q2: Redis 连接失败

**错误**: `Unable to connect to Redis`

**解决**:
1. 检查 Redis 是否启动: `redis-cli ping`
2. 检查端口是否正确（默认 6379）
3. 检查防火墙设置

### Q3: 端口被占用

**错误**: `Port 8080 was already in use`

**解决**:
1. 修改 application.yml 中的端口号
2. 或关闭占用 8080 端口的程序

### Q4: Flyway 迁移失败

**错误**: `Flyway migration failed`

**解决**:
1. 删除数据库重新创建
2. 或清空 `flyway_schema_history` 表
3. 检查 SQL 脚本是否正确

---

## 8. 下一步

- 📖 阅读 [登录认证模块文档](AUTH_MODULE.md)
- 📖 阅读 [微信对接指南](WECHAT_INTEGRATION.md)
- 📖 阅读 [API 设计文档](API_DESIGN.md)
- 🔧 开始开发业务功能

---

**祝你使用愉快！**

如有问题，请查看完整文档或联系开发团队。
