# 登录认证模块使用文档

## 文档说明
本文档详细说明登录认证模块的使用方法和注意事项。

**创建日期**: 2025-11-24  
**维护团队**: Core Ledger Team  
**文档版本**: 1.0.0

---

## 1. 模块概述

### 1.1 功能特性

- ✅ **微信小程序登录**：支持微信 code 换取 openid 登录
- ✅ **手机号密码登录**：支持管理后台使用手机号密码登录
- ✅ **Token 管理**：基于 Redis 的 Token 存储，7天有效期
- ✅ **自动续期**：每次请求自动刷新 Token 过期时间
- ✅ **统一拦截**：自动拦截需要登录的接口
- ✅ **用户信息获取**：提供当前登录用户信息查询

### 1.2 技术架构

```
┌─────────────────────────────────────────────────────┐
│                   Controller 层                      │
│  AuthController: 登录、登出、获取用户信息            │
└─────────────────┬───────────────────────────────────┘
                  │
┌─────────────────┴───────────────────────────────────┐
│                   Service 层                         │
│  AuthService: 登录业务逻辑、Token 生成              │
└─────────────────┬───────────────────────────────────┘
                  │
┌─────────────────┴───────────────────────────────────┐
│                   Utils 层                           │
│  TokenUtil: Token 管理（Redis）                     │
│  WechatUtil: 微信 API 调用                          │
└─────────────────┬───────────────────────────────────┘
                  │
┌─────────────────┴───────────────────────────────────┐
│                   Interceptor 层                     │
│  AuthInterceptor: 认证拦截器                        │
└─────────────────────────────────────────────────────┘
```

---

## 2. API 接口

### 2.1 微信小程序登录

**接口**: `POST /api/auth/wechat-login`

**请求头**: 无需 Token

**请求体**:
```json
{
  "code": "071Ab2Ga1n8YYJ0MJVIa1Ht9Ga1Ab2G5",
  "nickname": "张三",
  "avatarUrl": "https://thirdwx.qlogo.cn/..."
}
```

**响应（已注册）**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "a1b2c3d4e5f6g7h8i9j0",
    "userInfo": {
      "id": 1,
      "phone": "13800138000",
      "role": 0,
      "roleDesc": "普通用户",
      "wxNickname": "张三",
      "wxAvatarUrl": "https://..."
    },
    "needBindPhone": false,
    "expireTime": "2025-12-01 12:00:00"
  }
}
```

**响应（未注册）**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "needBindPhone": true,
    "tempOpenid": "oUpF8uMuAJO_M2pxb1Q9zNjWeS6o"
  }
}
```

### 2.2 绑定手机号

**接口**: `POST /api/auth/bind-phone`

**请求头**: 无需 Token

**请求体**:
```json
{
  "openid": "oUpF8uMuAJO_M2pxb1Q9zNjWeS6o",
  "phone": "13800138000"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "a1b2c3d4e5f6g7h8i9j0",
    "userInfo": {
      "id": 2,
      "phone": "13800138000",
      "role": 0,
      "roleDesc": "普通用户"
    },
    "needBindPhone": false,
    "expireTime": "2025-12-01 12:00:00"
  }
}
```

### 2.3 手机号密码登录

**接口**: `POST /api/auth/login`

**请求头**: 无需 Token

**请求体**:
```json
{
  "phone": "13800138000",
  "password": "admin123"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "a1b2c3d4e5f6g7h8i9j0",
    "userInfo": {
      "id": 1,
      "username": "admin",
      "phone": "13800138000",
      "role": 1,
      "roleDesc": "管理员"
    },
    "needBindPhone": false,
    "expireTime": "2025-12-01 12:00:00"
  }
}
```

### 2.4 登出

**接口**: `POST /api/auth/logout`

**请求头**: `Authorization: Bearer {token}`

**响应**:
```json
{
  "code": 200,
  "message": "登出成功"
}
```

### 2.5 获取当前用户信息

**接口**: `GET /api/auth/current-user`

**请求头**: `Authorization: Bearer {token}`

**响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "admin",
    "phone": "13800138000",
    "role": 1,
    "roleDesc": "管理员",
    "wxNickname": "张三",
    "wxAvatarUrl": "https://..."
  }
}
```

---

## 3. 使用示例

### 3.1 小程序端登录

```javascript
// 1. 调用微信登录
wx.login({
  success: (res) => {
    // 2. 调用后端接口
    wx.request({
      url: 'https://api.example.com/api/auth/wechat-login',
      method: 'POST',
      data: {
        code: res.code
      },
      success: (res) => {
        if (res.data.code === 200) {
          const result = res.data.data;
          
          if (result.needBindPhone) {
            // 需要绑定手机号
            wx.navigateTo({
              url: '/pages/bind-phone/index?openid=' + result.tempOpenid
            });
          } else {
            // 登录成功，保存 Token
            wx.setStorageSync('token', result.token);
            wx.setStorageSync('userInfo', result.userInfo);
          }
        }
      }
    });
  }
});
```

### 3.2 管理后台登录

```javascript
// 使用 Axios
import axios from 'axios';

async function login(phone, password) {
  try {
    const response = await axios.post('/api/auth/login', {
      phone: phone,
      password: password
    });
    
    if (response.data.code === 200) {
      const { token, userInfo } = response.data.data;
      
      // 保存 Token 到 localStorage
      localStorage.setItem('token', token);
      localStorage.setItem('userInfo', JSON.stringify(userInfo));
      
      // 设置 Axios 默认请求头
      axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
      
      return true;
    }
  } catch (error) {
    console.error('登录失败:', error);
    return false;
  }
}
```

### 3.3 携带 Token 请求

```javascript
// 小程序端
wx.request({
  url: 'https://api.example.com/api/ledgers',
  method: 'GET',
  header: {
    'Authorization': 'Bearer ' + wx.getStorageSync('token')
  },
  success: (res) => {
    console.log('账本列表:', res.data);
  }
});

// Web 端（Axios）
axios.get('/api/ledgers', {
  headers: {
    'Authorization': `Bearer ${localStorage.getItem('token')}`
  }
}).then(res => {
  console.log('账本列表:', res.data);
});
```

### 3.4 获取当前登录用户

在 Service 或 Controller 中获取当前登录用户：

```java
import com.coreledger.interceptor.AuthInterceptor;
import com.coreledger.vo.auth.UserInfoVO;

@Service
public class LedgerService {
    
    public void createLedger(CreateLedgerDTO dto) {
        // 获取当前登录用户
        UserInfoVO currentUser = AuthInterceptor.getCurrentUser();
        Long userId = currentUser.getId();
        
        // 业务逻辑...
    }
}
```

---

## 4. 错误码说明

| 错误码 | 说明 | 解决方案 |
|--------|------|----------|
| 401 | 未登录或登录已过期 | 重新登录 |
| 403 | 无权限访问 | 检查用户角色权限 |
| 5003 | 用户名或密码错误 | 检查输入 |
| 5004 | 用户已被禁用 | 联系管理员 |
| 5005 | 微信登录失败 | 检查微信配置 |
| 5006 | 微信API调用失败 | 检查网络或微信服务状态 |
| 5007 | 手机号已被绑定 | 使用其他手机号 |

---

## 5. 权限控制

### 5.1 拦截器配置

拦截器会自动拦截所有 `/api/**` 路径，排除以下路径：

- `/api/auth/**` - 认证接口（登录、注册等）
- `/api/doc.html` - Knife4j 文档
- `/api/swagger-ui/**` - Swagger UI
- `/api/v3/api-docs/**` - OpenAPI 文档

### 5.2 角色权限

系统支持两种角色：

- **USER (0)**: 普通用户
  - 可以创建账本
  - 可以查看自己的数据
  - 不能执行管理员操作

- **ADMIN (1)**: 管理员
  - 拥有所有权限
  - 可以执行折扣结清等特殊操作
  - 可以管理所有用户数据

### 5.3 权限检查示例

```java
@Service
public class LedgerService {
    
    /**
     * 折扣结清（仅管理员）
     */
    public void discountSettle(Long ledgerId, BigDecimal discountAmount) {
        UserInfoVO currentUser = AuthInterceptor.getCurrentUser();
        
        // 检查是否为管理员
        if (currentUser.getRole() != 1) {
            throw new ForbiddenException("仅管理员可执行折扣结清操作");
        }
        
        // 业务逻辑...
    }
}
```

---

## 6. Token 管理

### 6.1 Token 存储

- **存储位置**: Redis
- **Key 格式**: `token:{token}`
- **Value**: UserInfoVO 对象（JSON 序列化）
- **有效期**: 7天
- **自动续期**: 每次请求自动刷新过期时间

### 6.2 Token 失效场景

1. **主动登出**: 调用 `/api/auth/logout` 接口
2. **Token 过期**: 7天未使用自动过期
3. **Redis 重启**: Redis 数据丢失（建议配置持久化）

### 6.3 Token 安全

- ✅ Token 使用 UUID 生成，随机性强
- ✅ Token 存储在 Redis，支持主动失效
- ✅ 每次请求验证 Token 有效性
- ✅ Token 过期后自动返回 401 错误

---

## 7. 配置说明

### 7.1 Redis 配置

```yaml
spring:
  data:
    redis:
      host: localhost      # Redis 地址
      port: 6379          # Redis 端口
      password:           # Redis 密码（可选）
      database: 0         # 数据库编号
      timeout: 5000ms     # 连接超时时间
      lettuce:
        pool:
          max-active: 8   # 最大连接数
          max-idle: 8     # 最大空闲连接
          min-idle: 0     # 最小空闲连接
          max-wait: -1ms  # 最大等待时间
```

### 7.2 微信配置

```yaml
wechat:
  miniapp:
    appid: wx1234567890abcdef          # 小程序 AppID
    secret: abcdef1234567890abcdef...  # 小程序 AppSecret
    api-domain: https://api.weixin.qq.com  # 微信 API 域名
```

### 7.3 Token 配置

Token 配置在 `TokenUtil.java` 中：

```java
// Token 有效期（天）
private static final int TOKEN_EXPIRE_DAYS = 7;
```

如需修改有效期，直接修改此常量即可。

---

## 8. 测试指南

### 8.1 测试准备

1. 启动 MySQL 数据库
2. 启动 Redis 服务
3. 配置微信 AppID 和 AppSecret
4. 启动 Spring Boot 应用

### 8.2 测试用例

#### 测试1：微信登录（新用户）

```bash
curl -X POST http://localhost:8080/api/auth/wechat-login \
  -H "Content-Type: application/json" \
  -d '{
    "code": "test_code_123"
  }'

# 预期响应: needBindPhone = true
```

#### 测试2：绑定手机号

```bash
curl -X POST http://localhost:8080/api/auth/bind-phone \
  -H "Content-Type: application/json" \
  -d '{
    "openid": "test_openid",
    "phone": "13800138000"
  }'

# 预期响应: 返回 token 和 userInfo
```

#### 测试3：手机号密码登录

```bash
# 先创建管理员账号（直接插入数据库）
INSERT INTO sys_user (username, password, phone, role, status) 
VALUES ('admin', '$2a$10$...', '13900139000', 1, 1);

# 测试登录
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "13900139000",
    "password": "admin123"
  }'
```

#### 测试4：携带 Token 请求

```bash
curl -X GET http://localhost:8080/api/auth/current-user \
  -H "Authorization: Bearer {your_token}"

# 预期响应: 返回当前用户信息
```

#### 测试5：登出

```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer {your_token}"

# 预期响应: code = 200
```

---

## 9. 常见问题

### Q1: Redis 连接失败

**错误**: `Unable to connect to Redis`

**解决**:
1. 检查 Redis 是否启动: `redis-cli ping`
2. 检查 application.yml 中的 Redis 配置
3. 检查防火墙是否开放 6379 端口

### Q2: 微信登录失败

**错误**: `code: 5006, message: 微信API调用失败`

**解决**:
1. 检查 AppID 和 AppSecret 是否正确
2. 检查网络是否能访问微信 API
3. 查看后端日志获取详细错误信息

### Q3: Token 无效

**错误**: `code: 401, message: 未登录或登录已过期`

**解决**:
1. 检查 Token 是否正确携带在请求头中
2. 检查 Token 格式: `Authorization: Bearer {token}`
3. Token 可能已过期，重新登录

### Q4: 手机号已被绑定

**错误**: `code: 5007, message: 该手机号已被其他账号绑定`

**解决**:
1. 使用其他手机号
2. 联系管理员解绑原账号
3. 或使用原账号登录

---

## 10. 最佳实践

### 10.1 前端

- ✅ 登录成功后立即保存 Token
- ✅ 每次请求都携带 Token
- ✅ Token 过期后自动跳转登录页
- ✅ 退出登录时清除本地 Token

### 10.2 后端

- ✅ 敏感操作验证用户权限
- ✅ 记录登录日志
- ✅ 定期清理过期 Token
- ✅ 使用 HTTPS 保护 Token 传输

### 10.3 安全

- ✅ 不要在日志中打印 Token
- ✅ 不要将 AppSecret 提交到代码仓库
- ✅ 生产环境使用强密码
- ✅ 定期更换密钥

---

## 11. 参考资料

- [微信小程序登录文档](https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/login.html)
- [Spring Data Redis 文档](https://docs.spring.io/spring-data/redis/docs/current/reference/html/)
- [微信对接指南](./WECHAT_INTEGRATION.md)

---

**文档维护**: 本文档应随功能更新及时维护  
**最后更新**: 2025-11-24  
**维护团队**: Core Ledger Team
