package com.coreledger.service;

import com.coreledger.repository.CustomerRepository;
import com.coreledger.repository.LedgerRepository;
import com.coreledger.repository.ProductRepository;
import com.coreledger.vo.merchant.MerchantOverviewVO;
import com.coreledger.vo.merchant.MerchantStatsVO;
import com.coreledger.vo.merchant.TodayStatsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 商户统计服务
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MerchantStatsService {

    private final LedgerRepository ledgerRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    /**
     * 获取商户本月统计数据
     *
     * @param merchantId 商户ID
     * @return 商户统计VO
     */
    public MerchantStatsVO getMerchantStats(Long merchantId) {
        log.info("获取商户统计数据，商户ID: {}", merchantId);

        // 计算本月时间范围
        LocalDate today = LocalDate.now();
        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime monthEnd = today.plusMonths(1).withDayOfMonth(1).atStartOfDay();

        // 查询统计数据
        BigDecimal monthlySales = ledgerRepository.sumMonthlySales(merchantId, monthStart, monthEnd);
        BigDecimal pendingAmount = ledgerRepository.sumPendingAmount(merchantId);
        Integer monthlyOrders = ledgerRepository.countMonthlyOrders(merchantId, monthStart, monthEnd);

        return MerchantStatsVO.builder()
                .monthlySales(monthlySales != null ? monthlySales : BigDecimal.ZERO)
                .pendingAmount(pendingAmount != null ? pendingAmount : BigDecimal.ZERO)
                .monthlyOrders(monthlyOrders != null ? monthlyOrders : 0)
                .build();
    }

    /**
     * 获取商户今日汇总数据
     *
     * @param merchantId 商户ID
     * @return 今日统计VO
     */
    public TodayStatsVO getTodayStats(Long merchantId) {
        log.info("获取商户今日汇总数据，商户ID: {}", merchantId);

        // 计算今日时间范围
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);

        // 查询统计数据
        BigDecimal sales = ledgerRepository.sumTodaySales(merchantId, todayStart, todayEnd);
        BigDecimal payment = ledgerRepository.sumTodayPayment(merchantId, todayStart, todayEnd);
        BigDecimal debt = ledgerRepository.sumTodayDebt(merchantId, todayStart, todayEnd);
        Integer orders = ledgerRepository.countTodayOrders(merchantId, todayStart, todayEnd);

        return TodayStatsVO.builder()
                .sales(sales != null ? sales : BigDecimal.ZERO)
                .payment(payment != null ? payment : BigDecimal.ZERO)
                .debt(debt != null ? debt : BigDecimal.ZERO)
                .orders(orders != null ? orders : 0)
                .build();
    }

    /**
     * 获取商户概览统计（客户数、商品数、账单数）
     *
     * @param merchantId 商户ID
     * @return 概览统计VO
     */
    public MerchantOverviewVO getMerchantOverview(Long merchantId) {
        log.info("获取商户概览统计，商户ID: {}", merchantId);

        long customerCount = customerRepository.countByMerchantId(merchantId);
        long productCount = productRepository.countByMerchantId(merchantId);
        long ledgerCount = ledgerRepository.countByMerchantId(merchantId);

        return MerchantOverviewVO.builder()
                .customerCount((int) customerCount)
                .productCount((int) productCount)
                .ledgerCount((int) ledgerCount)
                .build();
    }
}
