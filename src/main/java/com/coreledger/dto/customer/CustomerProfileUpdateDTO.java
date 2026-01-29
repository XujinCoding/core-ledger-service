package com.coreledger.dto.customer;

import com.coreledger.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 客户个人信息更新DTO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Schema(description = "客户个人信息更新请求")
public class CustomerProfileUpdateDTO {

    /**
     * 客户姓名
     */
    @Size(max = 50, message = "客户姓名长度不能超过50个字符")
    @Schema(description = "客户姓名", example = "张三")
    private String name;

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
     * 关联地址ID
     */
    @Schema(description = "关联地址ID")
    private Long addressId;

    /**
     * 详细地址
     */
    @Schema(description = "详细地址", example = "XX街道XX号")
    private String addressDetail;

    /**
     * 客户头像URL
     */
    @Size(max = 500, message = "头像URL长度不能超过500个字符")
    @Schema(description = "客户头像URL")
    private String avatarUrl;

    /**
     * 手机号（修改时需要验证码）
     */
    @Size(max = 20, message = "手机号长度不能超过20个字符")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    /**
     * 短信验证码（修改手机号时必填）
     */
    @Size(max = 10, message = "验证码长度不能超过10个字符")
    @Schema(description = "短信验证码")
    private String smsCode;
}
