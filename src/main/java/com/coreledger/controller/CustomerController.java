package com.coreledger.controller;

import com.coreledger.common.Result;
import com.coreledger.dto.customer.CreateCustomerDTO;
import com.coreledger.dto.customer.UpdateCustomerDTO;
import com.coreledger.enums.CustomerType;
import com.coreledger.service.CustomerService;
import com.coreledger.vo.customer.CustomerDetailVO;
import com.coreledger.vo.customer.CustomerListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

/**
 * 客户控制器
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Tag(name = "客户管理", description = "客户的创建、查询、修改、删除等接口")
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    /**
     * 创建客户
     */
    @Operation(summary = "创建客户", description = "创建新客户，手机号必须唯一")
    @PostMapping
    public Result<CustomerDetailVO> createCustomer(@Valid @RequestBody CreateCustomerDTO dto) {
        CustomerDetailVO customer = customerService.createCustomer(dto);
        return Result.success(customer);
    }

    /**
     * 查询客户列表
     */
    @Operation(summary = "查询客户列表", description = "支持关键词搜索（姓名/手机号）、地址筛选、客户类型筛选，含欠款统计")
    @GetMapping
    public Result<Page<CustomerListVO>> listCustomers(
            @Parameter(description = "关键词（姓名/手机号模糊查询）")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "地址ID筛选")
            @RequestParam(required = false) Long addressId,

            @Parameter(description = "客户类型: 0=潜在客户, 1=活跃客户")
            @RequestParam(required = false) CustomerType customerType,

            @PageableDefault(size = 20, sort = "createInstant", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<CustomerListVO> customers = customerService.listCustomers(keyword, addressId, customerType, pageable);
        return Result.success(customers);
    }

    /**
     * 查询客户详情
     */
    @Operation(summary = "查询客户详情", description = "查询客户基本信息及账单统计")
    @GetMapping("/{id}")
    public Result<CustomerDetailVO> getCustomerDetail(
            @Parameter(description = "客户ID", required = true)
            @PathVariable Long id
    ) {
        CustomerDetailVO customer = customerService.getCustomerDetail(id);
        return Result.success(customer);
    }

    /**
     * 更新客户信息
     */
    @Operation(summary = "更新客户信息", description = "更新客户基本信息，只更新提供的字段")
    @PutMapping("/{id}")
    public Result<CustomerDetailVO> updateCustomer(
            @Parameter(description = "客户ID", required = true)
            @PathVariable Long id,

            @Valid @RequestBody UpdateCustomerDTO dto
    ) {
        CustomerDetailVO customer = customerService.updateCustomer(id, dto);
        return Result.success(customer);
    }

    /**
     * 删除客户
     */
    @Operation(summary = "删除客户", description = "软删除客户，有未结清账单时禁止删除")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCustomer(
            @Parameter(description = "客户ID", required = true)
            @PathVariable Long id
    ) {
        customerService.deleteCustomer(id);
        return Result.success("客户删除成功");
    }
}
