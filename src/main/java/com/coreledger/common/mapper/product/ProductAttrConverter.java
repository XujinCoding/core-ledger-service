package com.coreledger.common.mapper.product;

import com.coreledger.common.mapper.BeanMapperConf;
import com.coreledger.dto.product.ProductAttrCreateDTO;
import com.coreledger.entity.ProductAttr;
import com.coreledger.vo.product.ProductAttrVO;
import org.mapstruct.Mapper;

/**
 * 商品属性Converter
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Mapper(config = BeanMapperConf.class, uses = ProductAttrValueConverter.class)
public interface ProductAttrConverter {

    /**
     * Entity转VO
     *
     * @param entity 实体
     * @return VO
     */
    ProductAttrVO toVO(ProductAttr entity);

    /**
     * CreateDTO转Entity
     *
     * @param dto 创建DTO
     * @return 实体
     */
    ProductAttr toEntity(ProductAttrCreateDTO dto);
}
