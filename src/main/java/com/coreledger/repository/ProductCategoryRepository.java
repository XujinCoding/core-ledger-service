package com.coreledger.repository;

import com.coreledger.entity.ProductCategory;
import com.coreledger.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 商品分类 Repository
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

    /**
     * 根据ID和状态查询分类
     *
     * @param id 分类ID
     * @param status 状态 (1=有效, 0=删除)
     * @return 分类
     */
    Optional<ProductCategory> findByIdAndStatus(Long id, Status status);

    /**
     * 根据父级ID查询子分类
     *
     * @param parentId 父级ID
     * @param status 状态 (1=有效)
     * @return 子分类列表
     */
    List<ProductCategory> findByParentIdAndStatus(Long parentId, Status status);

    /**
     * 统计子分类数量
     *
     * @param parentId 父级ID
     * @param status 状态 (1=有效)
     * @return 子分类数量
     */
    long countByParentIdAndStatus(Long parentId, Status status);

    /**
     * 查询所有有效分类
     *
     * @param status 状态 (1=有效)
     * @return 分类列表
     */
    List<ProductCategory> findByStatus(Status status);
}
