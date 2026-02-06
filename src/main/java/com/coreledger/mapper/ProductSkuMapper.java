package com.coreledger.mapper;

import com.coreledger.entity.ProductSku;
import com.coreledger.enums.PriceStatus;
import com.coreledger.enums.Status;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品SKU Mapper接口
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Mapper
public interface ProductSkuMapper {

    /**
     * 根据SKU名称、定价状态、商户ID查询SKU列表
     *
     * @param skuName SKU名称（模糊查询）
     * @param priceStatus 定价状态
     * @param status 状态
     * @param merchantId 商户ID
     * @return SKU列表
     */
    List<ProductSku> findBySkuNameAndMerchant(
        @Param("skuName") String skuName,
        @Param("priceStatus") PriceStatus priceStatus,
        @Param("status") Status status,
        @Param("merchantId") Long merchantId
    );
}
