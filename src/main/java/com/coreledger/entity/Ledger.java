package com.coreledger.entity;

import com.coreledger.config.converter.LedgerStatusConverter;
import com.coreledger.enums.LedgerStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 账本主表实体
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "ledger")
@FilterDef(name = "merchantFilter", parameters = @ParamDef(name = "merchantId", type = Long.class))
@Filter(name = "merchantFilter", condition = "merchant_id = :merchantId")
public class Ledger extends BaseEntity {

    /** 所属商户ID，用于数据隔离 */
    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    /** 客户ID */
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    /** 应收总金额 */
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    /** 实收金额 */
    @Column(name = "paid_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    /** 抹零/优惠金额 */
    @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    /** 账本状态 */
    @Column(name = "ledger_status", nullable = false)
    @Convert(converter = LedgerStatusConverter.class)
    private LedgerStatus ledgerStatus = LedgerStatus.IN_PROGRESS;

    /** 账本明细列表 */
    @OneToMany(mappedBy = "ledgerId", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<LedgerItem> items = new ArrayList<>();

    /** 支付记录列表 */
    @OneToMany(mappedBy = "ledgerId", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<PaymentRecord> paymentRecords = new ArrayList<>();
}
