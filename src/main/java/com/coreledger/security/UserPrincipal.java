package com.coreledger.security;

import com.coreledger.enums.IdentityType;
import com.coreledger.vo.auth.CurrentUserIdentityInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * 用户主体对象
 * 实现 Spring Security 的 UserDetails 接口，封装用户详细信息
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    /**
     * customer.id
     *
     */
    @Schema(description = "客户标识", example = "1")
    private Long customerId;

    private String token;
    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1")
    private Long userId;


    /**
     * customer.name / merchant.name 根据身份确定
     */
    @Schema(description = "名称", example = "admin")
    private String name;

    /**
     * customer.phone / merchant.phone 根据身份确定
     */
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    /**
     * customer.code/ merchant.code 根据身份确定
     */
    @Schema(description = "编码", example = "xxxxxxxxxxxxxx")
    private String code;

    /**
     * customer.addressId/ merchant.addressId 根据身份确定
     */
    @Schema(description = "关联地址标识", example = "10001")
    private Long addressId;

    /**
     * customer.addressDetail/ merchant.addressDetail 根据身份确定
     */
    @Schema(description = "详细地址", example = "xxxxxx村x号")
    private String addressDetail;

    /**
     * 商户标识, 如果没有商户标识, 标识客户当前没有选择任何商户
     *
     */
    @Schema(description = "商户标识", example = "1")
    private Long merchantId;

    /**
     * 身份类型：MERCHANT_OWNER 或 CUSTOMER
     */
    @Schema(description = "身份类型", example = "MERCHANT_OWNER")
    private IdentityType identityType;

    /**
     * 是否已选择身份（用于临时Token限制）
     */
    public boolean hasSelectedIdentity() {
        return identityType != null &&
                (merchantId != null || customerId != null);
    }

    /**
     * 是否是商户身份
     */
    public boolean isMerchantOwner() {
        return IdentityType.MERCHANT_OWNER.equals(identityType);
    }

    /**
     * 是否是客户身份
     */
    public boolean isCustomer() {
        return IdentityType.CUSTOMER.equals(identityType);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (identityType == null) {
            return Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_TEMP_USER")
            );
        }
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + identityType.name())
        );
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return String.valueOf(userId);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
