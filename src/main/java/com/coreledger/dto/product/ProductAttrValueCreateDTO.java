package com.coreledger.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 商品属性值创建DTO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Schema(description = "商品属性值创建请求")
public class ProductAttrValueCreateDTO {

    /**
     * 属性值
     */
    @NotBlank(message = "属性值不能为空")
    @Size(max = 255, message = "属性值长度不能超过255个字符")
    @Schema(description = "属性值", example = "5斤")
    private String value;

    /**
     * 排序序号
     */
    @Min(value = 0, message = "排序序号不能小于0")
    @Schema(description = "排序序号", example = "0")
    private Integer sortOrder = 0;
}
