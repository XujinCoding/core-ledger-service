package com.coreledger.security;

import com.coreledger.utils.SecurityUtils;
import org.springframework.stereotype.Component;

/**
 * 自定义权限评估服务
 * 用于 @PreAuthorize 注解中的权限判断
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Component("authz")
public class CustomAuthorizationService {

    /**
     * 检查是否已选择身份
     * 用于临时Token限制，未选择身份时只能访问特定接口
     *
     * @return true 如果已选择身份，false 否则
     */
    public boolean hasSelectedIdentity() {
        return SecurityUtils.hasSelectedIdentity();
    }

    /**
     * 检查是否是商户身份
     *
     * @return true 如果是商户身份且已设置商户ID，false 否则
     */
    public boolean isMerchantOwner() {
        return SecurityUtils.isMerchantOwner();
    }

    /**
     * 检查是否是客户身份
     *
     * @return true 如果是客户身份且已设置客户ID，false 否则
     */
    public boolean isCustomer() {
        return SecurityUtils.isCustomer();
    }

    /**
     * 检查是否拥有指定身份类型
     *
     * @param identityType 身份类型名称（如 "MERCHANT_OWNER", "CUSTOMER"）
     * @return true 如果拥有指定身份类型，false 否则
     */
    public boolean hasIdentity(String identityType) {
        try {
            UserPrincipal user = SecurityUtils.getCurrentUser();
            return user.getIdentityType() != null &&
                    identityType.equals(user.getIdentityType().name());
        } catch (Exception e) {
            return false;
        }
    }
}
