package com.coreledger.controller;

import com.coreledger.common.Result;
import com.coreledger.dto.customer.CustomerAddressUpdateDTO;
import com.coreledger.dto.customer.CustomerCreateDTO;
import com.coreledger.dto.customer.CustomerSearchDTO;
import com.coreledger.dto.customer.CustomerUpdateDTO;
import com.coreledger.service.CustomerService;
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
     * 创建客户
     */
    @Operation(summary = "创建客户", description = "创建新客户（手机号不能重复，地址必须为村级）")
    @PostMapping
    public Result<CustomerVO> createCustomer(@Valid @RequestBody CustomerCreateDTO dto) {
        return Result.success(customerService.createCustomer(dto));
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
     * 更新客户地址
     */
    @Operation(summary = "更新客户地址", description = "修改客户的关联地址和详细地址")
    @PutMapping("/{id}/address")
    public Result<CustomerVO> updateCustomerAddress(@PathVariable Long id,
                                                     @Valid @RequestBody CustomerAddressUpdateDTO dto) {
        return Result.success(customerService.updateCustomerAddress(id, dto));
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
        return Result.success("删除成功");
    }
}
