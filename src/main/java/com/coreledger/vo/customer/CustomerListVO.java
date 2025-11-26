package com.coreledger.vo.customer;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 客户列表VO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Schema(description = "客户列表响应")
public class CustomerListVO {

    @Schema(description = "客户ID")
    private Long id;

    @Schema(description = "客户姓名")
    private String name;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "别名/昵称")
    private String alias;

    @Schema(description = "性别: 0=未知, 1=男, 2=女")
    private Integer gender;

    @Schema(description = "性别描述")
    private String genderDesc;

    @Schema(description = "年龄")
    private Integer age;

    @Schema(description = "完整地址")
    private String fullAddress;

    @Schema(description = "客户类型: 0=潜在客户, 1=活跃客户")
    private Integer customerType;

    @Schema(description = "客户类型描述")
    private String customerTypeDesc;

    @Schema(description = "欠款总额(进行中+部分缴费)")
    private BigDecimal debtAmount;

    @Schema(description = "赊账总额")
    private BigDecimal creditAmount;

    @Schema(description = "活跃账单数")
    private Integer activeLedgerCount;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createInstant;
}
