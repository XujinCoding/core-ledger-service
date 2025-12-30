package com.coreledger.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品分类Mapper接口
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Mapper
public interface ProductCategoryMapper {

    /**
     * 递归查询指定分类及其所有子分类的ID
     *
     * @param categoryId 分类ID
     * @param status 状态
     * @return 分类ID列表（包含自身及所有子孙分类）
     */
    List<Long> findAllCategoryIdsRecursive(@Param("categoryId") Long categoryId, @Param("status") Integer status);
}
