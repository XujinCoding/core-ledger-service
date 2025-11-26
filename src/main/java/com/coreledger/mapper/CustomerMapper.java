package com.coreledger.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 客户 MyBatis Mapper
 * 用于复杂查询和统计
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Mapper
public interface CustomerMapper {

    /**
     * 批量查询客户欠款统计
     *
     * @param customerIds 客户ID列表
     * @return 欠款统计列表
     */
    List<CustomerDebtSummary> batchQueryDebtSummary(@Param("customerIds") List<Long> customerIds);

    /**
     * 查询单个客户的账单统计
     *
     * @param customerId 客户ID
     * @return 账单统计
     */
    CustomerLedgerSummary queryLedgerSummary(@Param("customerId") Long customerId);

    /**
     * 客户欠款统计
     */
    class CustomerDebtSummary {
        private Long customerId;
        private BigDecimal debtAmount;        // 欠款总额(进行中+部分缴费)
        private BigDecimal creditAmount;      // 赊账总额
        private Integer activeLedgerCount;    // 活跃账单数

        // Getter/Setter
        public Long getCustomerId() {
            return customerId;
        }

        public void setCustomerId(Long customerId) {
            this.customerId = customerId;
        }

        public BigDecimal getDebtAmount() {
            return debtAmount;
        }

        public void setDebtAmount(BigDecimal debtAmount) {
            this.debtAmount = debtAmount;
        }

        public BigDecimal getCreditAmount() {
            return creditAmount;
        }

        public void setCreditAmount(BigDecimal creditAmount) {
            this.creditAmount = creditAmount;
        }

        public Integer getActiveLedgerCount() {
            return activeLedgerCount;
        }

        public void setActiveLedgerCount(Integer activeLedgerCount) {
            this.activeLedgerCount = activeLedgerCount;
        }
    }

    /**
     * 客户账单统计
     */
    class CustomerLedgerSummary {
        private Integer inProgressCount;
        private BigDecimal inProgressAmount;
        private Integer partialCount;
        private BigDecimal partialAmount;
        private BigDecimal partialPaidAmount;
        private Integer creditCount;
        private BigDecimal creditAmount;
        private Integer clearedCount;
        private BigDecimal totalDebt;
        private BigDecimal totalCredit;

        // Getter/Setter
        public Integer getInProgressCount() {
            return inProgressCount;
        }

        public void setInProgressCount(Integer inProgressCount) {
            this.inProgressCount = inProgressCount;
        }

        public BigDecimal getInProgressAmount() {
            return inProgressAmount;
        }

        public void setInProgressAmount(BigDecimal inProgressAmount) {
            this.inProgressAmount = inProgressAmount;
        }

        public Integer getPartialCount() {
            return partialCount;
        }

        public void setPartialCount(Integer partialCount) {
            this.partialCount = partialCount;
        }

        public BigDecimal getPartialAmount() {
            return partialAmount;
        }

        public void setPartialAmount(BigDecimal partialAmount) {
            this.partialAmount = partialAmount;
        }

        public BigDecimal getPartialPaidAmount() {
            return partialPaidAmount;
        }

        public void setPartialPaidAmount(BigDecimal partialPaidAmount) {
            this.partialPaidAmount = partialPaidAmount;
        }

        public Integer getCreditCount() {
            return creditCount;
        }

        public void setCreditCount(Integer creditCount) {
            this.creditCount = creditCount;
        }

        public BigDecimal getCreditAmount() {
            return creditAmount;
        }

        public void setCreditAmount(BigDecimal creditAmount) {
            this.creditAmount = creditAmount;
        }

        public Integer getClearedCount() {
            return clearedCount;
        }

        public void setClearedCount(Integer clearedCount) {
            this.clearedCount = clearedCount;
        }

        public BigDecimal getTotalDebt() {
            return totalDebt;
        }

        public void setTotalDebt(BigDecimal totalDebt) {
            this.totalDebt = totalDebt;
        }

        public BigDecimal getTotalCredit() {
            return totalCredit;
        }

        public void setTotalCredit(BigDecimal totalCredit) {
            this.totalCredit = totalCredit;
        }
    }
}
