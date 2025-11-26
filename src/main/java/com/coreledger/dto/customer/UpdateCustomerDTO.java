package com.coreledger.dto.customer;

import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 更新客户DTO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
public class UpdateCustomerDTO {

    /** 客户姓名 */
    @Length(max = 50, message = "姓名长度不能超过50")
    private String name;

    /** 手机号 */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 别名/昵称 */
    @Length(max = 50, message = "别名长度不能超过50")
    private String alias;

    /** 性别 (0=未知, 1=男, 2=女) */
    private Integer gender;

    /** 年龄 */
    private Integer age;

    /** 地址ID */
    private Long addressId;

    /** 详细地址 */
    @Length(max = 255, message = "详细地址长度不能超过255")
    private String addressDetail;

    /** 备注 */
    @Length(max = 255, message = "备注长度不能超过255")
    private String memo;
}
