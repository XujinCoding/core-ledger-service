package com.coreledger.entity;

import com.coreledger.config.converter.CustomerTypeConverter;
import com.coreledger.config.converter.GenderConverter;
import com.coreledger.enums.CustomerType;
import com.coreledger.enums.Gender;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
@Table(name = "customer")
public class Customer extends BaseEntity {

    /** 客户姓名 */
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    /** 手机号 */
    @Column(name = "phone", nullable = false, length = 20, unique = true)
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

    /** 客户类型 */
    @Column(name = "customer_type", nullable = false)
    @Convert(converter = CustomerTypeConverter.class)
    private CustomerType customerType = CustomerType.ACTIVE;
}
