package com.coreledger.repository;

import com.coreledger.entity.ProductCategory;
import com.coreledger.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 商品分类Repository接口
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long>, JpaSpecificationExecutor<ProductCategory> {

    /**
     * 根据父分类ID查询子分类列表
     *
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    List<ProductCategory> findByParentIdAndStatus(Long parentId, Status status);

    /**
     * 统计指定父分类下的子分类数量
     *
     * @param parentId 父分类ID
     * @param status 状态
     * @return 子分类数量
     */
    long countByParentIdAndStatus(Long parentId, Status status);
}
