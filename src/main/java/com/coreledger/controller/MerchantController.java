package com.coreledger.controller;

import com.coreledger.common.Result;
import com.coreledger.dto.auth.MerchantRegisterDTO;
import com.coreledger.dto.merchant.CreateCustomerDTO;
import com.coreledger.dto.merchant.UpdateMerchantDTO;
import com.coreledger.service.CustomerService;
import com.coreledger.service.MerchantService;
import com.coreledger.service.MerchantStatsService;
import com.coreledger.utils.SecurityUtils;
import com.coreledger.vo.customer.CustomerVO;
import com.coreledger.vo.merchant.MerchantOverviewVO;
import com.coreledger.vo.merchant.MerchantStatsVO;
import com.coreledger.vo.merchant.MerchantVO;
import com.coreledger.vo.merchant.TodayStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 商户控制器
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/merchant")
@RequiredArgsConstructor
@Tag(name = "商户管理", description = "商户相关接口")
@PreAuthorize("@authz.isMerchantOwner()") // 类级别权限: 只有商户所有者可以访问
public class MerchantController {

    private final MerchantService merchantService;
    private final CustomerService customerService;
    private final MerchantStatsService merchantStatsService;

    /**
     * 创建客户
     */
    @PostMapping("/customer/create")
    @Operation(summary = "创建客户")
    public Result<CustomerVO> createCustomer(@RequestBody CreateCustomerDTO dto) {
        return Result.success(customerService.createUnregisteredCustomer(dto));
    }

    /**
     * 获取商户信息
     */
    @GetMapping("/{merchantId}")
    @Operation(summary = "获取商户信息")
    public Result<MerchantVO> getMerchant(@PathVariable Long merchantId) {
        return Result.success(merchantService.getMerchantVO(merchantId));
    }

    /**
     * 获取商户本月统计数据
     */
    @GetMapping("/{merchantId}/stats")
    @Operation(summary = "获取商户本月统计", description = "获取本月销售额、待收款、本月订单数")
    public Result<MerchantStatsVO> getMerchantStats(@PathVariable Long merchantId) {
        return Result.success(merchantStatsService.getMerchantStats(merchantId));
    }

    /**
     * 获取商户今日汇总数据
     */
    @GetMapping("/{merchantId}/today-stats")
    @Operation(summary = "获取商户今日汇总", description = "获取今日销售额、已收款、新增欠款、订单数")
    public Result<TodayStatsVO> getTodayStats(@PathVariable Long merchantId) {
        return Result.success(merchantStatsService.getTodayStats(merchantId));
    }

    /**
     * 获取商户概览统计
     */
    @GetMapping("/{merchantId}/overview")
    @Operation(summary = "获取商户概览统计", description = "获取客户数、商品数、账单数")
    public Result<MerchantOverviewVO> getMerchantOverview(@PathVariable Long merchantId) {
        return Result.success(merchantStatsService.getMerchantOverview(merchantId));
    }

    /**
     * 更新商户信息
     */
    @PutMapping("/{merchantId}")
    @Operation(summary = "更新商户信息", description = "更新商户名称、手机号、地址等")
    public Result<MerchantVO> updateMerchant(@PathVariable Long merchantId, @RequestBody UpdateMerchantDTO dto) {
        return Result.success(merchantService.updateMerchant(merchantId, dto));
    }

    /**
     * 创建店铺（当前用户名下）
     */
    @PostMapping("/create")
    @Operation(summary = "创建店铺", description = "在当前用户名下创建新店铺")
    public Result<MerchantVO> createMerchant(@RequestBody MerchantRegisterDTO dto) {
        return Result.success(merchantService.createMerchant(dto, SecurityUtils.getCurrentUserId()));
    }
}
