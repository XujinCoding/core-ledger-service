package com.coreledger.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 商品属性批量更新DTO
 *
 * <p>一次性提交商品的所有属性和属性值，后端自动识别新增、修改、删除</p>
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Schema(description = "商品属性批量更新")
public class ProductAttrBatchUpdateDTO {

    /**
     * 属性列表（空列表表示删除所有属性）
     */
    @NotNull(message = "属性列表不能为null")
    @Schema(description = "属性列表")
    private List<@Valid AttrItem> attrs;

    /**
     * 属性项
     */
    @Data
    @Schema(description = "属性项")
    public static class AttrItem {

        /**
         * 属性ID（null表示新增，非null表示修改）
         */
        @Schema(description = "属性ID（null=新增，非null=修改）")
        private Long id;

        /**
         * 属性名称
         */
        @NotBlank(message = "属性名称不能为空")
        @Schema(description = "属性名称", example = "重量")
        private String attrName;

        /**
         * 排序
         */
        @NotNull(message = "排序不能为空")
        @Schema(description = "排序", example = "0")
        private Integer sortOrder;

        /**
         * 属性值列表（至少一个属性值）
         */
        @NotEmpty(message = "属性值列表不能为空")
        @Schema(description = "属性值列表")
        private List<@Valid AttrValueItem> values;
    }

    /**
     * 属性值项
     */
    @Data
    @Schema(description = "属性值项")
    public static class AttrValueItem {

        /**
         * 属性值ID（null表示新增，非null表示修改）
         */
        @Schema(description = "属性值ID（null=新增，非null=修改）")
        private Long id;

        /**
         * 属性值
         */
        @NotBlank(message = "属性值不能为空")
        @Schema(description = "属性值", example = "5斤")
        private String value;

        /**
         * 排序
         */
        @NotNull(message = "排序不能为空")
        @Schema(description = "排序", example = "0")
        private Integer sortOrder;
    }
}
