package com.coreledger.entity;

import com.coreledger.config.converter.CustomerTypeConverter;
import com.coreledger.config.converter.GenderConverter;
import com.coreledger.config.converter.RegisterStatusConverter;
import com.coreledger.enums.CustomerType;
import com.coreledger.enums.Gender;
import com.coreledger.enums.RegisterStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

/**
 * 客户信息实体
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "customer", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_merchant", columnNames = {"user_id", "merchant_id"}),
        @UniqueConstraint(name = "uk_customer_no_merchant", columnNames = {"customer_no", "merchant_id"})
})
@FilterDef(name = "merchantFilter", parameters = @ParamDef(name = "merchantId", type = Long.class))
@Filter(name = "merchantFilter", condition = "merchant_id = :merchantId")
public class Customer extends BaseEntity {

    /** 客户编号 */
    @Column(name = "customer_no", nullable = false, length = 32)
    private String customerNo;

    /** 关联的User ID，允许为空（商户手动创建时） */
    @Column(name = "user_id")
    private Long userId;

    /** 所属商户ID */
    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    /** 是否已注册：0=未注册, 1=已注册 */
    @Column(name = "is_registered", nullable = false)
    @Convert(converter = RegisterStatusConverter.class)
    private RegisterStatus isRegistered = RegisterStatus.UNREGISTERED;

    /** 客户姓名 */
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    /** 手机号 */
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    /** 别名/昵称 */
    @Column(name = "alias", length = 50)
    private String alias;

    /** 性别 */
    @Column(name = "gender", nullable = false)
    @Convert(converter = GenderConverter.class)
    private Gender gender = Gender.UNKNOWN;

    /** 年龄 */
    @Column(name = "age")
    private Integer age;

    /** 关联地址ID */
    @Column(name = "address_id", nullable = false)
    private Long addressId;

    /** 详细地址 */
    @Column(name = "address_detail", length = 255)
    private String addressDetail;

    /** 客户类型：TEMPLATE=模板客户，FORMAL=正式客户 */
    @Column(name = "customer_type", nullable = false)
    @Convert(converter = CustomerTypeConverter.class)
    private CustomerType customerType = CustomerType.FORMAL;
}
