package com.coreledger.common.mapper.product;

import com.coreledger.common.mapper.BeanMapperConf;
import com.coreledger.entity.ProductSku;
import com.coreledger.vo.product.ProductSkuVO;
import org.mapstruct.Mapper;

/**
 * 商品SKU Converter
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Mapper(config = BeanMapperConf.class, uses = ProductSkuAttrConverter.class)
public interface ProductSkuConverter {

    /**
     * Entity转VO
     *
     * @param entity 实体
     * @return VO
     */
    ProductSkuVO toVO(ProductSku entity);
}
