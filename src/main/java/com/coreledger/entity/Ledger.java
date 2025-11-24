package com.coreledger.entity;

import com.coreledger.config.converter.LedgerStatusConverter;
import com.coreledger.enums.LedgerStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 账本实体
 *
 * <p>对应数据库表: ledger</p>
 * <p>账本状态流转规则请参考: {@link LedgerStatus}</p>
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "ledger")
public class Ledger extends BaseEntity {

    /** 客户ID */
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    /**
     * 应收总金额
     * <p>所有明细项的金额总和</p>
     */
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    /**
     * 实收金额
     * <p>已收到的款项总和</p>
     */
    @Column(name = "paid_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    /**
     * 优惠金额
     * <p>抹零或优惠的金额</p>
     */
    @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    /**
     * 账本状态
     * <ul>
     *   <li>1 = IN_PROGRESS (进行中)</li>
     *   <li>2 = PARTIAL (部分缴费)</li>
     *   <li>3 = CLEARED (已结清)</li>
     *   <li>4 = ON_CREDIT (赊账中)</li>
     *   <li>5 = CLOSED (已关闭)</li>
     * </ul>
     */
    @Column(name = "ledger_status", nullable = false)
    @Convert(converter = LedgerStatusConverter.class)
    private LedgerStatus ledgerStatus = LedgerStatus.IN_PROGRESS;

    /**
     * 账本明细列表
     * <p>级联操作：删除账本时同时删除明细</p>
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "ledger_id")
    private List<LedgerItem> items = new ArrayList<>();

    /**
     * 计算剩余应收金额
     *
     * @return 剩余应收金额 = 应收总金额 - 实收金额 - 优惠金额
     */
    public BigDecimal getRemainingAmount() {
        return totalAmount.subtract(paidAmount).subtract(discountAmount);
    }

    /**
     * 添加明细项
     *
     * @param item 明细项
     */
    public void addItem(LedgerItem item) {
        items.add(item);
        item.setLedgerId(this.getId());
    }

    /**
     * 移除明细项
     *
     * @param item 明细项
     */
    public void removeItem(LedgerItem item) {
        items.remove(item);
        item.setLedgerId(null);
    }

    /**
     * 重新计算应收总金额
     * <p>根据所有明细项的小计金额求和</p>
     */
    public void recalculateTotalAmount() {
        this.totalAmount = items.stream()
                .map(LedgerItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
