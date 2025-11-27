package com.coreledger.entity;

import com.coreledger.config.converter.PriceStatusConverter;
import com.coreledger.enums.PriceStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品SKU实体
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "product_sku")
public class ProductSku extends BaseEntity {

    /** 商品ID */
    @Column(name = "product_id", nullable = false)
    private Long productId;

    /** SKU名称 */
    @Column(name = "sku_name", nullable = false, length = 150)
    private String skuName;

    /** 定价状态 */
    @Column(name = "price_status", nullable = false)
    @Convert(converter = PriceStatusConverter.class)
    private PriceStatus priceStatus = PriceStatus.UNPRICED;

    /** SKU销售价格 */
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    /** SKU图片URL */
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /** 排序序号 */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    /** SKU属性值列表 */
    @OneToMany(mappedBy = "skuId", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<ProductSkuAttr> skuAttrs = new ArrayList<>();
}
