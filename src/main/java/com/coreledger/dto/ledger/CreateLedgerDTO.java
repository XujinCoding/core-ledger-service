package com.coreledger.dto.ledger;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 创建账单DTO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
public class CreateLedgerDTO {

    /** 客户ID */
    @NotNull(message = "客户ID不能为空")
    private Long customerId;

    /** 账单明细列表 */
    @NotEmpty(message = "账单明细不能为空")
    @Valid
    private List<LedgerItemDTO> items;

    /** 备注 */
    @Length(max = 255, message = "备注长度不能超过255")
    private String memo;

    /**
     * 账单明细DTO
     */
    @Data
    public static class LedgerItemDTO {

        /** SKU ID */
        @NotNull(message = "SKU ID不能为空")
        private Long skuId;

        /** 商品ID (SPU) */
        @NotNull(message = "商品ID不能为空")
        private Long productId;

        /** 商品名称（冗余存储） */
        @NotBlank(message = "商品名称不能为空")
        private String productName;

        /** SKU名称（冗余存储） */
        private String skuName;

        /** 属性值映射（冗余存储） */
        private Map<String, String> attrValueMap;

        /** 价格 */
        @NotNull(message = "价格不能为空")
        @DecimalMin(value = "0.01", message = "价格必须大于0")
        private BigDecimal price;

        /** 数量 */
        @NotNull(message = "数量不能为空")
        @Min(value = 1, message = "数量必须大于0")
        private Integer quantity;
    }
}
