package com.coreledger.dto.customer;

import com.coreledger.enums.CustomerType;
import com.coreledger.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 客户更新DTO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Schema(description = "客户更新请求")
public class CustomerUpdateDTO {

    /**
     * 客户姓名
     */
    @Size(max = 50, message = "客户姓名长度不能超过50个字符")
    @Schema(description = "客户姓名", example = "张三")
    private String name;

    /**
     * 手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    /**
     * 别名/昵称
     */
    @Size(max = 50, message = "别名长度不能超过50个字符")
    @Schema(description = "别名/昵称", example = "小张")
    private String alias;

    /**
     * 性别
     */
    @Schema(description = "性别: 1=男, 2=女, 0=未知", example = "1")
    private Gender gender;

    /**
     * 年龄
     */
    @Schema(description = "年龄", example = "30")
    private Integer age;

    /**
     * 客户类型
     */
    @Schema(description = "客户类型: 1=活跃, 2=潜在, 3=流失", example = "1")
    private CustomerType customerType;


    /** 关联地址ID */
    @Schema(name = "关联地址ID")
    private Long addressId;

    /** 详细地址 */
    @Schema(name = "详细地址", example = "村")
    private String addressDetail;

    /** 备注 */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    @Schema(description = "备注", example = "VIP客户")
    private String remark;
}
