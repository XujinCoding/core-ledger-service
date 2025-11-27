package com.coreledger.common.mapper.product;

import com.coreledger.common.mapper.BeanMapperConf;
import com.coreledger.entity.ProductSkuAttr;
import com.coreledger.vo.product.ProductSkuAttrVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 商品SKU属性Converter
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Mapper(config = BeanMapperConf.class)
public interface ProductSkuAttrConverter {

    /**
     * Entity转VO
     *
     * @param entity 实体
     * @return VO
     */
    @Mapping(source = "productAttrName", target = "attrName")
    @Mapping(source = "productAttrValueName", target = "attrValue")
    ProductSkuAttrVO toVO(ProductSkuAttr entity);
}
