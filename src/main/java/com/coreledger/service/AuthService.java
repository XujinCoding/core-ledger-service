package com.coreledger.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.coreledger.dto.auth.*;
import com.coreledger.entity.Customer;
import com.coreledger.entity.Merchant;
import com.coreledger.entity.SysUser;
import com.coreledger.enums.*;
import com.coreledger.exception.BusinessException;
import com.coreledger.exception.NotFoundException;
import com.coreledger.exception.UnauthorizedException;
import com.coreledger.repository.SysUserRepository;
import com.coreledger.utils.AppSessionContext;
import com.coreledger.utils.TokenUtil;
import com.coreledger.utils.WechatUtil;
import com.coreledger.vo.auth.LoginVO;
import com.coreledger.vo.auth.UserIdentitiesVO;
import com.coreledger.vo.auth.UserInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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

    private static final String REDIS_KEY_LAST_CUSTOMER = "last_customer_";
    private static final int LAST_CUSTOMER_EXPIRE_DAYS = 30;
    private static final String TOKEN_PREFIX = "Bearer ";

    private final SysUserRepository sysUserRepository;
    private final WechatUtil wechatUtil;
    private final TokenUtil tokenUtil;
    private final MerchantService merchantService;
    private final CustomerService customerService;
    private final RedisTemplate<String, Long> redisTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 微信小程序登录
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginVO wechatLogin(WechatLoginDTO dto) {
        // 1. 校验
        String openid = validateWechatLogin(dto.getCode());
        log.info("微信登录: openid={}, identityType={}", openid, dto.getIdentityType());

        // 2. 获取数据
        SysUser user = sysUserRepository.findByWxOpenidAndStatus(openid, Status.ACTIVE)
                .orElseGet(() -> createNewUser(openid));

        // 3. 处理
        if (dto.getIdentityType() == IdentityType.MERCHANT_OWNER) {
            return handleMerchantLogin(user);
        } else if (dto.getIdentityType() == IdentityType.CUSTOMER) {
            return handleCustomerLogin(user);
        } else {
            throw new BusinessException(BusinessCode.INVALID_PARAMETER, "无效的身份类型");
        }
    }

    /**
     * 手机号密码登录
     */
    public LoginVO passwordLogin(PasswordLoginDTO dto) {
        // 1. 校验
        SysUser user = validatePasswordLogin(dto);
        log.info("密码登录成功: userId={}, phone={}", user.getId(), user.getPhone());

        // 2. 返回
        return generateLoginResponse(user);
    }

    /**
     * 商户微信注册
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginVO merchantWechatRegister(MerchantRegisterDTO dto) {
        // 1. 校验
        validateMerchantRegister(dto);

        // 2. 获取数据
        SysUser user = sysUserRepository.findByWxOpenidAndStatus(dto.getOpenid(), Status.ACTIVE)
                .orElseThrow(() -> {
                    return new BusinessException(BusinessCode.USER_NOT_FOUND, "用户不存在，请先登录");
                });

        // 3. 处理
        user.setPhone(dto.getPhone());
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setWxNickname(dto.getNickname());
        user.setWxAvatarUrl(dto.getAvatarUrl());
        sysUserRepository.save(user);

        Merchant merchant = merchantService.createMerchant(dto.getMerchantName(), user.getId());
        log.info("商户注册成功: userId={}, merchantId={}, phone={}", user.getId(), merchant.getId(), dto.getPhone());

        // 4. 返回
        return generateLoginResponse(user, merchant.getId(), null);
    }

    /**
     * 客户微信注册
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginVO customerWechatRegister(CustomerRegisterDTO dto) {
        // 1. 校验
        validateCustomerRegister(dto);

        // 2. 获取数据
        SysUser user = sysUserRepository.findByWxOpenidAndStatus(dto.getOpenid(), Status.ACTIVE)
                .orElseThrow(() -> {
                    log.error("客户注册失败: 用户不存在, openid={}", dto.getOpenid());
                    return new BusinessException(BusinessCode.USER_NOT_FOUND, "用户不存在，请先登录");
                });

        // 3. 处理
        user.setPhone(dto.getPhone());
        user.setUsername(dto.getPhone());
        user.setWxNickname(dto.getNickname());
        user.setWxAvatarUrl(dto.getAvatarUrl());
        sysUserRepository.save(user);

        Customer templateCustomer = customerService.createTemplateCustomer(
                dto.getCustomerName(),
                dto.getPhone(),
                dto.getAlias(),
                dto.getGender(),
                dto.getAge(),
                user.getId(),
                dto.getAddressId(),
                dto.getAddressDetail()
        );

        log.info("客户注册成功（创建模板客户）: userId={}, templateCustomerId={}, phone={}",
                user.getId(), templateCustomer.getId(), dto.getPhone());

        // 4. 返回
        LoginVO response = new LoginVO();
        response.setUserInfo(buildBaseUserInfo(user));
        response.setMessage("客户信息已保存，请选择商户进行绑定");
        return response;
    }

    /**
     * 客户扫码绑定商户
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginVO bindMerchant(BindMerchantDTO dto) {
        // 1. 获取数据
        Merchant merchant = merchantService.findByInviteCode(dto.getInviteCode())
                .orElseThrow(() -> new NotFoundException(BusinessCode.MERCHANT_NOT_FOUND));

        Long userId = AppSessionContext.getUserId();
        SysUser user = sysUserRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(BusinessCode.USER_NOT_FOUND));

        Customer templateCustomer = customerService.findTemplateByUserId(user.getId())
                .orElseThrow(() -> new BusinessException(BusinessCode.CUSTOMER_NOT_FOUND, "请先完成客户注册"));

        // 2. 处理
        Optional<Customer> existingCustomer = customerService
                .findUnregisteredCustomerByPhoneAndMerchantId(user.getPhone(), merchant.getId());

        Customer formalCustomer;
        if (existingCustomer.isPresent()) {
            formalCustomer = existingCustomer.get();
            customerService.bindCustomerToUser(formalCustomer.getId(), userId);
        } else {
            formalCustomer = customerService.createFormalCustomerFromTemplate(templateCustomer, merchant.getId());
        }

        log.info("客户绑定商户成功: userId={}, customerId={}, merchantId={}, templateCustomerId={}",
                user.getId(), formalCustomer.getId(), merchant.getId(), templateCustomer.getId());

        // 3. 返回
        return generateLoginResponse(user, merchant.getId(), formalCustomer.getId());
    }

    /**
     * 切换身份
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginVO switchIdentity(SwitchIdentityDTO dto) {
        // 1. 获取数据
        SysUser user = sysUserRepository.findById(AppSessionContext.getUserId())
                .orElseThrow(() -> new NotFoundException(BusinessCode.USER_NOT_FOUND));

        // 2. 校验并处理
        if (dto.getIdentityType() == IdentityType.MERCHANT_OWNER) {
            if (dto.getMerchantId() == null) {
                throw new BusinessException(BusinessCode.INVALID_PARAMETER);
            }

            Merchant merchant = merchantService.findById(dto.getMerchantId())
                    .orElseThrow(() -> new NotFoundException(BusinessCode.MERCHANT_NOT_FOUND));

            if (!merchant.getOwnerUserId().equals(user.getId())) {
                throw new BusinessException(BusinessCode.UNAUTHORIZED_OPERATION);
            }

            log.info("切换到商户身份: userId={}, merchantId={}", user.getId(), dto.getMerchantId());
            return generateLoginResponse(user, dto.getMerchantId(), null);

        } else if (dto.getIdentityType() == IdentityType.CUSTOMER) {
            if (dto.getCustomerId() == null) {
                throw new BusinessException(BusinessCode.INVALID_PARAMETER);
            }

            Customer customer = customerService.findById(dto.getCustomerId())
                    .orElseThrow(() -> new NotFoundException(BusinessCode.CUSTOMER_NOT_FOUND));

            if (!customer.getUserId().equals(user.getId())) {
                throw new BusinessException(BusinessCode.UNAUTHORIZED_OPERATION);
            }

            setLastSelectedCustomerId(user.getId(), dto.getCustomerId());
            log.info("切换到客户身份: userId={}, customerId={}, merchantId={}",
                    user.getId(), dto.getCustomerId(), customer.getMerchantId());

            return generateLoginResponse(user, customer.getMerchantId(), dto.getCustomerId());

        } else {
            throw new BusinessException(BusinessCode.INVALID_PARAMETER);
        }
    }

    /**
     * 登出
     */
    public void logout(String token) {
        if (StrUtil.isNotBlank(token)) {
            String cleanToken = token.startsWith(TOKEN_PREFIX) ? token.substring(TOKEN_PREFIX.length()) : token;
            tokenUtil.removeToken(cleanToken);
            log.info("用户登出成功");
        }
    }

    /**
     * 获取当前用户信息
     */
    public UserInfoVO getCurrentUser(String token) {
        if (StrUtil.isBlank(token)) {
            throw new UnauthorizedException();
        }

        String cleanToken = token.startsWith(TOKEN_PREFIX) ? token.substring(TOKEN_PREFIX.length()) : token;
        UserInfoVO userInfo = tokenUtil.getUserInfo(cleanToken);

        if (userInfo == null) {
            throw new UnauthorizedException();
        }

        return userInfo;
    }

    /**
     * 获取用户的所有身份
     */
    public UserIdentitiesVO getUserIdentities() {
        // 1. 获取数据
        SysUser user = sysUserRepository.findById(AppSessionContext.getUserId())
                .orElseThrow(() -> new NotFoundException(BusinessCode.USER_NOT_FOUND));

        UserIdentitiesVO response = new UserIdentitiesVO();
        response.setUserId(user.getId());

        // 2. 处理
        IdentityType identityType = AppSessionContext.getIdentityType();
        if (Objects.equals(identityType, IdentityType.MERCHANT_OWNER)) {
            List<Merchant> merchants = merchantService.findByOwnerUserId(user.getId());
            response.setMerchants(merchants);
            log.info("获取商户身份列表: userId={}, merchantCount={}", user.getId(), merchants.size());
        } else if (Objects.equals(identityType,  IdentityType.CUSTOMER)) {
            List<Customer> customers = customerService.findFormalByUserId(user.getId());
            response.setCustomers(customerService.toVOListWithMerchantName(customers));
            log.info("获取客户身份列表: userId={}, customerCount={}", user.getId(), customers.size());
        }

        return response;
    }

    // ==================== 校验方法 ====================

    /**
     * 校验微信登录
     */
    private String validateWechatLogin(String code) {
        JSONObject sessionData = wechatUtil.code2Session(code);
        String openid = sessionData.getStr("openid");

        if (StrUtil.isBlank(openid)) {
            log.error("微信登录失败: 未获取到openid");
            throw new BusinessException(BusinessCode.WECHAT_LOGIN_FAILED, "微信登录失败");
        }

        return openid;
    }

    /**
     * 校验密码登录
     */
    private SysUser validatePasswordLogin(PasswordLoginDTO dto) {
        SysUser user = sysUserRepository.findByPhoneAndStatus(dto.getPhone(), Status.ACTIVE)
                .orElseThrow(() -> new BusinessException(BusinessCode.USER_CREDENTIALS_INVALID));

        if (StrUtil.isBlank(user.getPassword())) {
            log.warn("用户未设置密码: userId={}", user.getId());
            throw new BusinessException(BusinessCode.USER_CREDENTIALS_INVALID, "该账号未设置密码，请使用微信登录");
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            log.warn("密码错误: userId={}", user.getId());
            throw new BusinessException(BusinessCode.USER_CREDENTIALS_INVALID);
        }

        if (user.getStatus() != Status.ACTIVE) {
            log.warn("用户已被禁用: userId={}", user.getId());
            throw new BusinessException(BusinessCode.USER_DISABLED);
        }

        return user;
    }

    /**
     * 校验商户注册
     */
    private void validateMerchantRegister(MerchantRegisterDTO dto) {
        if (sysUserRepository.existsByPhoneAndStatus(dto.getPhone(), Status.ACTIVE)) {
            throw new BusinessException(BusinessCode.PHONE_ALREADY_BOUND);
        }
        if (sysUserRepository.existsByUsernameAndStatus(dto.getUsername(), Status.ACTIVE)) {
            throw new BusinessException(BusinessCode.USERNAME_ALREADY_USED);
        }
    }

    /**
     * 校验客户注册
     */
    private void validateCustomerRegister(CustomerRegisterDTO dto) {
        Optional<SysUser> existingUserByPhone = sysUserRepository.findByPhoneAndStatus(dto.getPhone(), Status.ACTIVE);
        if (existingUserByPhone.isPresent() && !existingUserByPhone.get().getWxOpenid().equals(dto.getOpenid())) {
            log.warn("手机号已被其他微信账号绑定: phone={}", dto.getPhone());
            throw new BusinessException(BusinessCode.PHONE_ALREADY_BOUND);
        }

        Optional<Customer> existingTemplate = customerService.findTemplateByUserId(
                existingUserByPhone.map(SysUser::getId).orElse(null));
        if (existingTemplate.isPresent()) {
            log.warn("用户已有模板客户: userId={}", existingUserByPhone.get().getId());
            throw new BusinessException(BusinessCode.CUSTOMER_ALREADY_REGISTERED);
        }
    }

    // ==================== 处理方法 ====================

    /**
     * 创建新用户
     */
    private SysUser createNewUser(String openid) {
        SysUser user = new SysUser();
        user.setWxOpenid(openid);
        user.setRole(UserRole.USER);
        user.setStatus(Status.ACTIVE);
        user = sysUserRepository.save(user);
        log.info("创建新用户成功: userId={}, openid={}", user.getId(), openid);
        return user;
    }

    /**
     * 处理商户登录
     */
    private LoginVO handleMerchantLogin(SysUser user) {
        List<Merchant> merchants = merchantService.findByOwnerUserId(user.getId());

        if (merchants.isEmpty()) {
            log.info("商户登录需要注册: userId={}", user.getId());
            LoginVO response = new LoginVO();
            response.setUserInfo(buildBaseUserInfo(user));
            response.setNeedRegister(true);
            response.setRegisterType(IdentityType.MERCHANT_OWNER);
            response.setMessage("请完成商户注册");
            return response;
        } else if (merchants.size() == 1) {
            Merchant merchant = merchants.get(0);
            log.info("商户登录直接选中: userId={}, merchantId={}", user.getId(), merchant.getId());
            return generateLoginResponse(user, merchant.getId(), null);
        } else {
            log.info("商户登录返回列表: userId={}, merchantCount={}", user.getId(), merchants.size());
            LoginVO response = new LoginVO();
            response.setUserInfo(buildBaseUserInfo(user));
            response.setMerchants(merchants);
            response.setRegisterType(IdentityType.MERCHANT_OWNER);
            response.setMessage("请选择商户");
            return response;
        }
    }

    /**
     * 处理客户登录
     */
    private LoginVO handleCustomerLogin(SysUser user) {
        List<Customer> customers = customerService.findFormalByUserId(user.getId());

        if (customers.isEmpty()) {
            log.info("客户登录需要注册: userId={}", user.getId());
            LoginVO response = new LoginVO();
            response.setUserInfo(buildBaseUserInfo(user));
            response.setNeedRegister(true);
            response.setRegisterType(IdentityType.CUSTOMER);
            response.setMessage("请完成客户注册");
            return response;
        } else if (customers.size() == 1) {
            Customer customer = customers.get(0);
            log.info("客户登录直接选中: userId={}, customerId={}", user.getId(), customer.getId());
            return generateLoginResponse(user, customer.getMerchantId(), customer.getId());
        } else {
            Long lastSelectedCustomerId = getLastSelectedCustomerId(user.getId());

            if (lastSelectedCustomerId != null &&
                    customers.stream().anyMatch(c -> c.getId().equals(lastSelectedCustomerId))) {
                Customer customer = customerService.findById(lastSelectedCustomerId)
                        .orElseThrow(() -> new NotFoundException(BusinessCode.CUSTOMER_NOT_FOUND));
                log.info("客户登录默认选中上次: userId={}, customerId={}", user.getId(), lastSelectedCustomerId);
                return generateLoginResponse(user, customer.getMerchantId(), lastSelectedCustomerId);
            } else {
                log.info("客户登录返回列表: userId={}, customerCount={}", user.getId(), customers.size());
                LoginVO response = new LoginVO();
                response.setUserInfo(buildBaseUserInfo(user));
                response.setCustomers(customerService.toVOListWithMerchantName(customers));
                response.setRegisterType(IdentityType.CUSTOMER);
                response.setMessage("请选择客户");
                return response;
            }
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 获取用户上次选中的客户ID
     */
    private Long getLastSelectedCustomerId(Long userId) {
        String redisKey = REDIS_KEY_LAST_CUSTOMER + userId;
        Long customerId = redisTemplate.opsForValue().get(redisKey);
        if (customerId != null) {
            log.info("从Redis获取上次选中的客户: userId={}, customerId={}", userId, customerId);
        }
        return customerId;
    }

    /**
     * 设置用户上次选中的客户ID
     */
    private void setLastSelectedCustomerId(Long userId, Long customerId) {
        String redisKey = REDIS_KEY_LAST_CUSTOMER + userId;
        redisTemplate.opsForValue().set(redisKey, customerId, LAST_CUSTOMER_EXPIRE_DAYS, TimeUnit.DAYS);
    }

    /**
     * 构建基础用户信息
     */
    private UserInfoVO buildBaseUserInfo(SysUser user) {
        UserInfoVO userInfo = new UserInfoVO();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setPhone(user.getPhone());
        userInfo.setRole(user.getRole().getValue());
        userInfo.setRoleDesc(user.getRole().getDescription());
        userInfo.setWxNickname(user.getWxNickname());
        userInfo.setWxAvatarUrl(user.getWxAvatarUrl());
        return userInfo;
    }

    /**
     * 生成登录响应（不带身份信息）
     */
    private LoginVO generateLoginResponse(SysUser user) {
        UserInfoVO userInfo = buildBaseUserInfo(user);
        String token = tokenUtil.generateToken(userInfo);
        return LoginVO.success(token, userInfo, tokenUtil.getExpireTime());
    }

    /**
     * 生成登录响应（带身份信息）
     */
    private LoginVO generateLoginResponse(SysUser user, Long merchantId, Long customerId) {
        UserInfoVO userInfo = buildBaseUserInfo(user);
        userInfo.setMerchantId(merchantId);
        userInfo.setCustomerId(customerId);

        if (merchantId != null && customerId == null) {
            userInfo.setIdentityType(IdentityType.MERCHANT_OWNER);
        } else if (customerId != null) {
            userInfo.setIdentityType(IdentityType.CUSTOMER);
        }

        String token = tokenUtil.generateToken(userInfo);
        return LoginVO.success(token, userInfo, tokenUtil.getExpireTime());
    }
}