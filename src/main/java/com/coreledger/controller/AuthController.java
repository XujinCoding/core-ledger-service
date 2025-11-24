package com.coreledger.controller;

import com.coreledger.common.Result;
import com.coreledger.dto.auth.BindPhoneDTO;
import com.coreledger.dto.auth.PasswordLoginDTO;
import com.coreledger.dto.auth.WechatLoginDTO;
import com.coreledger.service.AuthService;
import com.coreledger.vo.auth.LoginVO;
import com.coreledger.vo.auth.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Tag(name = "认证管理", description = "用户登录、登出、注册等接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 微信小程序登录
     */
    @Operation(summary = "微信小程序登录", description = "通过微信code登录，首次登录需要绑定手机号")
    @PostMapping("/wechat-login")
    public Result<LoginVO> wechatLogin(@Valid @RequestBody WechatLoginDTO dto) {
        LoginVO loginVO = authService.wechatLogin(dto);
        return Result.success(loginVO);
    }

    /**
     * 绑定手机号
     */
    @Operation(summary = "绑定手机号", description = "微信登录后绑定手机号完成注册")
    @PostMapping("/bind-phone")
    public Result<LoginVO> bindPhone(@Valid @RequestBody BindPhoneDTO dto) {
        LoginVO loginVO = authService.bindPhone(dto);
        return Result.success(loginVO);
    }

    /**
     * 手机号密码登录
     */
    @Operation(summary = "手机号密码登录", description = "使用手机号和密码登录（管理后台）")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody PasswordLoginDTO dto) {
        LoginVO loginVO = authService.passwordLogin(dto);
        return Result.success(loginVO);
    }

    /**
     * 登出
     */
    @Operation(summary = "登出", description = "退出登录，清除Token")
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        authService.logout(token);
        return Result.success("登出成功");
    }

    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取当前用户信息", description = "根据Token获取当前登录用户信息")
    @GetMapping("/current-user")
    public Result<UserInfoVO> getCurrentUser(@RequestHeader("Authorization") String token) {
        UserInfoVO userInfo = authService.getCurrentUser(token);
        return Result.success(userInfo);
    }
}
