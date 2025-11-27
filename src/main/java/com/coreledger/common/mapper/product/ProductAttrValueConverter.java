package com.coreledger.common.mapper.product;

import com.coreledger.common.mapper.BeanMapperConf;
import com.coreledger.dto.product.ProductAttrValueCreateDTO;
import com.coreledger.entity.ProductAttrValue;
import com.coreledger.vo.product.ProductAttrValueVO;
import org.mapstruct.Mapper;

/**
 * 商品属性值Converter
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Mapper(config = BeanMapperConf.class)
public interface ProductAttrValueConverter {

    /**
     * Entity转VO
     *
     * @param entity 实体
     * @return VO
     */
    ProductAttrValueVO toVO(ProductAttrValue entity);

    /**
     * CreateDTO转Entity
     *
     * @param dto 创建DTO
     * @return 实体
     */
    ProductAttrValue toEntity(ProductAttrValueCreateDTO dto);
}
