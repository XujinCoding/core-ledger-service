package com.coreledger.controller;

import com.coreledger.common.Result;
import com.coreledger.dto.ledger.CreateLedgerDTO;
import com.coreledger.dto.ledger.DiscountSettleDTO;
import com.coreledger.dto.ledger.ReceivePaymentDTO;
import com.coreledger.dto.ledger.UpdateLedgerItemsDTO;
import com.coreledger.enums.LedgerStatus;
import com.coreledger.service.LedgerService;
import com.coreledger.vo.ledger.LedgerDetailVO;
import com.coreledger.vo.ledger.LedgerListVO;
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
 * 账单控制器
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Tag(name = "账单管理", description = "账单的创建、修改、收款、赊账、结清等接口")
@RestController
@RequestMapping("/api/ledgers")
@RequiredArgsConstructor
public class LedgerController {

    private final LedgerService ledgerService;

    /**
     * 创建账单
     */
    @Operation(summary = "创建账单", description = "创建新账单，包含账单明细")
    @PostMapping
    public Result<LedgerDetailVO> createLedger(@Valid @RequestBody CreateLedgerDTO dto) {
        LedgerDetailVO ledger = ledgerService.createLedger(dto);
        return Result.success(ledger);
    }

    /**
     * 修改账单明细
     */
    @Operation(summary = "修改账单明细", description = "修改账单明细（新增/修改/删除），只有进行中和部分缴费状态可修改")
    @PutMapping("/{id}/items")
    public Result<LedgerDetailVO> updateLedgerItems(
            @Parameter(description = "账单ID", required = true)
            @PathVariable Long id,

            @Valid @RequestBody UpdateLedgerItemsDTO dto
    ) {
        LedgerDetailVO ledger = ledgerService.updateLedgerItems(id, dto);
        return Result.success(ledger);
    }

    /**
     * 收款
     */
    @Operation(summary = "收款", description = "收款（部分缴费或全额缴费），不允许超额收款")
    @PostMapping("/{id}/receive-payment")
    public Result<LedgerDetailVO> receivePayment(
            @Parameter(description = "账单ID", required = true)
            @PathVariable Long id,

            @Valid @RequestBody ReceivePaymentDTO dto
    ) {
        LedgerDetailVO ledger = ledgerService.receivePayment(id, dto);
        return Result.success(ledger);
    }

    /**
     * 优惠结清
     */
    @Operation(summary = "优惠结清", description = "优惠结清（抹零），直接结清账单")
    @PostMapping("/{id}/discount-settle")
    public Result<LedgerDetailVO> discountSettle(
            @Parameter(description = "账单ID", required = true)
            @PathVariable Long id,

            @Valid @RequestBody DiscountSettleDTO dto
    ) {
        LedgerDetailVO ledger = ledgerService.discountSettle(id, dto);
        return Result.success(ledger);
    }

    /**
     * 赊账
     */
    @Operation(summary = "赊账", description = "将进行中的账单转为赊账状态")
    @PostMapping("/{id}/on-credit")
    public Result<LedgerDetailVO> onCredit(
            @Parameter(description = "账单ID", required = true)
            @PathVariable Long id
    ) {
        LedgerDetailVO ledger = ledgerService.onCredit(id);
        return Result.success(ledger);
    }

    /**
     * 关闭账单
     */
    @Operation(summary = "关闭账单", description = "关闭账单（已结清和已关闭的账单不能关闭）")
    @PostMapping("/{id}/close")
    public Result<LedgerDetailVO> closeLedger(
            @Parameter(description = "账单ID", required = true)
            @PathVariable Long id
    ) {
        LedgerDetailVO ledger = ledgerService.closeLedger(id);
        return Result.success(ledger);
    }

    /**
     * 查询账单列表
     */
    @Operation(summary = "查询账单列表", description = "支持客户筛选、状态筛选")
    @GetMapping
    public Result<Page<LedgerListVO>> listLedgers(
            @Parameter(description = "客户ID筛选")
            @RequestParam(required = false) Long customerId,

            @Parameter(description = "账单状态: 1=进行中, 2=部分缴费, 3=已结清, 4=赊账中, 5=已关闭")
            @RequestParam(required = false) LedgerStatus ledgerStatus,

            @PageableDefault(size = 20, sort = "createInstant", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<LedgerListVO> ledgers = ledgerService.listLedgers(customerId, ledgerStatus, pageable);
        return Result.success(ledgers);
    }

    /**
     * 查询账单详情
     */
    @Operation(summary = "查询账单详情", description = "查询账单完整信息，包括明细、支付记录等")
    @GetMapping("/{id}")
    public Result<LedgerDetailVO> getLedgerDetail(
            @Parameter(description = "账单ID", required = true)
            @PathVariable Long id
    ) {
        LedgerDetailVO ledger = ledgerService.getLedgerDetail(id);
        return Result.success(ledger);
    }

    /**
     * 删除账单
     */
    @Operation(summary = "删除账单", description = "软删除账单，已结清的账单不允许删除")
    @DeleteMapping("/{id}")
    public Result<Void> deleteLedger(
            @Parameter(description = "账单ID", required = true)
            @PathVariable Long id
    ) {
        ledgerService.deleteLedger(id);
        return Result.success("账单删除成功");
    }
}
