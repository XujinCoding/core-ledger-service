package com.coreledger.repository;

import com.coreledger.entity.Product;
import com.coreledger.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * 商品Repository接口
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    /**
     * 统计指定分类下的商品数量
     *
     * @param categoryId 分类ID
     * @param status 状态
     * @return 商品数量
     */
    long countByCategoryIdAndStatus(Long categoryId, Status status);
}
