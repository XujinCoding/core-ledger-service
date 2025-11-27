package com.coreledger.common.mapper.product;

import com.coreledger.common.mapper.BeanMapperConf;
import com.coreledger.dto.product.ProductCreateDTO;
import com.coreledger.dto.product.ProductUpdateDTO;
import com.coreledger.entity.Product;
import com.coreledger.vo.product.ProductVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * 商品Converter
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Mapper(config = BeanMapperConf.class, uses = {ProductAttrConverter.class, ProductSkuConverter.class})
public interface ProductConverter {

    /**
     * Entity转VO
     *
     * @param entity 实体
     * @return VO
     */
    @Mapping(target = "categoryName", ignore = true)
    ProductVO toVO(Product entity);

    /**
     * CreateDTO转Entity
     *
     * @param dto 创建DTO
     * @return 实体
     */
    Product toEntity(ProductCreateDTO dto);

    /**
     * 更新Entity
     *
     * @param dto 更新DTO
     * @param entity 实体
     */
    void updateEntity(ProductUpdateDTO dto, @MappingTarget Product entity);
}
