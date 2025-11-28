package com.coreledger.dto.customer;

import com.coreledger.enums.CustomerType;
import com.coreledger.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 客户创建DTO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Schema(description = "客户创建请求")
public class CustomerCreateDTO {

    /**
     * 客户姓名
     */
    @NotBlank(message = "客户姓名不能为空")
    @Size(max = 50, message = "客户姓名长度不能超过50个字符")
    @Schema(description = "客户姓名", example = "张三")
    private String name;

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
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
    @NotNull(message = "性别不能为空")
    @Schema(description = "性别: 1=男, 2=女, 0=未知", example = "1")
    private Gender gender;

    /**
     * 年龄
     */
    @Schema(description = "年龄", example = "30")
    private Integer age;

    /**
     * 关联地址ID（村级地址）
     */
    @NotNull(message = "关联地址ID不能为空")
    @Schema(description = "关联地址ID（必须为村级地址）", example = "1")
    private Long addressId;

    /**
     * 详细地址
     */
    @Size(max = 255, message = "详细地址长度不能超过255个字符")
    @Schema(description = "详细地址", example = "XX街道XX号")
    private String addressDetail;

    /**
     * 客户类型
     */
    @NotNull(message = "客户类型不能为空")
    @Schema(description = "客户类型: 1=活跃, 2=潜在, 3=流失", example = "1")
    private CustomerType customerType;
}
