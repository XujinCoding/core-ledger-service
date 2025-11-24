# 微信小程序对接指南

## 文档说明
本文档详细说明如何对接微信小程序登录功能。

**创建日期**: 2025-11-24  
**维护团队**: Core Ledger Team  
**文档版本**: 1.0.0

---

## 1. 前置准备

### 1.1 注册微信小程序

1. 访问 [微信公众平台](https://mp.weixin.qq.com/)
2. 注册小程序账号
3. 完成小程序信息填写和认证

### 1.2 获取小程序凭证

登录微信公众平台，进入 **开发** → **开发管理** → **开发设置**，获取：

- **AppID**: 小程序唯一标识
- **AppSecret**: 小程序密钥

### 1.3 配置服务器域名

在 **开发** → **开发管理** → **开发设置** → **服务器域名** 中配置：

```
request合法域名: https://your-api-domain.com
```

---

## 2. 后端配置

### 2.1 修改 application.yml

将获取的 AppID 和 AppSecret 填入配置文件：

```yaml
wechat:
  miniapp:
    appid: wx1234567890abcdef  # 替换为你的 AppID
    secret: abcdef1234567890abcdef1234567890ab  # 替换为你的 AppSecret
    api-domain: https://api.weixin.qq.com
```

### 2.2 启动 Redis

登录模块依赖 Redis 存储 Token，确保 Redis 已启动：

```bash
# Windows
redis-server.exe

# Linux/Mac
redis-server
```

如果 Redis 有密码，修改 application.yml：

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: your_redis_password  # 设置密码
```

---

## 3. 小程序端开发

### 3.1 获取登录凭证

在小程序端调用 `wx.login()` 获取临时登录凭证 code：

```javascript
// 小程序登录
wx.login({
  success: (res) => {
    if (res.code) {
      console.log('登录凭证:', res.code);
      // 将 code 发送到后端
      loginToBackend(res.code);
    } else {
      console.error('登录失败:', res.errMsg);
    }
  }
});
```

### 3.2 调用后端登录接口

#### 方式一：基础登录（推荐）

```javascript
function loginToBackend(code) {
  wx.request({
    url: 'https://your-api-domain.com/api/auth/wechat-login',
    method: 'POST',
    data: {
      code: code,
      nickname: '',  // 可选
      avatarUrl: ''  // 可选
    },
    success: (res) => {
      console.log('登录响应:', res.data);
      
      if (res.data.code === 200) {
        const result = res.data.data;
        
        // 判断是否需要绑定手机号
        if (result.needBindPhone) {
          // 需要绑定手机号
          console.log('需要绑定手机号, tempOpenid:', result.tempOpenid);
          // 跳转到绑定手机号页面
          wx.navigateTo({
            url: '/pages/bind-phone/index?openid=' + result.tempOpenid
          });
        } else {
          // 登录成功
          console.log('登录成功, token:', result.token);
          // 保存 token
          wx.setStorageSync('token', result.token);
          wx.setStorageSync('userInfo', result.userInfo);
          
          // 跳转到首页
          wx.switchTab({
            url: '/pages/index/index'
          });
        }
      } else {
        wx.showToast({
          title: res.data.message || '登录失败',
          icon: 'none'
        });
      }
    },
    fail: (err) => {
      console.error('请求失败:', err);
      wx.showToast({
        title: '网络错误',
        icon: 'none'
      });
    }
  });
}
```

#### 方式二：获取用户信息后登录

```javascript
// 1. 先获取用户信息
wx.getUserProfile({
  desc: '用于完善用户资料',
  success: (profileRes) => {
    console.log('用户信息:', profileRes.userInfo);
    
    // 2. 再登录
    wx.login({
      success: (loginRes) => {
        if (loginRes.code) {
          // 3. 调用后端接口
          wx.request({
            url: 'https://your-api-domain.com/api/auth/wechat-login',
            method: 'POST',
            data: {
              code: loginRes.code,
              nickname: profileRes.userInfo.nickName,
              avatarUrl: profileRes.userInfo.avatarUrl
            },
            success: (res) => {
              // 处理登录响应（同上）
            }
          });
        }
      }
    });
  },
  fail: (err) => {
    console.error('获取用户信息失败:', err);
  }
});
```

### 3.3 绑定手机号

#### 方式一：手动输入手机号（推荐）

```javascript
// bind-phone.wxml
<view class="container">
  <input type="number" placeholder="请输入手机号" bindinput="onPhoneInput" />
  <button bindtap="bindPhone">绑定手机号</button>
</view>

// bind-phone.js
Page({
  data: {
    openid: '',
    phone: ''
  },
  
  onLoad(options) {
    this.setData({
      openid: options.openid
    });
  },
  
  onPhoneInput(e) {
    this.setData({
      phone: e.detail.value
    });
  },
  
  bindPhone() {
    const { openid, phone } = this.data;
    
    // 验证手机号格式
    if (!/^1[3-9]\d{9}$/.test(phone)) {
      wx.showToast({
        title: '手机号格式不正确',
        icon: 'none'
      });
      return;
    }
    
    // 调用后端接口
    wx.request({
      url: 'https://your-api-domain.com/api/auth/bind-phone',
      method: 'POST',
      data: {
        openid: openid,
        phone: phone
      },
      success: (res) => {
        if (res.data.code === 200) {
          const result = res.data.data;
          
          // 保存 token
          wx.setStorageSync('token', result.token);
          wx.setStorageSync('userInfo', result.userInfo);
          
          wx.showToast({
            title: '绑定成功',
            icon: 'success'
          });
          
          // 跳转到首页
          setTimeout(() => {
            wx.switchTab({
              url: '/pages/index/index'
            });
          }, 1500);
        } else {
          wx.showToast({
            title: res.data.message || '绑定失败',
            icon: 'none'
          });
        }
      },
      fail: (err) => {
        console.error('请求失败:', err);
        wx.showToast({
          title: '网络错误',
          icon: 'none'
        });
      }
    });
  }
});
```

#### 方式二：使用微信手机号快速验证（需要认证）

```javascript
// 注意：此方式需要小程序已认证
<button open-type="getPhoneNumber" bindgetphonenumber="getPhoneNumber">
  授权手机号
</button>

// 处理手机号授权
getPhoneNumber(e) {
  console.log('手机号授权:', e.detail);
  
  if (e.detail.errMsg === 'getPhoneNumber:ok') {
    // 获取到加密数据
    const { encryptedData, iv } = e.detail;
    
    // 调用后端接口（需要后端实现解密）
    wx.request({
      url: 'https://your-api-domain.com/api/auth/bind-phone',
      method: 'POST',
      data: {
        openid: this.data.openid,
        encryptedData: encryptedData,
        iv: iv
      },
      success: (res) => {
        // 处理绑定结果
      }
    });
  } else {
    wx.showToast({
      title: '授权失败',
      icon: 'none'
    });
  }
}
```

### 3.4 携带 Token 请求接口

登录成功后，后续所有请求都需要携带 Token：

```javascript
// 封装请求方法
function request(url, method, data) {
  const token = wx.getStorageSync('token');
  
  return new Promise((resolve, reject) => {
    wx.request({
      url: 'https://your-api-domain.com' + url,
      method: method,
      data: data,
      header: {
        'Authorization': 'Bearer ' + token,  // 携带 Token
        'Content-Type': 'application/json'
      },
      success: (res) => {
        if (res.data.code === 401) {
          // Token 过期，跳转到登录页
          wx.removeStorageSync('token');
          wx.removeStorageSync('userInfo');
          wx.redirectTo({
            url: '/pages/login/index'
          });
          reject(new Error('未登录'));
        } else {
          resolve(res.data);
        }
      },
      fail: (err) => {
        reject(err);
      }
    });
  });
}

// 使用示例
request('/api/ledgers', 'GET', {})
  .then(res => {
    console.log('账本列表:', res.data);
  })
  .catch(err => {
    console.error('请求失败:', err);
  });
```

---

## 4. 登录流程图

```
┌─────────────┐
│ 小程序启动   │
└──────┬──────┘
       │
       ▼
┌─────────────────┐
│ 检查本地 Token  │
└──────┬──────────┘
       │
       ├─── 有 Token ───┐
       │                │
       │                ▼
       │         ┌──────────────┐
       │         │ 验证 Token   │
       │         └──────┬───────┘
       │                │
       │                ├─── 有效 ───→ 进入首页
       │                │
       │                └─── 无效 ───┐
       │                              │
       └─── 无 Token ─────────────────┤
                                      │
                                      ▼
                              ┌──────────────┐
                              │ wx.login()   │
                              │ 获取 code    │
                              └──────┬───────┘
                                     │
                                     ▼
                              ┌──────────────────┐
                              │ 调用后端登录接口 │
                              └──────┬───────────┘
                                     │
                                     ├─── 已注册 ───→ 保存 Token → 进入首页
                                     │
                                     └─── 未注册 ───→ 绑定手机号 → 保存 Token → 进入首页
```

---

## 5. 接口说明

### 5.1 微信小程序登录

**接口**: `POST /api/auth/wechat-login`

**请求参数**:
```json
{
  "code": "071Ab2Ga1n8YYJ0MJVIa1Ht9Ga1Ab2G5",
  "nickname": "张三",  // 可选
  "avatarUrl": "https://..."  // 可选
}
```

**响应示例（已注册）**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "a1b2c3d4e5f6...",
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
  },
  "timestamp": "2025-11-24 12:00:00"
}
```

**响应示例（未注册）**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "needBindPhone": true,
    "tempOpenid": "oUpF8uMuAJO_M2pxb1Q9zNjWeS6o"
  },
  "timestamp": "2025-11-24 12:00:00"
}
```

### 5.2 绑定手机号

**接口**: `POST /api/auth/bind-phone`

**请求参数**:
```json
{
  "openid": "oUpF8uMuAJO_M2pxb1Q9zNjWeS6o",
  "phone": "13800138000"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "a1b2c3d4e5f6...",
    "userInfo": {
      "id": 2,
      "phone": "13800138000",
      "role": 0,
      "roleDesc": "普通用户"
    },
    "needBindPhone": false,
    "expireTime": "2025-12-01 12:00:00"
  },
  "timestamp": "2025-11-24 12:00:00"
}
```

---

## 6. 常见问题

### Q1: code 已被使用

**错误**: `errcode: 40163, errmsg: code been used`

**原因**: 每个 code 只能使用一次，5分钟内有效

**解决**: 重新调用 `wx.login()` 获取新的 code

### Q2: AppID 或 AppSecret 错误

**错误**: `errcode: 40013, errmsg: invalid appid`

**原因**: 配置文件中的 appid 或 secret 不正确

**解决**: 检查 application.yml 中的配置是否正确

### Q3: Token 过期

**错误**: `code: 401, message: 未登录或登录已过期`

**原因**: Token 已过期（默认7天）

**解决**: 清除本地 Token，重新登录

### Q4: 手机号已被绑定

**错误**: `code: 5007, message: 该手机号已被其他账号绑定`

**原因**: 该手机号已经绑定了其他微信账号

**解决**: 使用其他手机号，或联系管理员解绑

### Q5: Redis 连接失败

**错误**: `Unable to connect to Redis`

**原因**: Redis 未启动或配置错误

**解决**: 
1. 启动 Redis: `redis-server`
2. 检查 application.yml 中的 Redis 配置

---

## 7. 安全建议

### 7.1 保护 AppSecret

- ❌ 不要将 AppSecret 提交到公开的代码仓库
- ✅ 使用环境变量或配置中心管理敏感信息
- ✅ 定期更换 AppSecret

### 7.2 Token 安全

- ✅ Token 存储在小程序的 Storage 中（已加密）
- ✅ 不要将 Token 打印到日志
- ✅ Token 过期后自动跳转登录页

### 7.3 HTTPS

- ✅ 生产环境必须使用 HTTPS
- ✅ 配置 SSL 证书

---

## 8. 测试建议

### 8.1 开发阶段

1. 使用微信开发者工具测试
2. 开启"不校验合法域名"选项
3. 使用 localhost 或内网 IP 测试

### 8.2 生产环境

1. 配置服务器域名白名单
2. 使用真实的 AppID 和 AppSecret
3. 完整测试登录、绑定、Token 过期等场景

---

## 9. 参考资料

- [微信小程序登录文档](https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/login.html)
- [wx.login API](https://developers.weixin.qq.com/miniprogram/dev/api/open-api/login/wx.login.html)
- [code2Session 接口](https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/login/auth.code2Session.html)

---

**文档维护**: 本文档应随微信 API 更新及时维护  
**最后更新**: 2025-11-24  
**维护团队**: Core Ledger Team
