package com.coreledger.controller;

import com.coreledger.common.Result;
import com.coreledger.service.ReportService;
import com.coreledger.vo.report.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 报表控制器
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
@Tag(name = "报表管理", description = "经营报表和欠款汇总相关接口")
public class ReportController {

    private final ReportService reportService;

    /**
     * 获取收入统计
     */
    @GetMapping("/{merchantId}/income-stats")
    @Operation(summary = "获取收入统计", description = "按年/月/日获取收入统计数据")
    public Result<IncomeStatsVO> getIncomeStats(
            @PathVariable @Parameter(description = "商户ID") Long merchantId,
            @RequestParam @Parameter(description = "时间类型：year/month/day") String type,
            @RequestParam @Parameter(description = "日期参数") String date) {
        return Result.success(reportService.getIncomeStats(merchantId, type, date));
    }

    /**
     * 获取商品销售统计
     */
    @GetMapping("/{merchantId}/product-sales")
    @Operation(summary = "获取商品销售统计", description = "获取商品销售占比数据")
    public Result<List<ProductSalesVO>> getProductSales(
            @PathVariable @Parameter(description = "商户ID") Long merchantId,
            @RequestParam @Parameter(description = "时间类型：year/month/day") String type,
            @RequestParam @Parameter(description = "日期参数") String date) {
        return Result.success(reportService.getProductSales(merchantId, type, date));
    }

    /**
     * 获取客户交易排行
     */
    @GetMapping("/{merchantId}/customer-ranking")
    @Operation(summary = "获取客户交易排行", description = "获取客户交易金额排行榜")
    public Result<List<CustomerRankingVO>> getCustomerRanking(
            @PathVariable @Parameter(description = "商户ID") Long merchantId,
            @RequestParam(defaultValue = "20") @Parameter(description = "返回数量限制") Integer limit,
            @RequestParam @Parameter(description = "日期参数") String date) {
        return Result.success(reportService.getCustomerRanking(merchantId, limit, date));
    }

    /**
     * 获取欠款趋势
     */
    @GetMapping("/{merchantId}/debt-trend")
    @Operation(summary = "获取欠款趋势", description = "按年/月/日获取欠款趋势数据")
    public Result<DebtTrendVO> getDebtTrend(
            @PathVariable @Parameter(description = "商户ID") Long merchantId,
            @RequestParam @Parameter(description = "时间类型：year/month/day") String type,
            @RequestParam @Parameter(description = "日期参数") String date) {
        return Result.success(reportService.getDebtTrend(merchantId, type, date));
    }

    /**
     * 获取按地址欠款分布
     */
    @GetMapping("/{merchantId}/debt-by-address")
    @Operation(summary = "获取按地址欠款分布", description = "获取各地址的欠款金额分布")
    public Result<List<DebtByAddressVO>> getDebtByAddress(
            @PathVariable @Parameter(description = "商户ID") Long merchantId,
            @RequestParam @Parameter(description = "日期参数") String date) {
        return Result.success(reportService.getDebtByAddress(merchantId, date));
    }

    /**
     * 获取按客户欠款排行
     */
    @GetMapping("/{merchantId}/debt-by-customer")
    @Operation(summary = "获取按客户欠款排行", description = "获取客户欠款金额排行榜")
    public Result<List<DebtByCustomerVO>> getDebtByCustomer(
            @PathVariable @Parameter(description = "商户ID") Long merchantId,
            @RequestParam(defaultValue = "20") @Parameter(description = "返回数量限制") Integer limit,
            @RequestParam @Parameter(description = "日期参数") String date) {
        return Result.success(reportService.getDebtByCustomer(merchantId, limit, date));
    }
}
