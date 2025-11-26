package com.coreledger.repository;

import com.coreledger.entity.ProductAttribute;
import com.coreledger.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 商品属性 Repository
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Repository
public interface ProductAttributeRepository extends JpaRepository<ProductAttribute, Long>, JpaSpecificationExecutor<ProductAttribute> {

    /**
     * 根据商品ID查询所有属性
     *
     * @param productId 商品ID
     * @param status 状态 (1=有效)
     * @return 属性列表
     */
    List<ProductAttribute> findByProductIdAndStatusOrderBySortOrderAsc(Long productId, Status status);

    /**
     * 根据商品ID和属性名称查询
     *
     * @param productId 商品ID
     * @param attrName 属性名称
     * @param status 状态 (1=有效)
     * @return 属性
     */
    Optional<ProductAttribute> findByProductIdAndAttrNameAndStatus(Long productId, String attrName, Status status);

    /**
     * 删除商品的所有属性
     *
     * @param productId 商品ID
     */
    void deleteByProductId(Long productId);
}
