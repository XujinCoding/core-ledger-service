package com.coreledger.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 商品分类实体
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "product_category")
public class ProductCategory extends BaseEntity {

    /** 父分类ID */
    @Column(name = "parent_id", nullable = false)
    private Long parentId = 0L;

    /** 分类名称 */
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    /** 分类层级 */
    @Column(name = "level", nullable = false)
    private Integer level = 1;

    /** 排序序号 */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    /** 分类图标URL */
    @Column(name = "icon_url", length = 500)
    private String iconUrl;
}
