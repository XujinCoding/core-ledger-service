package com.coreledger.controller;

import com.coreledger.service.CustomerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Tag(name = "客户管理", description = "客户信息增删改查")
public class CustomerController {

    private final CustomerService customerService;

    // TODO: 添加接口方法
}
