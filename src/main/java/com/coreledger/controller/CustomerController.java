package com.coreledger.controller;

import com.coreledger.common.Result;
import com.coreledger.dto.customer.CustomerProfileUpdateDTO;
import com.coreledger.dto.customer.CustomerSearchDTO;
import com.coreledger.dto.customer.CustomerUpdateDTO;
import com.coreledger.service.CustomerService;
import com.coreledger.vo.customer.CustomerListStatsVO;
import com.coreledger.vo.customer.CustomerStatsVO;
import com.coreledger.vo.customer.CustomerVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 客户管理Controller
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "客户管理", description = "客户信息增删改查、地址管理、条件查询")
public class CustomerController {

    private final CustomerService customerService;

    /**
     * 获取当前登录客户的个人信息（返回模板客户）
     */
    @Operation(summary = "获取个人信息", description = "获取当前登录客户的个人信息")
    @GetMapping("/profile")
    @PreAuthorize("@authz.isCustomer()") // 只有客户可以访问
    public Result<CustomerVO> getProfile() {
        return Result.success(customerService.getProfile());
    }

    /**
     * 客户修改个人信息（只更新模板客户）
     */
    @Operation(summary = "修改个人信息", description = "客户修改个人信息")
    @PutMapping("/profile")
    @PreAuthorize("@authz.isCustomer()") // 只有客户可以修改自己的信息
    public Result<CustomerVO> updateProfile(@Valid @RequestBody CustomerProfileUpdateDTO dto) {
        return Result.success(customerService.updateProfile(dto));
    }

    /**
     * 修改客户
     */
    @Operation(summary = "修改客户", description = "修改客户基本信息（不包括地址）")
    @PutMapping("/{id}")
    public Result<CustomerVO> updateCustomer(@PathVariable Long id,
                                             @Valid @RequestBody CustomerUpdateDTO dto) {
        return Result.success(customerService.updateCustomer(id, dto));
    }


    /**
     * 获取客户详情
     */
    @Operation(summary = "获取客户详情", description = "根据ID获取客户详情")
    @GetMapping("/{id}")
    public Result<CustomerVO> getCustomer(@PathVariable Long id) {
        return Result.success(customerService.getCustomer(id));
    }

    /**
     * 获取客户统计信息
     */
    @Operation(summary = "获取客户统计信息", description = "根据ID获取客户消费统计（总金额、订单数、平均消费）")
    @GetMapping("/{id}/stats")
    public Result<CustomerStatsVO> getCustomerStats(@PathVariable Long id) {
        return Result.success(customerService.getCustomerStats(id));
    }

    /**
     * 条件查询客户列表
     */
    @Operation(summary = "条件查询客户", description = "根据姓名、电话、地址ID查询客户（支持分页）")
    @GetMapping
    public Result<Page<CustomerVO>> searchCustomers(
            CustomerSearchDTO searchDTO,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return Result.success(customerService.searchCustomers(searchDTO, pageable));
    }

    /**
     * 删除客户
     */
    @Operation(summary = "删除客户", description = "物理删除客户（谨慎操作）")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return Result.success();
    }

    /**
     * 获取客户总数
     */
    @Operation(summary = "获取客户总数", description = "获取当前商户的客户总数")
    @GetMapping("/count")
    public Result<Long> getCustomerCount() {
        return Result.success(customerService.countCustomers());
    }

    /**
     * 获取客户列表统计（支持与搜索相同的条件）
     */
    @Operation(summary = "获取客户列表统计", description = "获取客户列表的统计数据，支持与搜索相同的条件")
    @GetMapping("/stats")
    public Result<CustomerListStatsVO> getCustomerListStats(CustomerSearchDTO dto) {
        return Result.success(customerService.getCustomerListStats(dto));
    }
}
