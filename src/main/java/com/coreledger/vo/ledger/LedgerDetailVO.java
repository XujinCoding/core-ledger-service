package com.coreledger.vo.ledger;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 账单详情VO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Schema(description = "账单详情响应")
public class LedgerDetailVO {

    @Schema(description = "账本ID")
    private Long id;

    @Schema(description = "客户ID")
    private Long customerId;

    @Schema(description = "客户姓名")
    private String customerName;

    @Schema(description = "客户手机号")
    private String customerPhone;

    @Schema(description = "客户地址")
    private String customerAddress;

    @Schema(description = "客户详细地址")
    private String customerAddressDetail;

    @Schema(description = "应收总金额")
    private BigDecimal totalAmount;

    @Schema(description = "实收金额")
    private BigDecimal paidAmount;

    @Schema(description = "优惠金额")
    private BigDecimal discountAmount;

    @Schema(description = "剩余应收金额")
    private BigDecimal remainingAmount;

    @Schema(description = "账本状态: 1=进行中, 2=部分缴费, 3=已结清, 4=赊账中, 5=已关闭")
    private Integer ledgerStatus;

    @Schema(description = "状态描述")
    private String ledgerStatusDesc;

    @Schema(description = "账单明细")
    private List<LedgerItemVO> items;

    @Schema(description = "支付记录")
    private List<PaymentRecordVO> paymentRecords;

    @Schema(description = "备注")
    private String memo;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createInstant;

    @Schema(description = "修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime modifyInstant;

    @Schema(description = "版本号")
    private Integer version;

    /**
     * 账单明细VO
     */
    @Data
    @Schema(description = "账单明细")
    public static class LedgerItemVO {

        @Schema(description = "明细ID")
        private Long id;

        @Schema(description = "SKU ID")
        private Long skuId;

        @Schema(description = "SKU名称")
        private String skuName;

        @Schema(description = "属性值映射")
        private Map<String, String> attrValueMap;

        @Schema(description = "价格")
        private BigDecimal price;

        @Schema(description = "数量")
        private Integer quantity;

        @Schema(description = "小计")
        private BigDecimal subtotal;
    }

    /**
     * 支付记录VO
     */
    @Data
    @Schema(description = "支付记录")
    public static class PaymentRecordVO {

        @Schema(description = "支付记录ID")
        private Long id;

        @Schema(description = "支付金额")
        private BigDecimal amount;

        @Schema(description = "支付方式: 1=现金, 2=微信, 3=支付宝, 4=银行转账")
        private Integer paymentMethod;

        @Schema(description = "支付方式描述")
        private String paymentMethodDesc;

        @Schema(description = "备注")
        private String memo;

        @Schema(description = "支付时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createInstant;
    }
}
