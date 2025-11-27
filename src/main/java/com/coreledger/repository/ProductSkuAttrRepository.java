package com.coreledger.repository;

import com.coreledger.entity.ProductSkuAttr;
import com.coreledger.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 商品SKU属性值Repository接口
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Repository
public interface ProductSkuAttrRepository extends JpaRepository<ProductSkuAttr, Long>, JpaSpecificationExecutor<ProductSkuAttr> {

    /**
     * 根据SKU ID查询属性列表
     *
     * @param skuId SKU ID
     * @param status 状态
     * @return 属性列表
     */
    List<ProductSkuAttr> findBySkuIdAndStatusOrderBySortOrderAsc(Long skuId, Status status);

    /**
     * 根据SKU ID删除属性
     *
     * @param skuId SKU ID
     */
    void deleteBySkuId(Long skuId);

    /**
     * 统计属性值被使用的次数
     *
     * @param productAttrValueId 属性值ID
     * @param status 状态
     * @return 使用次数
     */
    long countByProductAttrValueIdAndStatus(Long productAttrValueId, Status status);
}
