package com.coreledger.vo.product;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 商品详情VO (SPU)
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Schema(description = "商品详情响应")
public class ProductDetailVO {

    @Schema(description = "商品ID")
    private Long id;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "商品名称")
    private String name;

    @Schema(description = "商品主图URL")
    private String imageUrl;

    @Schema(description = "商品描述")
    private String description;

    @Schema(description = "基本单位")
    private String unit;

    @Schema(description = "存放位置")
    private String location;

    @Schema(description = "备注")
    private String memo;

    @Schema(description = "属性列表")
    private List<AttributeVO> attributes;

    @Schema(description = "SKU列表")
    private List<SkuVO> skus;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createInstant;

    @Schema(description = "修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime modifyInstant;

    /**
     * 属性VO
     */
    @Data
    @Schema(description = "商品属性")
    public static class AttributeVO {

        @Schema(description = "属性ID")
        private Long id;

        @Schema(description = "属性名称")
        private String attrName;

        @Schema(description = "属性值列表")
        private List<String> attrValues;
    }

    /**
     * SKU VO
     */
    @Data
    @Schema(description = "商品SKU")
    public static class SkuVO {

        @Schema(description = "SKU ID")
        private Long id;

        @Schema(description = "SKU名称")
        private String skuName;

        @Schema(description = "属性值映射")
        private Map<String, String> attrValueMap;

        @Schema(description = "销售价格")
        private java.math.BigDecimal price;

        @Schema(description = "SKU图片URL")
        private String imageUrl;
    }
}
