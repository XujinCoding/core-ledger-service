package com.coreledger.entity;

import com.coreledger.config.converter.IdentityConverter;
import com.coreledger.enums.Identity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 用户-商户关系实体
 *
 * <p>管理用户在不同商户中的身份关系</p>
 *
 * @author Core Ledger Team
 * @since 2.0.0
 */
@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "user_merchant_relation", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_merchant_role", columnNames = {"user_id", "merchant_id", "identity"})
})
public class UserMerchantRelation extends BaseEntity {

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 商户ID
     */
    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    /**
     * 身份：0=商户老板, 1=员工, 2=客户
     */
    @Column(name = "identity", nullable = false)
    @Convert(converter = IdentityConverter.class)
    private Identity identity;

    /**
     * 绑定时间
     */
    @Column(name = "bind_time", nullable = false)
    private LocalDateTime bindTime;
}
