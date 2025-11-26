package com.coreledger.vo.ledger;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账单列表VO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Schema(description = "账单列表响应")
public class LedgerListVO {

    @Schema(description = "账本ID")
    private Long id;

    @Schema(description = "客户ID")
    private Long customerId;

    @Schema(description = "客户姓名")
    private String customerName;

    @Schema(description = "客户手机号")
    private String customerPhone;

    @Schema(description = "应收总金额")
    private BigDecimal totalAmount;

    @Schema(description = "实收金额")
    private BigDecimal paidAmount;

    @Schema(description = "剩余应收金额")
    private BigDecimal remainingAmount;

    @Schema(description = "账本状态: 1=进行中, 2=部分缴费, 3=已结清, 4=赊账中, 5=已关闭")
    private Integer ledgerStatus;

    @Schema(description = "状态描述")
    private String ledgerStatusDesc;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createInstant;
}
