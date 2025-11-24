package com.coreledger.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 商品分类实体
 *
 * <p>对应数据库表: product_category</p>
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "product_category")
public class ProductCategory extends BaseEntity {

    /**
     * 父分类ID
     * <p>0 表示顶级分类</p>
     */
    @Column(name = "parent_id", nullable = false)
    private Long parentId = 0L;

    /** 分类名称 */
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    /**
     * 排序序号
     * <p>数字越小越靠前</p>
     */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;
}
