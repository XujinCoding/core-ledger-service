package com.coreledger.common.mapper.product;

import com.coreledger.common.mapper.BeanMapperConf;
import com.coreledger.dto.product.CategoryCreateDTO;
import com.coreledger.dto.product.CategoryUpdateDTO;
import com.coreledger.entity.ProductCategory;
import com.coreledger.vo.product.CategoryTreeVO;
import com.coreledger.vo.product.CategoryVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

/**
 * 商品分类Converter
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Mapper(config = BeanMapperConf.class)
public interface ProductCategoryConverter {

    /**
     * Entity转VO
     *
     * @param entity 实体
     * @return VO
     */
    CategoryVO toVO(ProductCategory entity);

    /**
     * Entity转TreeVO
     *
     * @param entity 实体
     * @return TreeVO
     */
    CategoryTreeVO toTreeVO(ProductCategory entity);

    /**
     * CreateDTO转Entity
     *
     * @param dto 创建DTO
     * @return 实体
     */
    ProductCategory toEntity(CategoryCreateDTO dto);

    /**
     * 更新Entity
     *
     * @param dto 更新DTO
     * @param entity 实体
     */
    void updateEntity(CategoryUpdateDTO dto, @MappingTarget ProductCategory entity);
}
