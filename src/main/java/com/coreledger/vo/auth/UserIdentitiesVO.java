package com.coreledger.vo.auth;

import com.coreledger.entity.Merchant;
import com.coreledger.vo.customer.CustomerVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 用户身份列表VO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Schema(description = "用户身份列表")
public class UserIdentitiesVO {

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1")
    private Long userId;

    /**
     * 用户拥有的商户列表
     */
    @Schema(description = "用户拥有的商户列表")
    private List<Merchant> merchants;

    /**
     * 用户是客户的商户列表
     */
    @Schema(description = "用户是客户的商户列表")
    private List<CustomerVO> customers;
}
