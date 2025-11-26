package com.coreledger.dto.ledger;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 修改账单明细DTO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
public class UpdateLedgerItemsDTO {

    /** 新增明细 */
    @Valid
    private List<AddItemDTO> addItems;

    /** 修改已有明细(只能改价格和数量，不能改商品) */
    @Valid
    private List<UpdateItemDTO> updateItems;

    /** 删除的明细ID列表 */
    private List<Long> deleteItemIds;

    /**
     * 新增明细DTO
     */
    @Data
    public static class AddItemDTO {

        /** SKU ID */
        @NotNull(message = "SKU ID不能为空")
        private Long skuId;

        /** 商品ID (SPU) */
        @NotNull(message = "商品ID不能为空")
        private Long productId;

        /** 商品名称（冗余存储） */
        @NotNull(message = "商品名称不能为空")
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

    /**
     * 修改明细DTO
     */
    @Data
    public static class UpdateItemDTO {

        /** 明细ID */
        @NotNull(message = "明细ID不能为空")
        private Long id;

        /** 价格 */
        @DecimalMin(value = "0.01", message = "价格必须大于0")
        private BigDecimal price;

        /** 数量 */
        @Min(value = 1, message = "数量必须大于0")
        private Integer quantity;

        // 不允许修改 skuId
    }
}
