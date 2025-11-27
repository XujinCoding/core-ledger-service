package com.coreledger.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 商品分类修改DTO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Schema(description = "商品分类修改请求")
public class CategoryUpdateDTO {

    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称长度不能超过50个字符")
    @Schema(description = "分类名称", example = "水果")
    private String name;

    /**
     * 排序序号
     */
    @Min(value = 0, message = "排序序号不能小于0")
    @Schema(description = "排序序号", example = "0")
    private Integer sortOrder;

    /**
     * 分类图标URL
     */
    @Size(max = 500, message = "图标URL长度不能超过500个字符")
    @Schema(description = "分类图标URL")
    private String iconUrl;

    /**
     * 备注
     */
    @Size(max = 255, message = "备注长度不能超过255个字符")
    @Schema(description = "备注")
    private String memo;
}
