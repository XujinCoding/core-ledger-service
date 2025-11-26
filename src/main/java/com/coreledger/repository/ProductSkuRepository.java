package com.coreledger.repository;

import com.coreledger.entity.ProductSku;
import com.coreledger.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 商品SKU Repository
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Repository
public interface ProductSkuRepository extends JpaRepository<ProductSku, Long>, JpaSpecificationExecutor<ProductSku> {

    /**
     * 根据ID和状态查询SKU
     *
     * @param id SKU ID
     * @param status 状态 (1=有效)
     * @return SKU
     */
    Optional<ProductSku> findByIdAndStatus(Long id, Status status);

    /**
     * 根据商品ID查询所有SKU
     *
     * @param productId 商品ID
     * @param status 状态 (1=有效)
     * @return SKU列表
     */
    List<ProductSku> findByProductIdAndStatusOrderBySortOrderAsc(Long productId, Status status);

    /**
     * 根据商品ID查询SKU（分页）
     *
     * @param productId 商品ID
     * @param status 状态 (1=有效)
     * @param pageable 分页参数
     * @return SKU分页列表
     */
    Page<ProductSku> findByProductIdAndStatus(Long productId, Status status, Pageable pageable);

    /**
     * 根据SKU名称模糊查询（用于开单时搜索商品）
     *
     * @param skuName SKU名称关键词
     * @param status 状态 (1=有效)
     * @param pageable 分页参数
     * @return SKU分页列表
     */
    Page<ProductSku> findBySkuNameContainingAndStatus(String skuName, Status status, Pageable pageable);

    /**
     * 统计商品的SKU数量
     *
     * @param productId 商品ID
     * @param status 状态 (1=有效)
     * @return SKU数量
     */
    long countByProductIdAndStatus(Long productId, Status status);

    /**
     * 删除商品的所有SKU
     *
     * @param productId 商品ID
     */
    void deleteByProductId(Long productId);

    /**
     * 根据商品ID列表批量查询SKU
     *
     * @param productIds 商品ID列表
     * @param status 状态 (1=有效)
     * @return SKU列表
     */
    @Query("SELECT s FROM ProductSku s WHERE s.productId IN :productIds AND s.status = :status ORDER BY s.productId, s.sortOrder")
    List<ProductSku> findByProductIdIn(@Param("productIds") List<Long> productIds, @Param("status") Integer status);

    /**
     * 批量统计商品的SKU数量（按商品ID分组）
     *
     * @param productIds 商品ID列表
     * @return Map<商品ID, SKU数量>
     */
    @Query("SELECT s.productId as productId, COUNT(s.id) as skuCount FROM ProductSku s WHERE s.productId IN :productIds AND s.status = 1 GROUP BY s.productId")
    List<SkuCountProjection> countByProductIdGroupedRaw(@Param("productIds") List<Long> productIds);

    /**
     * 批量统计SKU数量（返回Map）
     */
    default java.util.Map<Long, Long> countByProductIdGrouped(List<Long> productIds) {
        return countByProductIdGroupedRaw(productIds).stream()
                .collect(java.util.stream.Collectors.toMap(
                        SkuCountProjection::getProductId,
                        SkuCountProjection::getSkuCount
                ));
    }

    /**
     * SKU数量投影接口
     */
    interface SkuCountProjection {
        Long getProductId();
        Long getSkuCount();
    }

    /**
     * 根据状态查询所有SKU（分页）
     *
     * @param status 状态 (1=有效)
     * @param pageable 分页参数
     * @return SKU分页列表
     */
    Page<ProductSku> findByStatus(Status status, Pageable pageable);
}
