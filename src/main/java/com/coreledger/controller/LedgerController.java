package com.coreledger.controller;

import com.coreledger.service.LedgerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    // TODO: 添加接口方法
}
