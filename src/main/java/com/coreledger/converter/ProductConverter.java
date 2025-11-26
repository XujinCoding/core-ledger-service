package com.coreledger.converter;

import com.coreledger.dto.product.CreateProductDTO;
import com.coreledger.dto.product.UpdateProductDTO;
import com.coreledger.entity.Product;
import com.coreledger.entity.ProductAttribute;
import com.coreledger.entity.ProductSku;
import com.coreledger.vo.product.ProductDetailVO;
import com.coreledger.vo.product.ProductListVO;
import com.coreledger.vo.product.SkuListVO;
import org.mapstruct.*;

import java.util.List;

/**
 * 商品转换器
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductConverter {

    /**
     * CreateDTO → Entity (SPU)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createInstant", ignore = true)
    @Mapping(target = "modifyInstant", ignore = true)
    @Mapping(target = "version", ignore = true)
    Product toEntity(CreateProductDTO dto);

    /**
     * Entity → ListVO
     */
    @Mapping(target = "categoryName", ignore = true)  // 需要通过分类服务查询
    @Mapping(target = "skuCount", ignore = true)      // 需要统计
    ProductListVO toListVO(Product entity);

    /**
     * Entity → DetailVO
     */
    @Mapping(target = "categoryName", ignore = true)   // 需要通过分类服务查询
    @Mapping(target = "attributes", ignore = true)     // 需要单独查询
    @Mapping(target = "skus", ignore = true)           // 需要单独查询
    ProductDetailVO toDetailVO(Product entity);

    /**
     * ProductAttribute → AttributeVO
     */
    @Mapping(source = "attrName", target = "attrName")
    @Mapping(source = "attrValues", target = "attrValues")
    ProductDetailVO.AttributeVO toAttributeVO(ProductAttribute entity);

    /**
     * List<ProductAttribute> → List<AttributeVO>
     */
    List<ProductDetailVO.AttributeVO> toAttributeVOList(List<ProductAttribute> entities);

    /**
     * ProductSku → SkuVO
     */
    @Mapping(source = "skuName", target = "skuName")
    @Mapping(source = "attrValueMap", target = "attrValueMap")
    @Mapping(source = "price", target = "price")
    @Mapping(source = "imageUrl", target = "imageUrl")
    ProductDetailVO.SkuVO toSkuVO(ProductSku entity);

    /**
     * List<ProductSku> → List<SkuVO>
     */
    List<ProductDetailVO.SkuVO> toSkuVOList(List<ProductSku> entities);

    /**
     * ProductSku → SkuListVO (for billing)
     */
    @Mapping(source = "id", target = "skuId")
    @Mapping(source = "productId", target = "productId")
    @Mapping(target = "productName", ignore = true)  // 需要通过商品服务查询
    @Mapping(source = "skuName", target = "skuName")
    @Mapping(source = "attrValueMap", target = "attrValueMap")
    @Mapping(source = "price", target = "price")
    @Mapping(source = "imageUrl", target = "imageUrl")
    @Mapping(target = "unit", ignore = true)        // 需要通过商品服务查询
    @Mapping(target = "location", ignore = true)    // 需要通过商品服务查询
    SkuListVO toSkuListVO(ProductSku entity);

    /**
     * 批量转换 ProductSku → SkuListVO
     */
    List<SkuListVO> toSkuListVOList(List<ProductSku> entities);

    /**
     * UpdateDTO → Entity (更新实体)
     * 只更新非null字段
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createInstant", ignore = true)
    @Mapping(target = "modifyInstant", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(UpdateProductDTO dto, @MappingTarget Product entity);
}
