package com.coreledger.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 地址库/行政区划实体
 *
 * <p>对应数据库表: sys_address</p>
 * <p>层级: 1=省, 2=市, 3=区县, 4=镇/乡/街道, 5=村</p>
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "sys_address")
public class SysAddress extends BaseEntity {

    /**
     * 父级ID (0表示顶级)
     */
    @Column(name = "parent_id", nullable = false)
    private Long parentId = 0L;

    /**
     * 地址名称
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 层级: 1=省, 2=市, 3=区县, 4=镇/乡/街道, 5=村
     */
    @Column(name = "level", nullable = false)
    private Integer level;

    /**
     * 全称路径 (如: 广东省-深圳市-南山区-西丽街道-留仙村)
     */
    @Column(name = "merger_name", length = 500)
    private String mergerName;

    /**
     * 判断是否为顶级地址
     *
     * @return true=顶级, false=非顶级
     */
    public boolean isTopLevel() {
        return this.parentId == null || this.parentId == 0L;
    }

    /**
     * 判断是否为村级地址
     *
     * @return true=村级, false=非村级
     */
    public boolean isVillageLevel() {
        return this.level != null && this.level == 5;
    }
}
