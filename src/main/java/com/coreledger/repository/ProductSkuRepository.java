package com.coreledger.repository;

import com.coreledger.entity.ProductSku;
import com.coreledger.enums.PriceStatus;
import com.coreledger.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 商品SKU Repository接口
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Repository
public interface ProductSkuRepository extends JpaRepository<ProductSku, Long>, JpaSpecificationExecutor<ProductSku> {

    /**
     * 根据商品ID查询SKU列表
     *
     * @param productId 商品ID
     * @param status 状态
     * @return SKU列表
     */
    List<ProductSku> findByProductIdAndStatusOrderBySortOrderAsc(Long productId, Status status);

    /**
     * 根据商品ID和定价状态查询SKU列表（业务专用）
     *
     * @param productId 商品ID
     * @param priceStatus 定价状态
     * @param status 状态
     * @return SKU列表
     */
    List<ProductSku> findByProductIdAndPriceStatusAndStatusOrderBySortOrderAsc(Long productId, PriceStatus priceStatus, Status status);

    /**
     * 根据商品ID删除所有SKU
     *
     * @param productId 商品ID
     */
    void deleteByProductId(Long productId);
}
