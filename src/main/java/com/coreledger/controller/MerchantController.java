package com.coreledger.controller;

import com.coreledger.common.Result;
import com.coreledger.dto.merchant.CreateCustomerDTO;
import com.coreledger.entity.Customer;
import com.coreledger.entity.Merchant;
import com.coreledger.enums.BusinessCode;
import com.coreledger.exception.NotFoundException;
import com.coreledger.service.CustomerService;
import com.coreledger.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class MerchantController {

    private final MerchantService merchantService;
    private final CustomerService customerService;

    /**
     * 创建客户
     */
    @PostMapping("/customer/create")
    @Operation(summary = "创建客户")
    public Result<Customer> createCustomer(@RequestBody CreateCustomerDTO dto) {
        
        // 1. 查询商户
        Merchant merchant = merchantService.findById(dto.getMerchantId())
                .orElseThrow(() -> new NotFoundException(BusinessCode.MERCHANT_NOT_FOUND));

        // 2. 创建客户
        Customer customer = customerService.createUnregisteredCustomer(dto);
        return Result.success(customer);
    }

    /**
     * 获取商户信息
     */
    @GetMapping("/{merchantId}")
    @Operation(summary = "获取商户信息")
    public Result<Merchant> getMerchant(@PathVariable Long merchantId) {
        Merchant merchant = merchantService.findById(merchantId)
                .orElseThrow(() -> new NotFoundException(BusinessCode.MERCHANT_NOT_FOUND));
        return Result.success(merchant);
    }
}
