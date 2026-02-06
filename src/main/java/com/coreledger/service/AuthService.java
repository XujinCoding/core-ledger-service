package com.coreledger.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.coreledger.common.mapper.sysuser.SysUserConvert;
import com.coreledger.dto.auth.*;
import com.coreledger.entity.Customer;
import com.coreledger.entity.Merchant;
import com.coreledger.entity.SysUser;
import com.coreledger.entity.UserMerchantRelation;
import com.coreledger.enums.*;
import com.coreledger.exception.BusinessException;
import com.coreledger.exception.NotFoundException;
import com.coreledger.exception.UnauthorizedException;
import com.coreledger.repository.SysUserRepository;
import com.coreledger.utils.SecurityUtils;
import com.coreledger.utils.TokenUtil;
import com.coreledger.utils.WechatUtil;
import com.coreledger.vo.auth.LoginVO;
import com.coreledger.vo.merchant.MerchantVO;
import com.coreledger.vo.auth.UserIdentitiesVO;
import com.coreledger.vo.auth.CurrentUserIdentityInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.StringRedisTemplate;
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
    private final SmsService smsService;
    private final SysUserConvert sysUserConvert;
    private final StringRedisTemplate stringRedisTemplate;
    private final UserMerchantRelationService userMerchantRelationService;
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
                .orElseGet(() -> getOrCreateUser(openid));

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
        log.info("密码登录成功: userId={}, userName={}", user.getId(), user.getUsername());

        // 2. 返回
        return generateLoginResponse(user);
    }

    /**
     * 商户微信注册
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginVO merchantWechatRegister(MerchantRegisterDTO dto) {
        // 1. 校验验证码
        smsService.verifySmsCode(dto.getPhone(), dto.getSmsCode(), SmsScene.MERCHANT_REGISTER);

        // 2. 校验
        validateMerchantRegister(dto);

        // 3. 获取数据
        String openid = validateWechatLogin(dto.getCode());
        SysUser user = getOrCreateUser(openid);

        // 4. 更新用户个人信息
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setNickname(dto.getNickname());
        user.setAvatarUrl(dto.getAvatarUrl());
        user.setAddressId(dto.getAddressId());
        user.setAddressDetail(dto.getAddressDetail());
        sysUserRepository.save(user);

        // 5. 创建商户
        MerchantVO merchantVO = merchantService.createMerchant(dto, user.getId());

        // 6. 创建用户-商户关系（商户老板）
        userMerchantRelationService.createRelation(user.getId(), merchantVO.getId(), Identity.OWNER);

        log.info("商户注册成功: userId={}, merchantId={}, phone={}", user.getId(), merchantVO.getId(), dto.getPhone());

        // 7. 返回
        return generateLoginResponse(user, merchantVO.getId(), null);
    }

    /**
     * 客户微信注册
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginVO customerWechatRegister(CustomerRegisterDTO dto) {
        // 1. 校验验证码
        smsService.verifySmsCode(dto.getPhone(), dto.getSmsCode(), SmsScene.CUSTOMER_REGISTER);

        String openId = validateWechatLogin(dto.getCode());

        // 2. 创建/更新用户个人信息
        SysUser user = getOrCreateUser(openId);
        user.setName(dto.getCustomerName());
        user.setNickname(dto.getNickname());
        user.setAvatarUrl(dto.getAvatarUrl());
        user.setPhone(dto.getPhone());
        user.setGender(dto.getGender());
        user.setAge(dto.getAge());
        user.setAddressId(dto.getAddressId());
        user.setAddressDetail(dto.getAddressDetail());
        sysUserRepository.save(user);

        // 3. 如果有邀请码，直接绑定商户
        if (Objects.nonNull(dto.getInviteCode())) {
            Merchant merchant = merchantService.findByInviteCode(dto.getInviteCode())
                    .orElseThrow(() -> new NotFoundException(BusinessCode.MERCHANT_NOT_FOUND));

            // 查找是否有phone相同的customer（商户预先创建的）
            Optional<Customer> existingCustomer = customerService
                    .findUnregisteredCustomerByPhoneAndMerchantId(dto.getPhone(), merchant.getId());

            Customer customer;
            if (existingCustomer.isPresent()) {
                // 回写user_id
                customer = existingCustomer.get();
                customer.setUserId(user.getId());
                customerService.save(customer);
                log.info("客户注册成功（绑定已存在客户）: userId={}, customerId={}, merchantId={}, phone={}",
                        user.getId(), customer.getId(), merchant.getId(), dto.getPhone());
            } else {
                // 创建新customer
                customer = customerService.createCustomerFromSysUser(user, merchant.getId(), dto);
                log.info("客户注册成功（创建新客户）: userId={}, customerId={}, merchantId={}, phone={}",
                        user.getId(), customer.getId(), merchant.getId(), dto.getPhone());
            }

            // 创建用户-商户关系
            userMerchantRelationService.createRelation(user.getId(), merchant.getId(), Identity.CUSTOMER);

            return generateLoginResponse(user, merchant.getId(), customer.getId());
        }

        // 4. 没有邀请码，只创建SysUser，返回用户信息（类似原来的模板客户）
        log.info("客户注册成功（仅创建用户）: userId={}, phone={}", user.getId(), dto.getPhone());

        // 返回登录响应（使用SysUser信息，保持接口兼容性）
        return generateLoginResponseForUnboundUser(user);

    }


    /**
     * 客户扫码绑定商户
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginVO bindMerchant(BindMerchantDTO dto) {
        // 1. 获取数据
        Merchant merchant = merchantService.findByInviteCode(dto.getInviteCode())
                .orElseThrow(() -> new NotFoundException(BusinessCode.MERCHANT_NOT_FOUND));

        Long userId = SecurityUtils.getCurrentUserId();
        assert userId != null;
        SysUser user = sysUserRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(BusinessCode.USER_NOT_FOUND));

        // 2. 检查是否已绑定该商户（通过UserMerchantRelation）
        boolean alreadyBound = userMerchantRelationService.checkRelationExists(
                userId, merchant.getId(), Identity.CUSTOMER);
        if (alreadyBound) {
            throw new BusinessException(BusinessCode.CUSTOMER_BIND_EXISTS, "您已绑定该商户");
        }

        // 3. 查找是否有phone相同的customer（商户预先创建的）
        Optional<Customer> existingCustomer = customerService
                .findUnregisteredCustomerByPhoneAndMerchantId(user.getPhone(), merchant.getId());

        Customer customer;
        if (existingCustomer.isPresent()) {
            // 回写user_id
            customer = existingCustomer.get();
            customer.setUserId(userId);
            customerService.save(customer);
            log.info("客户绑定商户成功（绑定已存在客户）: userId={}, customerId={}, merchantId={}, phone={}",
                    userId, customer.getId(), merchant.getId(), user.getPhone());
        } else {
            // 从SysUser创建新customer
            CustomerRegisterDTO customerRegisterDTO = new CustomerRegisterDTO();
            customerRegisterDTO.setCustomerName(user.getName());
            customerRegisterDTO.setPhone(user.getPhone());
            customerRegisterDTO.setGender(user.getGender());
            customerRegisterDTO.setAge(user.getAge());
            customerRegisterDTO.setAddressId(user.getAddressId());
            customerRegisterDTO.setAddressDetail(user.getAddressDetail());
            customer = customerService.createCustomerFromSysUser(user, merchant.getId(), customerRegisterDTO);
            log.info("客户绑定商户成功（创建新客户）: userId={}, customerId={}, merchantId={}, phone={}",
                    userId, customer.getId(), merchant.getId(), user.getPhone());
        }

        // 4. 创建用户-商户关系
        userMerchantRelationService.createRelation(userId, merchant.getId(), Identity.CUSTOMER);

        // 5. 返回
        return generateLoginResponse(user, merchant.getId(), customer.getId());
    }

    /**
     * 切换身份
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginVO switchIdentity(SwitchIdentityDTO dto) {
        // 1. 获取数据
        SysUser user = sysUserRepository.findById(SecurityUtils.getCurrentUserId())
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
    public CurrentUserIdentityInfo getCurrentUser() {
        CurrentUserIdentityInfo userInfo = sysUserConvert.toInfo(SecurityUtils.getCurrentUser());


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
        SysUser user = sysUserRepository.findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new NotFoundException(BusinessCode.USER_NOT_FOUND));

        UserIdentitiesVO response = new UserIdentitiesVO();
        response.setUserId(user.getId());

        // 2. 处理
        IdentityType identityType = SecurityUtils.getCurrentIdentityType();
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
        SysUser user = sysUserRepository.findByUsernameAndStatus(dto.getUserName(), Status.ACTIVE)
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
        if (sysUserRepository.existsByUsernameAndStatus(dto.getUsername(), Status.ACTIVE)) {
            throw new BusinessException(BusinessCode.USERNAME_ALREADY_USED);
        }
    }

    // ==================== 处理方法 ====================

    private @NotNull SysUser getOrCreateUser(String openid) {
        // 创建新用户
        return sysUserRepository.findByWxOpenidAndStatus(openid, Status.ACTIVE)
                .orElseGet(() -> {
                    // 创建新用户
                    SysUser newUser = new SysUser();
                    newUser.setWxOpenid(openid);
                    newUser.setStatus(Status.ACTIVE);
                    return sysUserRepository.save(newUser);
                });
    }
    /**
     * 处理商户登录
     */
    private LoginVO handleMerchantLogin(SysUser user) {
        // 通过UserMerchantRelation查询用户的商户列表
        List<Long> merchantIds = userMerchantRelationService.findMerchantIdsByUserIdAndIdentity(
                user.getId(), List.of(Identity.OWNER,Identity.EMPLOYEE));

        if (merchantIds.isEmpty()) {
            log.info("商户登录需要注册: userId={}", user.getId());
            LoginVO response = new LoginVO();
            response.setUserInfo(buildBaseUserInfo(user.getId(), IdentityType.MERCHANT_OWNER));
            response.setNeedRegister(true);
            response.setRegisterType(IdentityType.MERCHANT_OWNER);
            response.setMessage("请完成商户注册");
            return response;
        } else if (merchantIds.size() == 1) {
            Long merchantId = merchantIds.get(0);
            log.info("商户登录直接选中: userId={}, merchantId={}", user.getId(), merchantId);
            return generateLoginResponse(user, merchantId, null);
        } else {
            log.info("商户登录返回列表: userId={}, merchantCount={}", user.getId(), merchantIds.size());
            // 查询商户详情
            List<Merchant> merchants = merchantService.findByIds(merchantIds);
            
            // 生成临时token（不含merchantId），用于选择商户
            CurrentUserIdentityInfo userInfo = buildBaseUserInfo(user.getId(), IdentityType.MERCHANT_OWNER);
            String tempToken = tokenUtil.generateTempToken(userInfo);
            
            LoginVO response = new LoginVO();
            response.setToken(tempToken);
            response.setExpireTime(tokenUtil.getTempExpireTime());
            response.setUserInfo(userInfo);
            response.setMerchants(merchants);
            response.setNeedSelect(true);
            response.setMessage("请选择商户");
            return response;
        }
    }

    /**
     * 处理客户登录
     */
    private LoginVO handleCustomerLogin(SysUser user) {
        // 通过UserMerchantRelation查询用户的客户关系
        List<UserMerchantRelation> relations = userMerchantRelationService.findByUserIdAndIdentity(
                user.getId(), List.of(Identity.CUSTOMER));

        if (relations.isEmpty()) {
            // 没有任何客户关系，返回用户信息（类似未绑定商户的状态）
            log.info("客户登录（未绑定商户）: userId={}", user.getId());
            return generateLoginResponseForUnboundUser(user);
        }

        // 查询对应的Customer记录
        List<Customer> customers = relations.stream()
                .map(relation -> customerService.findByUserIdAndMerchantId(user.getId(), relation.getMerchantId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        if (customers.isEmpty()) {
            // 有关系但没有customer记录（数据不一致），返回未绑定状态
            log.warn("客户登录数据不一致: userId={}, 有关系但无customer记录", user.getId());
            return generateLoginResponseForUnboundUser(user);
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
                // 生成临时token（不含customerId/merchantId），用于选择客户
                CurrentUserIdentityInfo userInfo = buildBaseUserInfo(user.getId(), IdentityType.CUSTOMER);
                String tempToken = tokenUtil.generateTempToken(userInfo);
                
                LoginVO response = new LoginVO();
                response.setToken(tempToken);
                response.setExpireTime(tokenUtil.getTempExpireTime());
                response.setUserInfo(userInfo);
                response.setCustomers(customerService.toVOListWithMerchantName(customers));
                response.setNeedSelect(true);
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
        String customerId = stringRedisTemplate.opsForValue().get(redisKey);
        if (customerId != null) {
            log.info("从Redis获取上次选中的客户: userId={}, customerId={}", userId, customerId);
            return Long.valueOf(customerId);
        }
        return null;
    }

    /**
     * 设置用户上次选中的客户ID
     */
    private void setLastSelectedCustomerId(Long userId, Long customerId) {
        String redisKey = REDIS_KEY_LAST_CUSTOMER + userId;
        stringRedisTemplate.opsForValue().set(redisKey, customerId.toString(), LAST_CUSTOMER_EXPIRE_DAYS, TimeUnit.DAYS);
    }

    /**
     * 构建基础用户信息
     */
    private CurrentUserIdentityInfo buildBaseUserInfo(Long userId, IdentityType identityType) {
        CurrentUserIdentityInfo userInfo = new CurrentUserIdentityInfo();
        userInfo.setUserId(userId);
        userInfo.setIdentityType(identityType);
        return userInfo;
    }

    /**
     * 生成登录响应（不带身份信息）
     */
    private LoginVO generateLoginResponse(SysUser user) {
        CurrentUserIdentityInfo userInfo = buildBaseUserInfo(user.getId(),IdentityType.MERCHANT_OWNER);
        String token = tokenUtil.generateToken(userInfo);
        return LoginVO.success(token, userInfo, tokenUtil.getExpireTime());
    }

    /**
     * 生成登录响应（带身份信息）
     */
    private LoginVO generateLoginResponse(SysUser user, Long merchantId, Long customerId) {
        CurrentUserIdentityInfo userInfo = null;
        String token = null;
        if (merchantId != null && customerId == null) {
            // 商户登录
            userInfo = buildBaseUserInfo(user.getId(),IdentityType.MERCHANT_OWNER);
            userInfo.setMerchantId(merchantId);
            Merchant merchant = merchantService.findById(merchantId)
                    .orElseThrow(() -> new NotFoundException(BusinessCode.MERCHANT_NOT_FOUND));
            userInfo.setName(merchant.getName());
            userInfo.setCode(merchant.getCode());
            userInfo.setPhone(merchant.getPhone());
            userInfo.setAddressId(merchant.getAddressId());
            userInfo.setAddressDetail(merchant.getAddressDetail());
            token = tokenUtil.generateToken(userInfo);
        } else if (merchantId != null) {
            // 正式客户登录
            userInfo = buildBaseUserInfo(user.getId(),IdentityType.CUSTOMER);
            Customer customer = customerService.findById(customerId)
                    .orElseThrow(() -> new NotFoundException(BusinessCode.CUSTOMER_NOT_FOUND));
            userInfo.setCustomerId(customerId);
            userInfo.setMerchantId(merchantId);
            userInfo.setName(customer.getName());
            userInfo.setCode(customer.getCode());
            userInfo.setPhone(customer.getPhone());
            userInfo.setAddressId(customer.getAddressId());
            userInfo.setAddressDetail(customer.getAddressDetail());
            token = tokenUtil.generateToken(userInfo);
        }else if (customerId != null) {
            // 客户登录
            userInfo = buildBaseUserInfo(user.getId(),IdentityType.CUSTOMER);
            userInfo.setIdentityType(IdentityType.CUSTOMER);
            Customer customer = customerService.findById(customerId)
                    .orElseThrow(() -> new NotFoundException(BusinessCode.CUSTOMER_NOT_FOUND));
            userInfo.setCustomerId(customerId);
            userInfo.setName(customer.getName());
            userInfo.setCode(customer.getCode());
            userInfo.setPhone(customer.getPhone());
            userInfo.setAddressId(customer.getAddressId());
            userInfo.setAddressDetail(customer.getAddressDetail());
            token = tokenUtil.generateToken(userInfo);
        }
        return LoginVO.success(token, userInfo, tokenUtil.getExpireTime());
    }

    /**
     * 为未绑定商户的用户生成登录响应
     * 使用SysUser信息，保持与原模板客户接口兼容
     */
    private LoginVO generateLoginResponseForUnboundUser(SysUser user) {
        CurrentUserIdentityInfo userInfo = buildBaseUserInfo(user.getId(), IdentityType.CUSTOMER);
        userInfo.setUserId(user.getId());  // 使用userId作为id
        userInfo.setName(user.getName());
        userInfo.setPhone(user.getPhone());
        userInfo.setAddressId(user.getAddressId());
        userInfo.setAddressDetail(user.getAddressDetail());

        String token = tokenUtil.generateToken(userInfo);
        return LoginVO.success(token, userInfo, tokenUtil.getExpireTime());
    }
}
