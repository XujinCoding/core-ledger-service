package com.coreledger.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品修改DTO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Schema(description = "商品修改请求")
public class ProductUpdateDTO {

    /**
     * 商品名称
     */
    @NotBlank(message = "商品名称不能为空")
    @Size(max = 100, message = "商品名称长度不能超过100个字符")
    @Schema(description = "商品名称", example = "红富士苹果")
    private String name;

    /**
     * 商品主图URL
     */
    @Size(max = 500, message = "图片URL长度不能超过500个字符")
    @Schema(description = "商品主图URL")
    private String imageUrl;

    /**
     * 商品描述
     */
    @Size(max = 500, message = "描述长度不能超过500个字符")
    @Schema(description = "商品描述")
    private String description;

    /**
     * 标准价格
     */
    @DecimalMin(value = "0.00", message = "标准价格不能小于0")
    @Schema(description = "标准价格", example = "30.00")
    private BigDecimal price;

    /**
     * 规格型号
     */
    @Size(max = 100, message = "规格型号长度不能超过100个字符")
    @Schema(description = "规格型号")
    private String spec;

    /**
     * 单位
     */
    @Size(max = 20, message = "单位长度不能超过20个字符")
    @Schema(description = "单位", example = "斤")
    private String unit;

    /**
     * 存放位置
     */
    @Size(max = 100, message = "存放位置长度不能超过100个字符")
    @Schema(description = "存放位置", example = "A区3排5列")
    private String location;


    /**
     * 商品分类标识
     */
    @Schema(description = "商品分类标识", example = "1")
    private Long categoryId;

    /**
     * 备注
     */
    @Size(max = 255, message = "备注长度不能超过255个字符")
    @Schema(description = "备注")
    private String memo;
}
