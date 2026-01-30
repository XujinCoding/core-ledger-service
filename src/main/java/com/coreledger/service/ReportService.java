package com.coreledger.service;

import com.coreledger.entity.Customer;
import com.coreledger.entity.Ledger;
import com.coreledger.entity.LedgerItem;
import com.coreledger.entity.Product;
import com.coreledger.entity.SysAddress;
import com.coreledger.enums.LedgerStatus;
import com.coreledger.enums.Status;
import com.coreledger.repository.CustomerRepository;
import com.coreledger.repository.LedgerItemRepository;
import com.coreledger.repository.LedgerRepository;
import com.coreledger.repository.ProductRepository;
import com.coreledger.repository.SysAddressRepository;
import com.coreledger.utils.specification.PredicateBuilder;
import com.coreledger.vo.report.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 报表服务
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final LedgerRepository ledgerRepository;
    private final LedgerItemRepository ledgerItemRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final SysAddressRepository sysAddressRepository;

    /**
     * 获取收入统计
     *
     * @param merchantId 商户ID
     * @param type       时间类型：year/month/day
     * @param date       日期参数
     * @return 收入统计VO
     */
    public IncomeStatsVO getIncomeStats(Long merchantId, String type, String date) {
        log.info("获取收入统计，商户ID: {}, 类型: {}, 日期: {}", merchantId, type, date);

        List<IncomeDetailItem> details = new ArrayList<>();
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal maxAmount = BigDecimal.ZERO;
        int orderCount = 0;
        Set<Long> customerIds = new HashSet<>();

        switch (type) {
            case "year":
                // 按年：从选择的年往前查5年数据
                int year = Integer.parseInt(date);
                for (int i = year - 4; i <= year; i++) {
                    LocalDateTime startTime = LocalDateTime.of(i, 1, 1, 0, 0);
                    LocalDateTime endTime = LocalDateTime.of(i + 1, 1, 1, 0, 0);
                    BigDecimal amount = sumSales(merchantId, startTime, endTime);
                    details.add(IncomeDetailItem.builder()
                            .label(i + "年")
                            .amount(amount)
                            .build());
                    totalIncome = totalIncome.add(amount);
                    if (amount.compareTo(maxAmount) > 0) {
                        maxAmount = amount;
                    }
                }
                // 统计当年数据
                LocalDateTime yearStart = LocalDateTime.of(year, 1, 1, 0, 0);
                LocalDateTime yearEnd = LocalDateTime.of(year + 1, 1, 1, 0, 0);
                orderCount = countOrders(merchantId, yearStart, yearEnd);
                customerIds = getCustomerIds(merchantId, yearStart, yearEnd);
                totalIncome = sumSales(merchantId, yearStart, yearEnd);
                break;

            case "month":
                // 按月：从选择的月往前查12个月数据
                YearMonth ym = YearMonth.parse(date);
                for (int i = 11; i >= 0; i--) {
                    YearMonth targetYm = ym.minusMonths(i);
                    LocalDateTime startTime = targetYm.atDay(1).atStartOfDay();
                    LocalDateTime endTime = targetYm.plusMonths(1).atDay(1).atStartOfDay();
                    BigDecimal amount = sumSales(merchantId, startTime, endTime);
                    details.add(IncomeDetailItem.builder()
                            .label(targetYm.getYear() + "年" + targetYm.getMonthValue() + "月")
                            .amount(amount)
                            .build());
                    totalIncome = totalIncome.add(amount);
                    if (amount.compareTo(maxAmount) > 0) {
                        maxAmount = amount;
                    }
                }
                // 统计当月数据
                LocalDateTime monthStart = ym.atDay(1).atStartOfDay();
                LocalDateTime monthEnd = ym.plusMonths(1).atDay(1).atStartOfDay();
                orderCount = countOrders(merchantId, monthStart, monthEnd);
                customerIds = getCustomerIds(merchantId, monthStart, monthEnd);
                totalIncome = sumSales(merchantId, monthStart, monthEnd);
                break;

            case "day":
                // 按日：从选择的日期往前查30天数据
                LocalDate targetDate = LocalDate.parse(date);
                for (int d = 29; d >= 0; d--) {
                    LocalDate day = targetDate.minusDays(d);
                    LocalDateTime startTime = day.atStartOfDay();
                    LocalDateTime endTime = day.atTime(LocalTime.MAX);
                    BigDecimal amount = sumSales(merchantId, startTime, endTime);
                    details.add(IncomeDetailItem.builder()
                            .label(day.getMonthValue() + "月" + day.getDayOfMonth() + "日")
                            .amount(amount)
                            .build());
                    totalIncome = totalIncome.add(amount);
                    if (amount.compareTo(maxAmount) > 0) {
                        maxAmount = amount;
                    }
                }
                // 统计当日数据
                LocalDateTime dayStart = targetDate.atStartOfDay();
                LocalDateTime dayEnd = targetDate.atTime(LocalTime.MAX);
                orderCount = countOrders(merchantId, dayStart, dayEnd);
                customerIds = getCustomerIds(merchantId, dayStart, dayEnd);
                totalIncome = sumSales(merchantId, dayStart, dayEnd);
                break;
        }

        return IncomeStatsVO.builder()
                .totalIncome(totalIncome)
                .orderCount(orderCount)
                .customerCount(customerIds.size())
                .maxAmount(maxAmount)
                .details(details)
                .build();
    }

    /**
     * 获取商品销售统计
     *
     * @param merchantId 商户ID
     * @param type       时间类型
     * @param date       日期参数
     * @return 商品销售列表
     */
    public List<ProductSalesVO> getProductSales(Long merchantId, String type, String date) {
        log.info("获取商品销售统计，商户ID: {}, 类型: {}, 日期: {}", merchantId, type, date);

        // 获取时间范围
        LocalDateTime[] timeRange = getTimeRange(type, date);
        LocalDateTime startTime = timeRange[0];
        LocalDateTime endTime = timeRange[1];

        // 查询时间范围内的账单
        List<Ledger> ledgers = findLedgers(merchantId, startTime, endTime);
        List<Long> ledgerIds = ledgers.stream().map(Ledger::getId).collect(Collectors.toList());

        if (ledgerIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 查询账单明细，按商品分组统计
        List<LedgerItem> items = ledgerItemRepository.findByLedgerIdIn(ledgerIds);
        Map<Long, BigDecimal> productAmountMap = new HashMap<>();
        Map<Long, Integer> productQuantityMap = new HashMap<>();

        for (LedgerItem item : items) {
            Long productId = item.getProductId();
            BigDecimal amount = item.getAmount() != null ? item.getAmount() : BigDecimal.ZERO;
            Integer quantity = item.getQuantity() != null ? item.getQuantity() : 0;

            productAmountMap.merge(productId, amount, BigDecimal::add);
            productQuantityMap.merge(productId, quantity, Integer::sum);
        }

        // 计算总金额
        BigDecimal totalAmount = productAmountMap.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 构建结果
        List<ProductSalesVO> result = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : productAmountMap.entrySet()) {
            Long productId = entry.getKey();
            BigDecimal amount = entry.getValue();
            Integer quantity = productQuantityMap.getOrDefault(productId, 0);

            // 获取商品名称
            String productName = "未知商品";
            Optional<Product> product = productRepository.findById(productId);
            if (product.isPresent()) {
                productName = product.get().getName();
            }

            // 计算占比
            BigDecimal percentage = BigDecimal.ZERO;
            if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
                percentage = amount.multiply(BigDecimal.valueOf(100))
                        .divide(totalAmount, 1, RoundingMode.HALF_UP);
            }

            result.add(ProductSalesVO.builder()
                    .productId(productId)
                    .productName(productName)
                    .amount(amount)
                    .quantity(quantity)
                    .percentage(percentage)
                    .build());
        }

        // 按金额降序排序，取前10
        result.sort((a, b) -> b.getAmount().compareTo(a.getAmount()));
        if (result.size() > 10) {
            // 合并其他
            BigDecimal otherAmount = BigDecimal.ZERO;
            int otherQuantity = 0;
            for (int i = 9; i < result.size(); i++) {
                otherAmount = otherAmount.add(result.get(i).getAmount());
                otherQuantity += result.get(i).getQuantity();
            }
            result = new ArrayList<>(result.subList(0, 9));
            BigDecimal otherPercentage = BigDecimal.ZERO;
            if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
                otherPercentage = otherAmount.multiply(BigDecimal.valueOf(100))
                        .divide(totalAmount, 1, RoundingMode.HALF_UP);
            }
            result.add(ProductSalesVO.builder()
                    .productId(0L)
                    .productName("其他")
                    .amount(otherAmount)
                    .quantity(otherQuantity)
                    .percentage(otherPercentage)
                    .build());
        }

        return result;
    }

    /**
     * 获取客户交易排行
     *
     * @param merchantId 商户ID
     * @param limit      返回数量限制
     * @param date       日期参数
     * @return 客户交易排行列表
     */
    public List<CustomerRankingVO> getCustomerRanking(Long merchantId, Integer limit, String date) {
        log.info("获取客户交易排行，商户ID: {}, 限制: {}, 日期: {}", merchantId, limit, date);

        // 获取时间范围（使用年度数据）
        LocalDateTime[] timeRange = getTimeRange("year", date.substring(0, 4));
        LocalDateTime startTime = timeRange[0];
        LocalDateTime endTime = timeRange[1];

        // 查询时间范围内的账单
        List<Ledger> ledgers = findLedgers(merchantId, startTime, endTime);

        // 按客户分组统计
        Map<Long, BigDecimal> customerAmountMap = new HashMap<>();
        Map<Long, Integer> customerOrderMap = new HashMap<>();

        for (Ledger ledger : ledgers) {
            Long customerId = ledger.getCustomerId();
            BigDecimal amount = ledger.getTotalAmount() != null ? ledger.getTotalAmount() : BigDecimal.ZERO;

            customerAmountMap.merge(customerId, amount, BigDecimal::add);
            customerOrderMap.merge(customerId, 1, Integer::sum);
        }

        // 构建结果
        List<CustomerRankingVO> result = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : customerAmountMap.entrySet()) {
            Long customerId = entry.getKey();
            BigDecimal amount = entry.getValue();
            Integer orderCount = customerOrderMap.getOrDefault(customerId, 0);

            // 获取客户名称
            String customerName = "未知客户";
            Optional<Customer> customer = customerRepository.findById(customerId);
            if (customer.isPresent()) {
                customerName = customer.get().getName();
                if (customerName == null || customerName.isEmpty()) {
                    customerName = customer.get().getAlias();
                }
            }

            result.add(CustomerRankingVO.builder()
                    .customerId(customerId)
                    .customerName(customerName)
                    .totalAmount(amount)
                    .orderCount(orderCount)
                    .build());
        }

        // 按金额降序排序
        result.sort((a, b) -> b.getTotalAmount().compareTo(a.getTotalAmount()));

        // 限制返回数量
        if (result.size() > limit) {
            result = result.subList(0, limit);
        }

        return result;
    }

    /**
     * 获取欠款趋势
     *
     * @param merchantId 商户ID
     * @param type       时间类型
     * @param date       日期参数
     * @return 欠款趋势VO
     */
    public DebtTrendVO getDebtTrend(Long merchantId, String type, String date) {
        log.info("获取欠款趋势，商户ID: {}, 类型: {}, 日期: {}", merchantId, type, date);

        List<DebtTrendDetailItem> details = new ArrayList<>();
        BigDecimal maxAmount = BigDecimal.ZERO;
        BigDecimal totalDebt = BigDecimal.ZERO;

        switch (type) {
            case "year":
                // 按年：从选择的年往前查5年数据
                int year = Integer.parseInt(date);
                for (int i = year - 4; i <= year; i++) {
                    LocalDateTime startTime = LocalDateTime.of(i, 1, 1, 0, 0);
                    LocalDateTime endTime = LocalDateTime.of(i + 1, 1, 1, 0, 0);
                    BigDecimal newDebt = sumNewDebt(merchantId, startTime, endTime);
                    BigDecimal paidAmount = sumPaidAmount(merchantId, startTime, endTime);
                    details.add(DebtTrendDetailItem.builder()
                            .label(i + "年")
                            .newDebt(newDebt)
                            .paidAmount(paidAmount)
                            .totalDebt(BigDecimal.ZERO)
                            .build());
                    if (newDebt.compareTo(maxAmount) > 0) maxAmount = newDebt;
                    if (paidAmount.compareTo(maxAmount) > 0) maxAmount = paidAmount;
                }
                break;

            case "month":
                // 按月：从选择的月往前查12个月数据
                YearMonth ym = YearMonth.parse(date);
                for (int i = 11; i >= 0; i--) {
                    YearMonth targetYm = ym.minusMonths(i);
                    LocalDateTime startTime = targetYm.atDay(1).atStartOfDay();
                    LocalDateTime endTime = targetYm.plusMonths(1).atDay(1).atStartOfDay();
                    BigDecimal newDebt = sumNewDebt(merchantId, startTime, endTime);
                    BigDecimal paidAmount = sumPaidAmount(merchantId, startTime, endTime);
                    details.add(DebtTrendDetailItem.builder()
                            .label(targetYm.getYear() + "年" + targetYm.getMonthValue() + "月")
                            .newDebt(newDebt)
                            .paidAmount(paidAmount)
                            .totalDebt(BigDecimal.ZERO)
                            .build());
                    if (newDebt.compareTo(maxAmount) > 0) maxAmount = newDebt;
                    if (paidAmount.compareTo(maxAmount) > 0) maxAmount = paidAmount;
                }
                break;

            case "day":
                // 按日：从选择的日期往前查30天数据
                LocalDate targetDate = LocalDate.parse(date);
                for (int d = 29; d >= 0; d--) {
                    LocalDate day = targetDate.minusDays(d);
                    LocalDateTime startTime = day.atStartOfDay();
                    LocalDateTime endTime = day.atTime(LocalTime.MAX);
                    BigDecimal newDebt = sumNewDebt(merchantId, startTime, endTime);
                    BigDecimal paidAmount = sumPaidAmount(merchantId, startTime, endTime);
                    details.add(DebtTrendDetailItem.builder()
                            .label(day.getMonthValue() + "月" + day.getDayOfMonth() + "日")
                            .newDebt(newDebt)
                            .paidAmount(paidAmount)
                            .totalDebt(BigDecimal.ZERO)
                            .build());
                    if (newDebt.compareTo(maxAmount) > 0) maxAmount = newDebt;
                    if (paidAmount.compareTo(maxAmount) > 0) maxAmount = paidAmount;
                }
                break;
        }

        // 获取当前总欠款
        totalDebt = getCurrentTotalDebt(merchantId);
        int debtCustomerCount = getDebtCustomerCount(merchantId);

        return DebtTrendVO.builder()
                .totalDebt(totalDebt)
                .debtCustomerCount(debtCustomerCount)
                .maxAmount(maxAmount)
                .details(details)
                .build();
    }

    /**
     * 获取按地址欠款分布
     *
     * @param merchantId 商户ID
     * @param date       日期参数
     * @return 地址欠款分布列表
     */
    public List<DebtByAddressVO> getDebtByAddress(Long merchantId, String date) {
        log.info("获取按地址欠款分布，商户ID: {}, 日期: {}", merchantId, date);

        // 查询所有赊账中的账单
        List<Ledger> debtLedgers = findDebtLedgers(merchantId);

        // 按客户地址分组统计
        Map<String, BigDecimal> addressAmountMap = new HashMap<>();
        Map<String, Set<Long>> addressCustomerMap = new HashMap<>();

        for (Ledger ledger : debtLedgers) {
            Long customerId = ledger.getCustomerId();
            BigDecimal debtAmount = ledger.getTotalAmount()
                    .subtract(ledger.getPaidAmount())
                    .subtract(ledger.getDiscountAmount());

            // 获取客户地址（通过addressId从地址表获取）
            String address = "未知地址";
            Optional<Customer> customer = customerRepository.findById(customerId);
            if (customer.isPresent()) {
                Long addressId = customer.get().getAddressId();
                if (addressId != null && addressId > 0) {
                    Optional<SysAddress> sysAddress = sysAddressRepository.findByIdAndStatus(addressId, Status.ACTIVE);
                    if (sysAddress.isPresent()) {
                        // 使用地址表中的名称
                        address = sysAddress.get().getName();
                        // 如果有完整路径，可以使用mergerName
                        if (sysAddress.get().getMergerName() != null && !sysAddress.get().getMergerName().isEmpty()) {
                            address = sysAddress.get().getMergerName();
                        }
                    }
                }
            }

            addressAmountMap.merge(address, debtAmount, BigDecimal::add);
            addressCustomerMap.computeIfAbsent(address, k -> new HashSet<>()).add(customerId);
        }

        // 构建结果
        List<DebtByAddressVO> result = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : addressAmountMap.entrySet()) {
            String address = entry.getKey();
            BigDecimal amount = entry.getValue();
            int customerCount = addressCustomerMap.getOrDefault(address, Collections.emptySet()).size();

            result.add(DebtByAddressVO.builder()
                    .address(address)
                    .amount(amount)
                    .customerCount(customerCount)
                    .build());
        }

        // 按金额降序排序
        result.sort((a, b) -> b.getAmount().compareTo(a.getAmount()));

        // 限制返回数量
        if (result.size() > 10) {
            result = result.subList(0, 10);
        }

        return result;
    }

    /**
     * 获取按客户欠款排行
     *
     * @param merchantId 商户ID
     * @param limit      返回数量限制
     * @param date       日期参数
     * @return 客户欠款排行列表
     */
    public List<DebtByCustomerVO> getDebtByCustomer(Long merchantId, Integer limit, String date) {
        log.info("获取按客户欠款排行，商户ID: {}, 限制: {}, 日期: {}", merchantId, limit, date);

        // 查询所有赊账中的账单
        List<Ledger> debtLedgers = findDebtLedgers(merchantId);

        // 按客户分组统计
        Map<Long, BigDecimal> customerAmountMap = new HashMap<>();
        Map<Long, Integer> customerLedgerCountMap = new HashMap<>();
        Map<Long, LocalDateTime> customerEarliestDebtMap = new HashMap<>();

        for (Ledger ledger : debtLedgers) {
            Long customerId = ledger.getCustomerId();
            BigDecimal debtAmount = ledger.getTotalAmount()
                    .subtract(ledger.getPaidAmount())
                    .subtract(ledger.getDiscountAmount());

            customerAmountMap.merge(customerId, debtAmount, BigDecimal::add);
            customerLedgerCountMap.merge(customerId, 1, Integer::sum);

            // 记录最早欠款时间
            LocalDateTime createTime = ledger.getCreateInstant();
            if (!customerEarliestDebtMap.containsKey(customerId) ||
                    createTime.isBefore(customerEarliestDebtMap.get(customerId))) {
                customerEarliestDebtMap.put(customerId, createTime);
            }
        }

        // 构建结果
        List<DebtByCustomerVO> result = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Map.Entry<Long, BigDecimal> entry : customerAmountMap.entrySet()) {
            Long customerId = entry.getKey();
            BigDecimal amount = entry.getValue();
            Integer ledgerCount = customerLedgerCountMap.getOrDefault(customerId, 0);

            // 计算逾期天数
            int overdueDays = 0;
            if (customerEarliestDebtMap.containsKey(customerId)) {
                overdueDays = (int) ChronoUnit.DAYS.between(
                        customerEarliestDebtMap.get(customerId).toLocalDate(),
                        now.toLocalDate());
            }

            // 获取客户名称
            String customerName = "未知客户";
            Optional<Customer> customer = customerRepository.findById(customerId);
            if (customer.isPresent()) {
                customerName = customer.get().getName();
                if (customerName == null || customerName.isEmpty()) {
                    customerName = customer.get().getAlias();
                }
            }

            result.add(DebtByCustomerVO.builder()
                    .customerId(customerId)
                    .customerName(customerName)
                    .amount(amount)
                    .ledgerCount(ledgerCount)
                    .overdueDays(overdueDays)
                    .build());
        }

        // 按金额降序排序
        result.sort((a, b) -> b.getAmount().compareTo(a.getAmount()));

        // 限制返回数量
        if (result.size() > limit) {
            result = result.subList(0, limit);
        }

        return result;
    }

    // ==================== 私有方法 ====================

    private BigDecimal sumSales(Long merchantId, LocalDateTime startTime, LocalDateTime endTime) {
        BigDecimal result = ledgerRepository.sumMonthlySales(merchantId, startTime, endTime);
        return result != null ? result : BigDecimal.ZERO;
    }

    private int countOrders(Long merchantId, LocalDateTime startTime, LocalDateTime endTime) {
        Integer result = ledgerRepository.countMonthlyOrders(merchantId, startTime, endTime);
        return result != null ? result : 0;
    }

    private Set<Long> getCustomerIds(Long merchantId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Ledger> ledgers = findLedgers(merchantId, startTime, endTime);
        return ledgers.stream()
                .map(Ledger::getCustomerId)
                .collect(Collectors.toSet());
    }

    private List<Ledger> findLedgers(Long merchantId, LocalDateTime startTime, LocalDateTime endTime) {
        return ledgerRepository.findAll(
                PredicateBuilder.<Ledger>and()
                        .equal("merchantId", merchantId)
                        .greaterThanOrEqualTo(true, "createInstant", startTime)
                        .lessThan(true, "createInstant", endTime)
                        .build()
        );
    }

    private List<Ledger> findDebtLedgers(Long merchantId) {
        return ledgerRepository.findAll(
                PredicateBuilder.<Ledger>and()
                        .equal("merchantId", merchantId)
                        .equal("ledgerStatus", LedgerStatus.ON_CREDIT)
                        .build()
        );
    }

    private BigDecimal sumNewDebt(Long merchantId, LocalDateTime startTime, LocalDateTime endTime) {
        BigDecimal result = ledgerRepository.sumTodayDebt(merchantId, startTime, endTime);
        return result != null ? result : BigDecimal.ZERO;
    }

    private BigDecimal sumPaidAmount(Long merchantId, LocalDateTime startTime, LocalDateTime endTime) {
        BigDecimal result = ledgerRepository.sumTodayPayment(merchantId, startTime, endTime);
        return result != null ? result : BigDecimal.ZERO;
    }

    private BigDecimal getCurrentTotalDebt(Long merchantId) {
        BigDecimal result = ledgerRepository.sumPendingAmount(merchantId);
        return result != null ? result : BigDecimal.ZERO;
    }

    private int getDebtCustomerCount(Long merchantId) {
        List<Ledger> debtLedgers = findDebtLedgers(merchantId);
        return (int) debtLedgers.stream()
                .map(Ledger::getCustomerId)
                .distinct()
                .count();
    }

    private LocalDateTime[] getTimeRange(String type, String date) {
        LocalDateTime startTime;
        LocalDateTime endTime;

        switch (type) {
            case "year":
                int year = Integer.parseInt(date);
                startTime = LocalDateTime.of(year, 1, 1, 0, 0);
                endTime = LocalDateTime.of(year + 1, 1, 1, 0, 0);
                break;
            case "month":
                YearMonth ym = YearMonth.parse(date);
                startTime = ym.atDay(1).atStartOfDay();
                endTime = ym.plusMonths(1).atDay(1).atStartOfDay();
                break;
            case "day":
            default:
                LocalDate targetDate = LocalDate.parse(date);
                startTime = targetDate.atStartOfDay();
                endTime = targetDate.atTime(LocalTime.MAX);
                break;
        }

        return new LocalDateTime[]{startTime, endTime};
    }
}
