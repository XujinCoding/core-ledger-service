package com.coreledger.dto.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 设置商品属性DTO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
public class SetProductAttributesDTO {

    /** 属性列表 */
    @NotEmpty(message = "属性列表不能为空")
    @Valid
    private List<AttributeDTO> attributes;

    /**
     * 单个属性DTO
     */
    @Data
    public static class AttributeDTO {

        /** 属性名称 */
        @NotNull(message = "属性名称不能为空")
        private String attrName;

        /** 属性值列表 */
        @NotEmpty(message = "属性值列表不能为空")
        private List<String> attrValues;

        /** 排序序号 */
        private Integer sortOrder;
    }
}
