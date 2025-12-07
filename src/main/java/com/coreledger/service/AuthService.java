package com.coreledger.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.coreledger.dto.auth.BindMerchantDTO;
import com.coreledger.dto.auth.CustomerRegisterDTO;
import com.coreledger.dto.auth.MerchantRegisterDTO;
import com.coreledger.dto.auth.PasswordLoginDTO;
import com.coreledger.dto.auth.SwitchIdentityDTO;
import com.coreledger.dto.auth.WechatLoginDTO;
import com.coreledger.entity.Customer;
import com.coreledger.entity.Merchant;
import com.coreledger.entity.SysUser;
import com.coreledger.enums.BusinessCode;
import com.coreledger.enums.IdentityType;
import com.coreledger.enums.Status;
import com.coreledger.enums.UserRole;
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

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
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
    private final MerchantService merchantService;
    private final CustomerService customerService;
    private final RedisTemplate<String, Long> redisTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 微信小程序登录
     * 根据选择的身份类型进行不同的处理：
     * - 商户登录：查询用户拥有的商户，根据数量返回不同响应
     * - 客户登录：查询用户是客户的关系，根据数量返回不同响应
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

        log.info("微信登录: openid={}, identityType={}", openid, dto.getIdentityType());

        // 2. 查询用户是否存在
        Optional<SysUser> existingUser = sysUserRepository.findByWxOpenidAndStatus(openid, Status.ACTIVE);

        SysUser user = existingUser.orElseGet(() -> createNewUser(dto, openid));

        // 3. 根据身份类型处理
        if (dto.getIdentityType() == IdentityType.MERCHANT_OWNER) {
            return handleMerchantLogin(user);
        } else if (dto.getIdentityType() == IdentityType.CUSTOMER) {
            return handleCustomerLogin(user);
        } else {
            throw new BusinessException(BusinessCode.INVALID_PARAMETER, "无效的身份类型");
        }
    }

    /**
     * 创建新用户
     */
    private SysUser createNewUser(WechatLoginDTO dto, String openid) {
        SysUser user = new SysUser();
        user.setPhone(null);
        user.setWxOpenid(openid);
        user.setRole(UserRole.USER);
        user.setStatus(Status.ACTIVE);
        user.setWxNickname(dto.getNickname());

        if (StrUtil.isNotBlank(dto.getAvatarUrl())) {
            user.setWxAvatarUrl(dto.getAvatarUrl());
        }

        user = sysUserRepository.save(user);
        log.info("创建新用户成功: userId={}, openid={}", user.getId(), openid);
        return user;
    }

    /**
     * 处理商户登录
     */
    private LoginVO handleMerchantLogin(SysUser user) {
        // 1. 查询用户拥有的商户
        List<Merchant> merchants = merchantService.findByOwnerUserId(user.getId());

        if (merchants.isEmpty()) {
            // 没有商户：需要注册
            LoginVO response = new LoginVO();
            response.setUserInfo(buildBaseUserInfo(user));
            response.setNeedRegister(true);
            response.setRegisterType(IdentityType.MERCHANT_OWNER);
            response.setMessage("请完成商户注册");
            log.info("商户登录需要注册: userId={}", user.getId());
            return response;
        } else if (merchants.size() == 1) {
            // 唯一商户：直接选中
            Merchant merchant = merchants.get(0);
            log.info("商户登录直接选中: userId={}, merchantId={}", user.getId(), merchant.getId());
            return generateLoginResponse(user, merchant.getId(), null);
        } else {
            // 多个商户：返回列表让用户选择
            LoginVO response = new LoginVO();
            response.setUserInfo(buildBaseUserInfo(user));
            response.setMerchants(merchants);
            response.setMessage("请选择商户");
            log.info("商户登录返回列表: userId={}, merchantCount={}", user.getId(), merchants.size());
            return response;
        }
    }

    /**
     * 处理客户登录
     */
    private LoginVO handleCustomerLogin(SysUser user) {
        // 1. 查询用户是客户的所有关系
        List<Customer> customers = customerService.findFormalByUserId(user.getId());

        if (customers.isEmpty()) {
            // 没有客户：需要注册
            LoginVO response = new LoginVO();
            response.setUserInfo(buildBaseUserInfo(user));
            response.setNeedRegister(true);
            response.setRegisterType(IdentityType.CUSTOMER);
            response.setMessage("请完成客户注册");
            log.info("客户登录需要注册: userId={}", user.getId());
            return response;
        } else if (customers.size() == 1) {
            // 唯一客户：直接选中
            Customer customer = customers.get(0);
            log.info("客户登录直接选中: userId={}, customerId={}", user.getId(), customer.getId());
            return generateLoginResponse(user, customer.getMerchantId(), customer.getId());
        } else {
            // 多个客户：检查是否有上次选中的记录
            Long lastSelectedCustomerId = getLastSelectedCustomerId(user.getId());

            if (lastSelectedCustomerId != null && customers.stream().anyMatch(c -> c.getId().equals(lastSelectedCustomerId))) {
                // 有上次记录且仍然有效：默认选中
                Customer customer = customerService.findById(lastSelectedCustomerId)
                        .orElseThrow(() -> new NotFoundException(BusinessCode.CUSTOMER_NOT_FOUND));
                log.info("客户登录默认选中上次: userId={}, customerId={}", user.getId(), lastSelectedCustomerId);
                return generateLoginResponse(user, customer.getMerchantId(), lastSelectedCustomerId);
            } else {
                // 没有上次记录或记录无效：返回列表让用户选择
                LoginVO response = new LoginVO();
                response.setUserInfo(buildBaseUserInfo(user));
                response.setCustomers(customers);
                response.setMessage("请选择客户");
                log.info("客户登录返回列表: userId={}, customerCount={}", user.getId(), customers.size());
                return response;
            }
        }
    }

    /**
     * 获取用户上次选中的客户 ID
     * 从 Redis 中获取，key 格式: last_customer_{userId}
     */
    private Long getLastSelectedCustomerId(Long userId) {
        String redisKey = "last_customer_" + userId;
        Long customerId = redisTemplate.opsForValue().get(redisKey);
        if (customerId != null) {
            log.info("从 Redis 获取上次选中的客户: userId={}, customerId={}", userId, customerId);
        }
        return customerId;
    }

    /**
     * 构建用户信息（不包含身份信息）
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
     * 商户微信注册
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginVO merchantWechatRegister(MerchantRegisterDTO dto) {
        String openid = dto.getOpenid();
        String phone = dto.getPhone();
        String username = dto.getUsername();
        String password = dto.getPassword();
        String merchantName = dto.getMerchantName();

        // 1. 检查手机号是否已被绑定
        if (sysUserRepository.existsByPhoneAndStatus(phone, Status.ACTIVE)) {
            throw new BusinessException(BusinessCode.PHONE_ALREADY_BOUND);
        }

        // 2. 检查username是否已被使用
        if (sysUserRepository.existsByUsernameAndStatus(username, Status.ACTIVE)) {
            throw new BusinessException(BusinessCode.USERNAME_ALREADY_USED);
        }
        SysUser user = sysUserRepository.findByWxOpenidAndStatus(openid, Status.ACTIVE)
                .orElseGet(() -> {
                    // 创建新用户
                    SysUser newUser = new SysUser();
                    newUser.setPhone(phone);
                    newUser.setWxOpenid(openid);
                    newUser.setPassword(passwordEncoder.encode(password));
                    newUser.setWxNickname(dto.getNickname());
                    newUser.setWxAvatarUrl(dto.getAvatarUrl());
                    newUser.setRole(UserRole.USER);
                    newUser.setStatus(Status.ACTIVE);
                    return sysUserRepository.save(newUser);
                });
        // 补充信息, 登录时候没有这些信息
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setUsername(phone);
        sysUserRepository.save(user);
        // 5. 创建Merchant
        Merchant merchant = merchantService.createMerchant(merchantName, user.getId());

        log.info("商户注册成功: userId={}, merchantId={}", user.getId(), merchant.getId());

        // 6. 生成Token并返回
        return generateLoginResponse(user, merchant.getId(), null);
    }

    /**
     * 客户微信注册
     * 创建模板客户（TEMPLATE 类型），用于保存用户的客户信息
     * 注意：同一个微信号可以先注册成客户，后来再注册成商户
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginVO customerWechatRegister(CustomerRegisterDTO dto) {
        String openid = dto.getOpenid();
        String phone = dto.getPhone();

        // 1. 检查手机号是否已被绑定（同一个手机号不能绑定多个微信账号）
        Optional<SysUser> existingUserByPhone = sysUserRepository.findByPhoneAndStatus(phone, Status.ACTIVE);
        if (existingUserByPhone.isPresent() && !existingUserByPhone.get().getWxOpenid().equals(openid)) {
            log.warn("手机号已被其他微信账号绑定: phone={}", phone);
            throw new BusinessException(BusinessCode.PHONE_ALREADY_BOUND);
        }

        // 2. 检查或创建User
        SysUser user = sysUserRepository.findByWxOpenidAndStatus(openid, Status.ACTIVE)
                .orElseGet(() -> {
                    // 创建新用户
                    SysUser newUser = new SysUser();
                    newUser.setPhone(phone);
                    newUser.setWxOpenid(openid);
                    newUser.setWxNickname(dto.getNickname());
                    newUser.setWxAvatarUrl(dto.getAvatarUrl());
                    newUser.setRole(UserRole.USER);
                    newUser.setStatus(Status.ACTIVE);
                    return sysUserRepository.save(newUser);
                });
        user.setPhone(phone);
        user.setUsername(phone);
        sysUserRepository.save(user);
        // 3. 检查用户是否已有模板客户
        Optional<Customer> existingTemplate = customerService.findTemplateByUserId(user.getId());
        if (existingTemplate.isPresent()) {
            log.warn("用户已有模板客户: userId={}", user.getId());
            throw new BusinessException(BusinessCode.CUSTOMER_ALREADY_REGISTERED);
        }

        // 4. 创建模板客户（TEMPLATE 类型，merchantId 为 null）
        // 客户的专属信息（地址、性别、年龄等）存储在Customer表中，不存储在SysUser表中
        Customer templateCustomer = customerService.createTemplateCustomer(
                dto.getCustomerName(),
                phone,
                dto.getAlias(),
                dto.getGender(),
                dto.getAge(),
                user.getId(),
                dto.getAddressId(),
                dto.getAddressDetail()
        );

        log.info("客户注册成功（创建模板客户）: userId={}, templateCustomerId={}, phone={}",
                user.getId(), templateCustomer.getId(), phone);

        // 5. 返回成功响应（不返回 Token，因为还没有绑定商户）
        LoginVO response = new LoginVO();
        response.setUserInfo(buildBaseUserInfo(user));
        response.setMessage("客户信息已保存，请选择商户进行绑定");
        return response;
    }

    /**
     * 生成登录响应
     *
     * @param user 用户实体
     * @return 登录响应
     */
    private LoginVO generateLoginResponse(SysUser user) {
        // 构建用户信息
        UserInfoVO userInfo = buildBaseUserInfo(user);

        // 生成 Token
        String token = tokenUtil.generateToken(userInfo);

        // 返回登录响应
        return LoginVO.success(token, userInfo, tokenUtil.getExpireTime());
    }

    /**
     * 生成登录响应（修改版本）
     */
    private LoginVO generateLoginResponse(SysUser user, Long merchantId, Long customerId) {
        // 构建用户信息
        UserInfoVO userInfo = buildBaseUserInfo(user);
        userInfo.setMerchantId(merchantId);
        userInfo.setCustomerId(customerId);

        // 根据 merchantId 和 customerId 判断身份类型
        if (merchantId != null && customerId == null) {
            userInfo.setIdentityType(IdentityType.MERCHANT_OWNER);
        } else if (customerId != null) {
            userInfo.setIdentityType(IdentityType.CUSTOMER);
        }

        // 生成 Token
        String token = tokenUtil.generateToken(userInfo);

        // 返回登录响应
        return LoginVO.success(token, userInfo, tokenUtil.getExpireTime());
    }

    /**
     * 客户扫码绑定商户
     * 从模板客户复制数据创建正式客户（FORMAL 类型）
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginVO bindMerchant(BindMerchantDTO dto) {
        // 1. 根据邀请码查询商户
        Merchant merchant = merchantService.findByInviteCode(dto.getInviteCode())
                .orElseThrow(() -> new NotFoundException(BusinessCode.MERCHANT_NOT_FOUND));

        // 2. 从 Token 获取当前用户
        Long userId = AppSessionContext.getUserId();
        SysUser user = sysUserRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(BusinessCode.USER_NOT_FOUND));

        // 3. 检查用户是否有模板客户
        Customer templateCustomer = customerService.findTemplateByUserId(user.getId())
                .orElseThrow(() -> new BusinessException(BusinessCode.CUSTOMER_NOT_FOUND, "请先完成客户注册"));
        Customer formalCustomer = customerService.findUnregisteredCustomerByPhoneAndMerchantId(user.getPhone(), merchant.getId()).orElse(null);
        if(Objects.nonNull(formalCustomer)){
            customerService.bindCustomerToUser(formalCustomer.getId(),userId);
        }else{
            //4. 从模板复制数据创建正式客户（FORMAL 类型）
            formalCustomer = customerService.createFormalCustomerFromTemplate(
                    templateCustomer,
                    merchant.getId()
            );
        }


        log.info("客户绑定商户成功: userId={}, customerId={}, merchantId={}, templateCustomerId={}",
                user.getId(), formalCustomer.getId(), merchant.getId(), templateCustomer.getId());

        // 5. 生成登录响应
        return generateLoginResponse(user, merchant.getId(), formalCustomer.getId());
    }

    /**
     * 切换身份
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginVO switchIdentity(SwitchIdentityDTO dto) {
        // 1. 从 Token 获取当前用户
        UserInfoVO userInfo = getCurrentUser(dto.getToken());
        SysUser user = sysUserRepository.findById(userInfo.getId())
                .orElseThrow(() -> new NotFoundException(BusinessCode.USER_NOT_FOUND));

        // 2. 根据身份类型验证权限
        if (dto.getIdentityType() == IdentityType.MERCHANT_OWNER) {
            // 验证用户是否拥有该商户
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
            // 验证用户是否是该客户
            if (dto.getCustomerId() == null) {
                throw new BusinessException(BusinessCode.INVALID_PARAMETER);
            }
            Customer customer = customerService.findById(dto.getCustomerId())
                    .orElseThrow(() -> new NotFoundException(BusinessCode.CUSTOMER_NOT_FOUND));

            if (!customer.getUserId().equals(user.getId())) {
                throw new BusinessException(BusinessCode.UNAUTHORIZED_OPERATION);
            }

            // 记录上次选中的客户 ID 到 Redis
            String redisKey = "last_customer_" + user.getId();
            redisTemplate.opsForValue().set(redisKey, dto.getCustomerId(), 30, TimeUnit.DAYS);
            log.info("记录上次选中的客户: userId={}, customerId={}, redisKey={}", user.getId(), dto.getCustomerId(), redisKey);

            log.info("切换到客户身份: userId={}, customerId={}, merchantId={}", user.getId(), dto.getCustomerId(), customer.getMerchantId());
            return generateLoginResponse(user, customer.getMerchantId(), dto.getCustomerId());

        } else {
            throw new BusinessException(BusinessCode.INVALID_PARAMETER);
        }
    }

    /**
     * 获取用户的所有身份
     * 根据当前身份类型返回可切换的身份列表：
     * - 如果当前是商户身份，返回该用户拥有的所有商户
     * - 如果当前是客户身份，返回该用户是客户的所有关系
     */
    public UserIdentitiesVO getUserIdentities(String token) {
        // 1. 从 Token 获取用户信息
        UserInfoVO userInfo = getCurrentUser(token);
        SysUser user = sysUserRepository.findById(userInfo.getId())
                .orElseThrow(() -> new NotFoundException(BusinessCode.USER_NOT_FOUND));

        // 2. 构建返回对象
        UserIdentitiesVO response = new UserIdentitiesVO();
        response.setUserId(user.getId());

        // 3. 根据当前身份类型返回可切换的身份列表
        if (userInfo.getIdentityType() == IdentityType.MERCHANT_OWNER) {
            // 商户登录：返回该用户拥有的所有商户
            List<Merchant> merchants = merchantService.findByOwnerUserId(user.getId());
            response.setMerchants(merchants);
            log.info("获取商户身份列表: userId={}, merchantCount={}", user.getId(), merchants.size());

        } else if (userInfo.getIdentityType() == IdentityType.CUSTOMER) {
            // 客户登录：返回该用户是客户的所有关系
            List<Customer> customers = customerService.findFormalByUserId(user.getId());
            response.setCustomers(customers);
            log.info("获取客户身份列表: userId={}, customerCount={}", user.getId(), customers.size());

        } else {
            log.warn("未知的身份类型: userId={}, identityType={}", user.getId(), userInfo.getIdentityType());
        }

        return response;
    }
}
