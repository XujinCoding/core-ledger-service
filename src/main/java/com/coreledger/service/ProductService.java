package com.coreledger.service;

import cn.hutool.core.util.StrUtil;
import com.coreledger.common.mapper.product.ProductConverter;
import com.coreledger.common.mapper.product.ProductAttrConverter;
import com.coreledger.common.mapper.product.ProductSkuConverter;
import com.coreledger.utils.specification.PredicateBuilder;
import com.coreledger.dto.product.ProductCreateDTO;
import com.coreledger.dto.product.ProductUpdateDTO;
import com.coreledger.entity.Product;
import com.coreledger.entity.ProductCategory;
import com.coreledger.entity.ProductAttr;
import com.coreledger.entity.ProductSku;
import com.coreledger.enums.BusinessCode;
import com.coreledger.enums.Status;
import com.coreledger.exception.NotFoundException;
import com.coreledger.repository.ProductRepository;
import com.coreledger.repository.ProductCategoryRepository;
import com.coreledger.repository.ProductAttrRepository;
import com.coreledger.repository.ProductSkuRepository;
import com.coreledger.vo.product.ProductVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 商品业务服务类
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductAttrRepository productAttrRepository;
    private final ProductSkuRepository productSkuRepository;
    private final ProductConverter productConverter;
    private final ProductAttrConverter productAttrConverter;
    private final ProductSkuConverter productSkuConverter;

    /**
     * 创建商品
     *
     * @param dto 创建DTO
     * @return 商品VO
     * @throws NotFoundException 当分类不存在时抛出
     */
    @Transactional
    public ProductVO createProduct(ProductCreateDTO dto) {
        // 验证分类是否存在
        ProductCategory category = productCategoryRepository.findById(dto.getCategoryId())
            .orElseThrow(() -> new NotFoundException(BusinessCode.PRODUCT_CATEGORY_NOT_FOUND));

        // 创建商品
        Product product = productConverter.toEntity(dto);
        product.setStatus(Status.ACTIVE);
        product = productRepository.save(product);
        
        log.info("创建商品成功, ID: {}, 名称: {}, 分类: {}", 
            product.getId(), product.getName(), category.getName());
        
        return toDetailVO(product);
    }

    /**
     * 修改商品
     *
     * @param id 商品ID
     * @param dto 更新DTO
     * @return 商品VO
     * @throws NotFoundException 当商品不存在时抛出
     */
    @Transactional
    public ProductVO updateProduct(Long id, ProductUpdateDTO dto) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(BusinessCode.PRODUCT_NOT_FOUND));

        productConverter.updateEntity(dto, product);
        product = productRepository.save(product);
        
        log.info("修改商品成功, ID: {}, 名称: {}", id, product.getName());
        return toDetailVO(product);
    }

    /**
     * 删除商品
     *
     * @param id 商品ID
     * @throws NotFoundException 当商品不存在时抛出
     */
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(BusinessCode.PRODUCT_NOT_FOUND));

        // 逻辑删除商品
        product.setStatus(Status.INACTIVE);
        productRepository.save(product);
        
        // 逻辑删除所有属性
        productAttrRepository.findByProductIdAndStatusOrderBySortOrderAsc(id, Status.ACTIVE)
            .forEach(attr -> {
                attr.setStatus(Status.INACTIVE);
                productAttrRepository.save(attr);
            });
        
        // 逻辑删除所有SKU
        productSkuRepository.findByProductIdAndStatusOrderBySortOrderAsc(id, Status.ACTIVE)
            .forEach(sku -> {
                sku.setStatus(Status.INACTIVE);
                productSkuRepository.save(sku);
            });
        
        log.info("删除商品成功, ID: {}, 名称: {}", id, product.getName());
    }

    /**
     * 获取商品详情（含属性和SKU）
     *
     * @param id 商品ID
     * @return 商品VO
     * @throws NotFoundException 当商品不存在时抛出
     */
    public ProductVO getProduct(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(BusinessCode.PRODUCT_NOT_FOUND));
        
        return toDetailVO(product);
    }

    /**
     * 获取商品列表
     *
     * @param categoryId 分类ID（可选）
     * @param keyword 关键词（可选）
     * @param pageable 分页参数
     * @return 商品列表
     */
    public Page<ProductVO> listProducts(Long categoryId, String keyword, Pageable pageable) {
        Specification<Product> spec = PredicateBuilder.<Product>and()
            .equal("status", Status.ACTIVE)
            .equal(Objects::nonNull, "categoryId", categoryId)
            .like(StrUtil::isNotBlank, "name", keyword)
            .build();

        return productRepository.findAll(spec, pageable)
            .map(this::toSimpleVO);
    }

    /**
     * 启用/禁用商品
     *
     * @param id 商品ID
     * @param status 状态
     * @return 商品VO
     * @throws NotFoundException 当商品不存在时抛出
     */
    @Transactional
    public ProductVO updateStatus(Long id, Status status) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(BusinessCode.PRODUCT_NOT_FOUND));

        product.setStatus(status);
        product = productRepository.save(product);
        
        log.info("更新商品状态成功, ID: {}, 状态: {}", id, status);
        return toDetailVO(product);
    }

    /**
     * 转换为详细VO（含属性和SKU）
     *
     * @param product 商品实体
     * @return 商品VO
     */
    private ProductVO toDetailVO(Product product) {
        ProductVO vo = productConverter.toVO(product);
        
        // 设置分类名称
        productCategoryRepository.findById(product.getCategoryId())
            .ifPresent(category -> vo.setCategoryName(category.getName()));
        
        // 加载属性列表
        vo.setAttrs(
            productAttrRepository.findByProductIdAndStatusOrderBySortOrderAsc(product.getId(), Status.ACTIVE)
                .stream()
                .map(productAttrConverter::toVO)
                .collect(Collectors.toList())
        );
        
        // 加载SKU列表
        vo.setSkus(
            productSkuRepository.findByProductIdAndStatusOrderBySortOrderAsc(product.getId(), Status.ACTIVE)
                .stream()
                .map(productSkuConverter::toVO)
                .collect(Collectors.toList())
        );
        
        return vo;
    }

    /**
     * 转换为简单VO（不含属性和SKU）
     *
     * @param product 商品实体
     * @return 商品VO
     */
    private ProductVO toSimpleVO(Product product) {
        ProductVO vo = productConverter.toVO(product);
        
        // 设置分类名称
        productCategoryRepository.findById(product.getCategoryId())
            .ifPresent(category -> vo.setCategoryName(category.getName()));
        
        return vo;
    }
}
