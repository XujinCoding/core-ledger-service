package com.coreledger.repository;

import com.coreledger.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 商品 Repository
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * 根据ID和状态查询商品
     *
     * @param id 商品ID
     * @param status 状态 (1=有效, 0=删除)
     * @return 商品
     */
    Optional<Product> findByIdAndStatus(Long id, Integer status);

    /**
     * 根据分类ID查询商品（分页）
     *
     * @param categoryId 分类ID
     * @param status 状态 (1=有效)
     * @param pageable 分页参数
     * @return 商品分页列表
     */
    Page<Product> findByCategoryIdAndStatus(Long categoryId, Integer status, Pageable pageable);

    /**
     * 根据商品名称模糊查询（分页）
     *
     * @param name 商品名称关键词
     * @param status 状态 (1=有效)
     * @param pageable 分页参数
     * @return 商品分页列表
     */
    Page<Product> findByNameContainingAndStatus(String name, Integer status, Pageable pageable);

    /**
     * 查询所有有效商品（分页）
     *
     * @param status 状态 (1=有效)
     * @param pageable 分页参数
     * @return 商品分页列表
     */
    Page<Product> findByStatus(Integer status, Pageable pageable);

    /**
     * 统计分类下的商品数量
     *
     * @param categoryId 分类ID
     * @param status 状态 (1=有效)
     * @return 商品数量
     */
    long countByCategoryIdAndStatus(Long categoryId, Integer status);
}
