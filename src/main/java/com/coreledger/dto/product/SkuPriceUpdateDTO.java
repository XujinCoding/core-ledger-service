package com.coreledger.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * SKU批量定价DTO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Schema(description = "SKU批量定价请求")
public class SkuPriceUpdateDTO {

    /**
     * SKU价格列表
     */
    @NotEmpty(message = "SKU价格列表不能为空")
    @Valid
    @Schema(description = "SKU价格列表")
    private List<SkuPriceItem> skuPrices;

    /**
     * SKU价格项
     */
    @Data
    @Schema(description = "SKU价格项")
    public static class SkuPriceItem {

        /**
         * SKU ID
         */
        @NotNull(message = "SKU ID不能为空")
        @Schema(description = "SKU ID", example = "1")
        private Long skuId;

        /**
         * 价格
         */
        @NotNull(message = "价格不能为空")
        @DecimalMin(value = "0.01", message = "价格必须大于0")
        @Schema(description = "价格", example = "25.00")
        private BigDecimal price;
    }
}
