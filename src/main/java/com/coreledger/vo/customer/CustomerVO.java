package com.coreledger.vo.customer;

import com.coreledger.enums.CustomerType;
import com.coreledger.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 客户详情VO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Schema(description = "客户详情")
public class CustomerVO {

    /**
     * 客户ID
     */
    @Schema(description = "客户ID")
    private Long id;

    /**
     * 客户编码
     */
    @Schema(description = "客户编码")
    private String customerNo;

    /**
     * 客户姓名
     */
    @Schema(description = "客户姓名")
    private String name;

    /**
     * 手机号
     */
    @Schema(description = "手机号")
    private String phone;

    /**
     * 别名/昵称
     */
    @Schema(description = "别名/昵称")
    private String alias;

    /**
     * 性别
     */
    @Schema(description = "性别")
    private Gender gender;

    /**
     * 性别描述
     */
    @Schema(description = "性别描述")
    private String genderDesc;

    /**
     * 年龄
     */
    @Schema(description = "年龄")
    private Integer age;

    /**
     * 关联地址ID
     */
    @Schema(description = "关联地址ID")
    private Long addressId;

    /**
     * 地址完整路径
     */
    @Schema(description = "地址完整路径")
    private String addressPath;

    /**
     * 详细地址
     */
    @Schema(description = "详细地址")
    private String addressDetail;

    /**
     * 客户类型
     */
    @Schema(description = "客户类型")
    private CustomerType customerType;

    /**
     * 客户类型描述
     */
    @Schema(description = "客户类型描述")
    private String customerTypeDesc;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createInstant;

    /**
     * 修改时间
     */
    @Schema(description = "修改时间")
    private LocalDateTime modifyInstant;

    /**
     * 商户ID
     */
    @Schema(description = "商户ID")
    private Long merchantId;

    /**
     * 商户名称
     */
    @Schema(description = "商户名称")
    private String merchantName;

    /**
     * 商户编号
     */
    @Schema(description = "商户编号")
    private String merchantNo;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 客户头像URL
     */
    @Schema(description = "客户头像URL")
    private String avatarUrl;
}
