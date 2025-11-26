package com.coreledger.entity;

import com.coreledger.config.converter.JsonListConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品属性实体
 *
 * <p>对应数据库表: product_attribute</p>
 * <p>用于存储商品的动态属性及其可选值</p>
 * <p>例如：重量属性，可选值为 ["5斤", "10斤", "20斤"]</p>
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "product_attribute")
public class ProductAttribute extends BaseEntity {

    /** 商品ID (SPU) */
    @Column(name = "product_id", nullable = false)
    private Long productId;

    /**
     * 属性名称
     * <p>如：重量、等级、包装等</p>
     */
    @Column(name = "attr_name", nullable = false, length = 50)
    private String attrName;

    /**
     * 属性值列表
     * <p>JSON 数组格式，如：["5斤", "10斤", "20斤"]</p>
     */
    @Column(name = "attr_values", nullable = false, length = 500)
    @Convert(converter = JsonListConverter.class)
    private List<String> attrValues = new ArrayList<>();

    /** 排序序号 */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;
}
