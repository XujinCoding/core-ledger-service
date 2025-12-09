package com.coreledger.vo.session;

import com.coreledger.enums.IdentityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 会话信息对象
 * 存储当前用户的会话信息，包括用户、商户、客户等详细信息
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionInfo {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 商户ID
     */
    private Long merchantId;

    /**
     * 客户ID
     */
    private Long customerId;

    /**
     * token
     */
    private String token;

    /**
     * 当前身份类型
     * MERCHANT_OWNER 或 CUSTOMER
     */
    private IdentityType identityType;

    @Override
    public String toString() {
        return "SessionInfo{" +
                "userId=" + userId +
                ", merchantId=" + merchantId +
                ", customerId=" + customerId +
                ", token='" + token + '\'' +
                ", identityType=" + identityType +
                '}';
    }
}
