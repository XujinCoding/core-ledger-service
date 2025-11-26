package com.coreledger.repository;

import com.coreledger.entity.ProductAttrTemplate;
import com.coreledger.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 商品属性模板 Repository
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Repository
public interface ProductAttrTemplateRepository extends JpaRepository<ProductAttrTemplate, Long>, JpaSpecificationExecutor<ProductAttrTemplate> {

    /**
     * 根据分类ID查询属性模板
     *
     * @param categoryId 分类ID
     * @param status 状态 (1=有效)
     * @return 属性模板列表
     */
    List<ProductAttrTemplate> findByCategoryIdAndStatusOrderBySortOrderAsc(Long categoryId, Status status);

    /**
     * 删除分类的所有属性模板
     *
     * @param categoryId 分类ID
     */
    void deleteByCategoryId(Long categoryId);
}
