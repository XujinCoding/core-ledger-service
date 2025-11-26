package com.coreledger.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 创建商品DTO (SPU)
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
public class CreateProductDTO {

    /** 分类ID */
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    /** 商品名称 */
    @NotBlank(message = "商品名称不能为空")
    @Length(max = 100, message = "商品名称长度不能超过100")
    private String name;

    /** 商品主图URL */
    @Length(max = 500, message = "图片URL长度不能超过500")
    private String imageUrl;

    /** 商品描述 */
    @Length(max = 500, message = "商品描述长度不能超过500")
    private String description;

    /** 基本单位 */
    @NotBlank(message = "单位不能为空")
    @Length(max = 20, message = "单位长度不能超过20")
    private String unit;

    /** 存放位置 */
    @Length(max = 100, message = "存放位置长度不能超过100")
    private String location;

    /** 备注 */
    @Length(max = 255, message = "备注长度不能超过255")
    private String memo;
}
