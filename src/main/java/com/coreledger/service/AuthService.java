package com.coreledger.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.coreledger.dto.auth.BindPhoneDTO;
import com.coreledger.dto.auth.PasswordLoginDTO;
import com.coreledger.dto.auth.WechatLoginDTO;
import com.coreledger.entity.SysUser;
import com.coreledger.enums.BusinessCode;
import com.coreledger.enums.Status;
import com.coreledger.enums.UserRole;
import com.coreledger.exception.BusinessException;
import com.coreledger.exception.UnauthorizedException;
import com.coreledger.repository.SysUserRepository;
import com.coreledger.utils.TokenUtil;
import com.coreledger.utils.WechatUtil;
import com.coreledger.vo.auth.LoginVO;
import com.coreledger.vo.auth.UserInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证服务
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserRepository sysUserRepository;
    private final WechatUtil wechatUtil;
    private final TokenUtil tokenUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 微信小程序登录
     *
     * @param dto 登录请求
     * @return 登录响应
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginVO wechatLogin(WechatLoginDTO dto) {
        // 1. 调用微信接口获取 openid
        JSONObject sessionData = wechatUtil.code2Session(dto.getCode());
        String openid = sessionData.getStr("openid");

        if (StrUtil.isBlank(openid)) {
            log.error("微信登录失败: 未获取到openid");
            throw new BusinessException(BusinessCode.WECHAT_LOGIN_FAILED, "微信登录失败");
        }

        log.info("微信登录: openid={}", openid);

        // 2. 查询用户是否存在
        SysUser user = sysUserRepository.findByWxOpenidAndStatus(openid, Status.ACTIVE).orElse(null);

        // 3. 用户不存在，需要绑定手机号
        if (user == null) {
            log.info("用户不存在，需要绑定手机号: openid={}", openid);
            return LoginVO.needBindPhone(openid);
        }

        // 4. 用户存在，更新微信信息（仅当数据库为空且前端有值时填充）
        boolean needUpdate = false;
        if (StrUtil.isBlank(user.getWxNickname()) && StrUtil.isNotBlank(dto.getNickname())) {
            user.setWxNickname(dto.getNickname());
            needUpdate = true;
        }
        if (StrUtil.isBlank(user.getWxAvatarUrl()) && StrUtil.isNotBlank(dto.getAvatarUrl())) {
            user.setWxAvatarUrl(dto.getAvatarUrl());
            needUpdate = true;
        }
        if (needUpdate) {
            sysUserRepository.save(user);
            log.info("更新用户微信信息: userId={}", user.getId());
        }

        // 5. 生成 Token 并返回
        return generateLoginResponse(user);
    }

    /**
     * 绑定手机号
     *
     * @param dto 绑定请求
     * @return 登录响应
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginVO bindPhone(BindPhoneDTO dto) {
        String openid = dto.getOpenid();
        String phone = dto.getPhone();

        // 1. 检查手机号是否已被绑定
        if (sysUserRepository.existsByPhoneAndStatus(phone, Status.ACTIVE)) {
            log.warn("手机号已被绑定: phone={}", phone);
            throw new BusinessException(BusinessCode.PHONE_ALREADY_BOUND);
        }

        // 2. 检查 openid 是否已注册（防止重复注册）
        if (sysUserRepository.existsByWxOpenidAndStatus(openid, Status.ACTIVE)) {
            log.warn("该微信账号已注册: openid={}", openid);
            throw new BusinessException(BusinessCode.WECHAT_LOGIN_FAILED, "该微信账号已注册");
        }

        // 3. 创建新用户
        SysUser user = new SysUser();
        user.setPhone(phone);
        user.setWxOpenid(openid);
        user.setRole(UserRole.USER);
        user.setStatus(Status.ACTIVE);

        // 如果有微信信息，一并保存
        // TODO: 从 encryptedData 解密获取微信手机号和其他信息

        user = sysUserRepository.save(user);
        log.info("创建新用户成功: userId={}, phone={}, openid={}", user.getId(), phone, openid);

        // 4. 生成 Token 并返回
        return generateLoginResponse(user);
    }

    /**
     * 手机号密码登录
     *
     * @param dto 登录请求
     * @return 登录响应
     */
    public LoginVO passwordLogin(PasswordLoginDTO dto) {
        // 1. 查询用户
        SysUser user = sysUserRepository.findByPhoneAndStatus(dto.getPhone(), Status.ACTIVE)
                .orElseThrow(() -> new BusinessException(BusinessCode.USER_CREDENTIALS_INVALID));

        // 2. 验证密码
        if (StrUtil.isBlank(user.getPassword())) {
            log.warn("用户未设置密码: userId={}", user.getId());
            throw new BusinessException(BusinessCode.USER_CREDENTIALS_INVALID, "该账号未设置密码，请使用微信登录");
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            log.warn("密码错误: userId={}", user.getId());
            throw new BusinessException(BusinessCode.USER_CREDENTIALS_INVALID);
        }

        // 3. 检查用户状态
        if (user.getStatus() != Status.ACTIVE) {
            log.warn("用户已被禁用: userId={}", user.getId());
            throw new BusinessException(BusinessCode.USER_DISABLED);
        }

        log.info("密码登录成功: userId={}, phone={}", user.getId(), user.getPhone());

        // 4. 生成 Token 并返回
        return generateLoginResponse(user);
    }

    /**
     * 登出
     *
     * @param token Token
     */
    public void logout(String token) {
        if (StrUtil.isNotBlank(token)) {
            // 移除 Bearer 前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            tokenUtil.removeToken(token);
            log.info("用户登出成功");
        }
    }

    /**
     * 获取当前用户信息
     *
     * @param token Token
     * @return 用户信息
     */
    public UserInfoVO getCurrentUser(String token) {
        if (StrUtil.isBlank(token)) {
            throw new UnauthorizedException();
        }

        // 移除 Bearer 前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        UserInfoVO userInfo = tokenUtil.getUserInfo(token);
        if (userInfo == null) {
            throw new UnauthorizedException();
        }

        return userInfo;
    }

    /**
     * 生成登录响应
     *
     * @param user 用户实体
     * @return 登录响应
     */
    private LoginVO generateLoginResponse(SysUser user) {
        // 构建用户信息
        UserInfoVO userInfo = new UserInfoVO();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setPhone(user.getPhone());
        userInfo.setRole(user.getRole().getValue());
        userInfo.setRoleDesc(user.getRole().getDescription());
        userInfo.setWxNickname(user.getWxNickname());
        userInfo.setWxAvatarUrl(user.getWxAvatarUrl());

        // 生成 Token
        String token = tokenUtil.generateToken(userInfo);

        // 返回登录响应
        return LoginVO.success(token, userInfo, tokenUtil.getExpireTime());
    }
}
