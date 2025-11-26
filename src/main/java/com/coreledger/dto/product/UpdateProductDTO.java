package com.coreledger.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新商品DTO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Schema(description = "更新商品请求")
public class UpdateProductDTO {

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "商品名称")
    @Size(max = 100, message = "商品名称不能超过100个字符")
    private String name;

    @Schema(description = "商品主图URL")
    @Size(max = 500, message = "图片URL不能超过500个字符")
    private String imageUrl;

    @Schema(description = "商品描述")
    private String description;

    @Schema(description = "基本单位")
    @Size(max = 20, message = "单位不能超过20个字符")
    private String unit;

    @Schema(description = "存放位置")
    @Size(max = 100, message = "位置不能超过100个字符")
    private String location;

    @Schema(description = "备注")
    private String memo;
}
