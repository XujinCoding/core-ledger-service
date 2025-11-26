package com.coreledger.vo.customer;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 客户详情VO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Schema(description = "客户详情响应")
public class CustomerDetailVO {

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

    @Schema(description = "地址ID")
    private Long addressId;

    @Schema(description = "完整地址")
    private String fullAddress;

    @Schema(description = "详细地址")
    private String addressDetail;

    @Schema(description = "客户类型: 0=潜在客户, 1=活跃客户")
    private Integer customerType;

    @Schema(description = "客户类型描述")
    private String customerTypeDesc;

    @Schema(description = "备注")
    private String memo;

    @Schema(description = "账单统计汇总")
    private LedgerSummary ledgerSummary;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createInstant;

    @Schema(description = "修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime modifyInstant;

    /**
     * 账单统计汇总
     */
    @Data
    @Schema(description = "账单统计")
    public static class LedgerSummary {

        @Schema(description = "进行中账单数")
        private Integer inProgressCount;

        @Schema(description = "进行中金额")
        private BigDecimal inProgressAmount;

        @Schema(description = "部分缴费账单数")
        private Integer partialCount;

        @Schema(description = "部分缴费应收金额")
        private BigDecimal partialAmount;

        @Schema(description = "部分缴费已收金额")
        private BigDecimal partialPaidAmount;

        @Schema(description = "赊账账单数")
        private Integer creditCount;

        @Schema(description = "赊账金额")
        private BigDecimal creditAmount;

        @Schema(description = "已结清账单数")
        private Integer clearedCount;

        @Schema(description = "总欠款(进行中+部分缴费未付)")
        private BigDecimal totalDebt;

        @Schema(description = "总赊账")
        private BigDecimal totalCredit;
    }
}
