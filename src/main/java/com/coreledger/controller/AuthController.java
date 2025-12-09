package com.coreledger.controller;

import com.coreledger.common.Result;
import com.coreledger.dto.auth.BindMerchantDTO;
import com.coreledger.dto.auth.CustomerRegisterDTO;
import com.coreledger.dto.auth.MerchantRegisterDTO;
import com.coreledger.dto.auth.PasswordLoginDTO;
import com.coreledger.dto.auth.SwitchIdentityDTO;
import com.coreledger.dto.auth.WechatLoginDTO;
import com.coreledger.service.AuthService;
import com.coreledger.vo.auth.LoginVO;
import com.coreledger.vo.auth.UserIdentitiesVO;
import com.coreledger.vo.auth.CurrentUserIdentityInfo;
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
    @Operation(summary = "微信小程序登录", description = "通过微信code登录，根据返回的needSupplement和isNewUser判断是否需要补充信息或注册")
    @PostMapping("/wechat-login")
    public Result<LoginVO> wechatLogin(@Valid @RequestBody WechatLoginDTO dto) {
        LoginVO loginVO = authService.wechatLogin(dto);
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
    public Result<CurrentUserIdentityInfo> getCurrentUser() {
        CurrentUserIdentityInfo userInfo = authService.getCurrentUser();
        return Result.success(userInfo);
    }

    /**
     * 商户微信注册
     */
    @Operation(summary = "商户微信注册", description = "商户通过微信注册，一次性提交所有信息")
    @PostMapping("/merchant/wechat/register")
    public Result<LoginVO> merchantWechatRegister(@Valid @RequestBody MerchantRegisterDTO dto) {
        LoginVO response = authService.merchantWechatRegister(dto);
        return Result.success(response);
    }

    /**
     * 客户微信注册
     */
    @Operation(summary = "客户微信注册", description = "客户通过微信注册，只需手机号")
    @PostMapping("/customer/wechat/register")
    public Result<LoginVO> customerWechatRegister(@Valid @RequestBody CustomerRegisterDTO dto) {
        LoginVO response = authService.customerWechatRegister(dto);
        return Result.success(response);
    }

    /**
     * 客户扫码绑定商户
     */
    @Operation(summary = "客户扫码绑定商户", description = "客户通过邀请码绑定商户")
    @PostMapping("/customer/bind-merchant")
    public Result<LoginVO> bindMerchant(@Valid @RequestBody BindMerchantDTO dto) {
        LoginVO response = authService.bindMerchant(dto);
        return Result.success(response);
    }

    /**
     * 切换身份
     */
    @Operation(summary = "切换身份", description = "用户在商户和客户身份之间切换")
    @PostMapping("/switch-identity")
    public Result<LoginVO> switchIdentity(@Valid @RequestBody SwitchIdentityDTO dto) {
        LoginVO response = authService.switchIdentity(dto);
        return Result.success(response);
    }

    /**
     * 获取用户的所有身份
     */
    @Operation(summary = "获取用户的所有身份", description = "获取当前用户拥有的所有身份（商户和客户）")
    @GetMapping("/identities")
    public Result<UserIdentitiesVO> getUserIdentities() {
        UserIdentitiesVO response = authService.getUserIdentities();
        return Result.success(response);
    }
}
