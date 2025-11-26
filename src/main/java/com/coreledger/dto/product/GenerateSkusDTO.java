package com.coreledger.dto.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 批量生成SKU DTO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
public class GenerateSkusDTO {

    /** SKU价格策略 */
    @NotNull(message = "价格策略不能为空")
    private PriceStrategy priceStrategy;

    /** 统一价格（当策略为UNIFORM时使用） */
    @DecimalMin(value = "0.01", message = "统一价格必须大于0")
    private BigDecimal uniformPrice;

    /** SKU列表（当策略为MANUAL时使用） */
    @Valid
    private List<SkuDTO> skus;

    /**
     * 价格策略枚举
     */
    public enum PriceStrategy {
        /** 统一价格 */
        UNIFORM,
        /** 手动指定 */
        MANUAL
    }

    /**
     * SKU DTO
     */
    @Data
    public static class SkuDTO {

        /** 属性值映射 */
        @NotNull(message = "属性值映射不能为空")
        private Map<String, String> attrValueMap;

        /** 销售价格 */
        @NotNull(message = "价格不能为空")
        @DecimalMin(value = "0.01", message = "价格必须大于0")
        private BigDecimal price;

        /** SKU图片URL */
        private String imageUrl;
    }
}
