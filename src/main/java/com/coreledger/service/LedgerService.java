package com.coreledger.service;

import com.coreledger.repository.LedgerRepository;
import com.coreledger.repository.LedgerItemRepository;
import com.coreledger.repository.PaymentRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 账本业务服务类
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LedgerService {

    private final LedgerRepository ledgerRepository;
    private final LedgerItemRepository ledgerItemRepository;
    private final PaymentRecordRepository paymentRecordRepository;

    // TODO: 添加业务方法
}
