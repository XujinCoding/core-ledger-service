package com.coreledger.controller;

import com.coreledger.common.Result;
import com.coreledger.dto.address.AddressCreateDTO;
import com.coreledger.dto.address.AddressQueryDTO;
import com.coreledger.service.AddressService;
import com.coreledger.vo.address.AddressChainVO;
import com.coreledger.vo.address.AddressVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 地址管理Controller
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
@Tag(name = "地址管理", description = "地址查询接口，用于客户地址选择")
public class AddressController {

    private final AddressService addressService;

    /**
     * 获取地址详情
     */
    @Operation(summary = "获取地址详情", description = "根据ID获取地址详情（含完整路径）")
    @GetMapping("/{id}")
    public Result<AddressVO> getAddress(@PathVariable Long id) {
        return Result.success(addressService.getAddress(id));
    }

    /**
     * 创建地址
     */
    @Operation(summary = "创建地址", 
               description = "创建地址。parentId=0或null创建省级地址，指定parentId创建子级地址（自动计算层级和路径）")
    @PostMapping
    public Result<AddressVO> createAddress(@Valid @RequestBody AddressCreateDTO createDTO) {
        return Result.success(addressService.createAddress(createDTO));
    }

    /**
     * 查询地址列表
     */
    @Operation(summary = "查询地址列表", 
               description = "优先按父级ID查询子级地址，如果父级ID为null则按层级查询，都为null则返回省份列表")
    @GetMapping
    public Result<List<AddressVO>> listAddresses(AddressQueryDTO queryDTO) {
        return Result.success(addressService.listAddresses(queryDTO));
    }

    /**
     * 获取顶级地址（省）
     */
    @Operation(summary = "获取顶级地址", description = "获取所有省份列表")
    @GetMapping("/top")
    public Result<List<AddressVO>> listTopAddresses() {
        return Result.success(addressService.listTopAddresses());
    }

    /**
     * 根据父级ID查询子级地址
     */
    @Operation(summary = "查询子级地址", description = "根据父级ID查询下一级地址（用于级联选择）")
    @GetMapping("/children/{parentId}")
    public Result<List<AddressVO>> listAddressesByParent(@PathVariable Long parentId) {
        return Result.success(addressService.listAddressesByParent(parentId));
    }

    /**
     * 查询村级地址
     */
    @Operation(summary = "查询村级地址", description = "根据父级ID（镇/乡）查询村级地址，用于客户地址选择")
    @GetMapping("/village/{parentId}")
    public Result<List<AddressVO>> listVillageAddresses(@PathVariable Long parentId) {
        return Result.success(addressService.listVillageAddresses(parentId));
    }

    /**
     * 根据地址ID查询地址链（用于回显）
     */
    @Operation(summary = "查询地址链", 
               description = "根据地址ID向上查询完整地址链，用于客户编辑时回显地址级联选择器")
    @GetMapping("/chain/{addressId}")
    public Result<AddressChainVO> getAddressChain(@PathVariable Long addressId) {
        return Result.success(addressService.getAddressChain(addressId));
    }
}
