package com.coreledger.repository;

import com.coreledger.entity.ProductAttr;
import com.coreledger.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 商品属性Repository接口
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Repository
public interface ProductAttrRepository extends JpaRepository<ProductAttr, Long>, JpaSpecificationExecutor<ProductAttr> {

    /**
     * 根据商品ID查询属性列表（按排序序号升序）
     *
     * @param productId 商品ID
     * @param status 状态
     * @return 属性列表
     */
    List<ProductAttr> findByProductIdAndStatusOrderBySortOrderAsc(Long productId, Status status);
}
