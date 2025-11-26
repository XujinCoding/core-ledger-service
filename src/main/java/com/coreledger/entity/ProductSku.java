package com.coreledger.entity;

import com.coreledger.config.converter.JsonMapConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 商品SKU实体 (Stock Keeping Unit - 库存单位)
 *
 * <p>对应数据库表: product_sku</p>
 * <p>存储商品的具体规格和价格</p>
 * <p>例如：红富士苹果-5斤-一级</p>
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "product_sku")
public class ProductSku extends BaseEntity {

    /** 商品ID (SPU) */
    @Column(name = "product_id", nullable = false)
    private Long productId;

    /**
     * 属性值映射
     * <p>JSON 格式，如：{"重量":"5斤", "等级":"一级"}</p>
     */
    @Column(name = "attr_value_map", nullable = false, length = 500)
    @Convert(converter = JsonMapConverter.class)
    private Map<String, String> attrValueMap = new HashMap<>();

    /**
     * SKU名称
     * <p>自动生成，格式：商品名-属性值1-属性值2</p>
     * <p>如：红富士苹果-5斤-一级</p>
     */
    @Column(name = "sku_name", nullable = false, length = 150)
    private String skuName;

    /** 销售价格 */
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    /** 成本价 (可选) */
    @Column(name = "cost_price", precision = 10, scale = 2)
    private BigDecimal costPrice;

    /** SKU图片URL (可选) */
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /** 排序序号 */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;
}
