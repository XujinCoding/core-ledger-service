package com.coreledger.repository;

import com.coreledger.entity.ProductAttrValue;
import com.coreledger.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 商品属性值Repository接口
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Repository
public interface ProductAttrValueRepository extends JpaRepository<ProductAttrValue, Long>, JpaSpecificationExecutor<ProductAttrValue> {

    /**
     * 根据属性ID查询属性值列表（按排序序号升序）
     *
     * @param productAttrId 商品属性ID
     * @param status 状态
     * @return 属性值列表
     */
    List<ProductAttrValue> findByProductAttrIdAndStatusOrderBySortOrderAsc(Long productAttrId, Status status);
}
