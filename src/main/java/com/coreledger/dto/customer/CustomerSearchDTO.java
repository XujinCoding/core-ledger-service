package com.coreledger.dto.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 客户查询请求DTO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Schema(description = "客户查询请求DTO")
public class CustomerSearchDTO {

    /**
     * 客户姓名（模糊查询）
     */
    @Schema(description = "客户姓名（模糊查询）", example = "张三")
    private String name;

    /**
     * 客户电话（模糊查询）
     */
    @Schema(description = "客户电话（模糊查询）", example = "138")
    private String phone;

    /**
     * 地址ID（精确查询，来自sys_address表）
     */
    @Schema(description = "地址ID（从地址树选择）", example = "12345")
    private Long addressId;
}
