package com.coreledger.entity;

import com.coreledger.config.converter.GenderConverter;
import com.coreledger.enums.Gender;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 客户实体
 *
 * <p>对应数据库表: customer</p>
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "customer")
public class Customer extends BaseEntity {

    /** 客户姓名 */
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    /**
     * 手机号
     * <p>全局唯一</p>
     */
    @Column(name = "phone", nullable = false, unique = true, length = 20)
    private String phone;

    /** 别名/昵称 */
    @Column(name = "alias", length = 50)
    private String alias;

    /**
     * 性别
     * <ul>
     *   <li>0 = UNKNOWN (未知)</li>
     *   <li>1 = MALE (男)</li>
     *   <li>2 = FEMALE (女)</li>
     * </ul>
     */
    @Column(name = "gender", nullable = false)
    @Convert(converter = GenderConverter.class)
    private Gender gender = Gender.UNKNOWN;

    /** 年龄 */
    @Column(name = "age")
    private Integer age;

    /**
     * 地址ID
     * <p>关联 sys_address 表，必须是村级 (level=5)</p>
     */
    @Column(name = "address_id", nullable = false)
    private Long addressId;

    /** 详细地址 */
    @Column(name = "address_detail", length = 255)
    private String addressDetail;
}
