package com.coreledger.controller;

import com.coreledger.common.PageQueryResult;
import com.coreledger.common.Result;
import com.coreledger.dto.ledger.*;
import com.coreledger.service.LedgerService;
import com.coreledger.vo.LedgerListStatsVO;
import com.coreledger.vo.LedgerListVO;
import com.coreledger.vo.LedgerVO;
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
 * 账本管理Controller
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/ledgers")
@RequiredArgsConstructor
@Tag(name = "账本管理", description = "账本增删改查、收款、赊账等操作")
public class LedgerController {

    private final LedgerService ledgerService;

    /**
     * 新增账单
     *
     * @param dto 新增账单DTO
     * @return 账单VO
     */
    @PostMapping
    @Operation(summary = "新增账单", description = "创建新账单并添加明细")
    public Result<LedgerVO> createLedger(@Valid @RequestBody CreateLedgerDTO dto) {
        return Result.success(ledgerService.createLedger(dto));
    }

    /**
     * 批量更新账单明细
     *
     * @param id 账单ID
     * @param dto 更新明细DTO
     * @return 账单VO
     */
    @PutMapping("/{id}/items")
    @Operation(summary = "批量更新明细", description = "批量新增、修改、删除账单明细")
    public Result<LedgerVO> updateLedgerItems(
        @PathVariable Long id,
        @Valid @RequestBody UpdateLedgerItemsDTO dto
    ) {
        return Result.success(ledgerService.updateLedgerItems(id, dto));
    }

    /**
     * 记账
     *
     * @param id 账单ID
     * @param dto 记账DTO
     * @return 账单VO
     */
    @PostMapping("/{id}/record")
    @Operation(summary = "记账", description = "记账操作，支持部分/全额支付或赊账")
    public Result<LedgerVO> recordLedger(
        @PathVariable Long id,
        @Valid @RequestBody RecordLedgerDTO dto
    ) {
        return Result.success(ledgerService.recordLedger(id, dto));
    }

    /**
     * 结账
     *
     * @param id 账单ID
     * @param dto 结账DTO
     * @return 账单VO
     */
    @PostMapping("/{id}/settle")
    @Operation(summary = "结账", description = "结账操作，差额作为优惠")
    public Result<LedgerVO> settleLedger(
        @PathVariable Long id,
        @Valid @RequestBody SettleLedgerDTO dto
    ) {
        return Result.success(ledgerService.settleLedger(id, dto));
    }

    /**
     * 新增支付记录（统一支付接口）
     *
     * @param id 账单ID
     * @param dto 支付记录DTO
     * @return 账单VO
     */
    @PostMapping("/{id}/payment-records")
    @Operation(summary = "新增支付记录", description = "统一支付接口，不考虑状态，只要未结清未关闭就可以添加支付记录")
    public Result<LedgerVO> addPaymentRecord(
        @PathVariable Long id,
        @Valid @RequestBody AddPaymentRecordDTO dto
    ) {
        return Result.success(ledgerService.addPaymentRecord(id, dto));
    }

    /**
     * 修改账单备注
     *
     * @param id 账单ID
     * @param dto 修改备注DTO
     * @return 账单VO
     */
    @PatchMapping("/{id}/memo")
    @Operation(summary = "修改账单备注", description = "修改账单的备注信息")
    public Result<LedgerVO> updateLedgerMemo(
        @PathVariable Long id,
        @Valid @RequestBody UpdateLedgerMemoDTO dto
    ) {
        ledgerService.updateLedgerMemo(id, dto);
        return Result.success();
    }

    /**
     * 关闭账单
     *
     * @param id 账单ID
     * @param dto 关闭DTO
     * @return 操作结果
     */
    @PostMapping("/{id}/close")
    @Operation(summary = "关闭账单", description = "关闭未支付的账单")
    public Result<Void> closeLedger(
        @PathVariable Long id,
        @Valid @RequestBody CloseLedgerDTO dto
    ) {
        ledgerService.closeLedger(id, dto);
        return Result.success();
    }

    /**
     * 根据客户查询账单列表
     *
     * @param queryDTO 查询条件
     * @param pageable 分页参数
     * @return 分页账单列表
     */
    @GetMapping
    @Operation(summary = "根据客户查询账单列表", description = "支持按状态和创建时间过滤，返回分页数据")
    public Result<Page<LedgerListVO>> queryLedgersByCustomer(
            LedgerQueryDTO queryDTO,
            @PageableDefault(size = 20, sort = "createInstant", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return Result.success(ledgerService.queryLedgersByCustomer(queryDTO, pageable));
    }

    /**
     * 查询进行中的账单
     *
     * @param pageable 分页参数
     * @return 分页账单列表
     */
    @GetMapping("/in-progress")
    @Operation(summary = "查询进行中的账单", description = "获取所有进行中状态的账单列表")
    public Result<Page<LedgerListVO>> queryInProgressLedgers(
            @PageableDefault(size = 20, sort = "createInstant", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return Result.success(ledgerService.queryInProgressLedgers(pageable));
    }

    /**
     * 搜索账单列表（支持客户姓名和电话模糊查询）
     *
     * @param dto 搜索条件
     * @return 分页账单列表
     */
    @GetMapping("/search")
    @Operation(summary = "搜索账单列表", description = "支持按客户姓名、电话模糊查询和状态过滤")
    public Result<PageQueryResult<LedgerListVO>> searchLedgers(LedgerSearchDTO dto) {
        return Result.success(ledgerService.searchLedgers(dto));
    }

    /**
     * 查询账单详情
     *
     * @param id 账单ID
     * @return 账单VO
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询账单详情", description = "获取账单完整信息，包括明细和支付记录")
    public Result<LedgerVO> getLedgerDetail(@PathVariable Long id) {
        return Result.success(ledgerService.getLedgerDetail(id));
    }

    /**
     * 获取账单列表统计数据
     *
     * @param dto 搜索条件（与搜索接口相同）
     * @return 账单列表统计VO
     */
    @GetMapping("/stats")
    @Operation(summary = "获取账单列表统计", description = "获取账单列表的统计数据，支持与搜索相同的条件")
    public Result<LedgerListStatsVO> getLedgerListStats(LedgerSearchDTO dto) {
        return Result.success(ledgerService.getLedgerListStats(dto));
    }
}
