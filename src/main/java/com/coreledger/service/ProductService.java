package com.coreledger.service;

import cn.hutool.core.util.StrUtil;
import com.coreledger.converter.ProductConverter;
import com.coreledger.dto.product.CreateProductDTO;
import com.coreledger.dto.product.GenerateSkusDTO;
import com.coreledger.dto.product.SetProductAttributesDTO;
import com.coreledger.dto.product.UpdateProductDTO;
import com.coreledger.entity.Product;
import com.coreledger.entity.ProductAttribute;
import com.coreledger.entity.ProductSku;
import com.coreledger.enums.Status;
import com.coreledger.exception.BusinessException;
import com.coreledger.enums.BusinessCode;
import com.coreledger.exception.NotFoundException;
import com.coreledger.repository.ProductAttributeRepository;
import com.coreledger.repository.ProductRepository;
import com.coreledger.repository.ProductSkuRepository;
import com.coreledger.utils.specification.PredicateBuilder;
import com.coreledger.vo.product.ProductDetailVO;
import com.coreledger.vo.product.ProductListVO;
import com.coreledger.vo.product.SkuListVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品服务 (SPU)
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
    private final ProductAttributeRepository productAttributeRepository;
    private final ProductSkuRepository productSkuRepository;
    private final ProductConverter productConverter;

    /**
     * 创建商品 (SPU)
     *
     * @param dto 创建商品DTO
     * @return 商品详情
     */
    @Transactional
    public ProductDetailVO createProduct(CreateProductDTO dto) {
        log.info("创建商品: {}", dto.getName());

        // 1. 转换并保存 SPU
        Product product = productConverter.toEntity(dto);
        product = productRepository.save(product);

        log.info("商品创建成功, ID: {}", product.getId());

        // 2. 返回详情
        return getProductDetail(product.getId());
    }

    /**
     * 设置商品属性（动态属性）
     *
     * @param productId 商品ID
     * @param dto       设置属性DTO
     */
    @Transactional
    public void setProductAttributes(Long productId, SetProductAttributesDTO dto) {
        log.info("设置商品属性: productId={}, attributes={}", productId, dto.getAttributes().size());

        // 1. 查询商品
        Product product = productRepository.findByIdAndStatus(productId, Status.ACTIVE)
                .orElseThrow(() -> new NotFoundException("商品不存在"));

        // 2. 删除旧属性
        productAttributeRepository.deleteByProductId(productId);

        // 3. 保存新属性
        int sortOrder = 1;
        for (SetProductAttributesDTO.AttributeDTO attrDTO : dto.getAttributes()) {
            ProductAttribute attribute = new ProductAttribute();
            attribute.setProductId(productId);
            attribute.setAttrName(attrDTO.getAttrName());
            attribute.setAttrValues(attrDTO.getAttrValues());
            attribute.setSortOrder(sortOrder++);
            productAttributeRepository.save(attribute);
        }

        log.info("商品属性设置成功: productId={}", productId);
    }

    /**
     * 生成SKU（批量生成或手动指定）
     *
     * @param productId 商品ID
     * @param dto       生成SKU DTO
     */
    @Transactional
    public void generateSkus(Long productId, GenerateSkusDTO dto) {
        log.info("生成SKU: productId={}, strategy={}", productId, dto.getPriceStrategy());

        // 1. 查询商品
        Product product = productRepository.findByIdAndStatus(productId, Status.ACTIVE)
                .orElseThrow(() -> new NotFoundException("商品不存在"));

        // 2. 查询商品属性
        List<ProductAttribute> attributes = productAttributeRepository.findByProductIdAndStatusOrderBySortOrderAsc(productId, Status.ACTIVE);
        if (attributes.isEmpty()) {
            throw new BusinessException(BusinessCode.BUSINESS_ERROR, "请先设置商品属性");
        }

        // 3. 根据策略生成SKU
        if (dto.getPriceStrategy() == GenerateSkusDTO.PriceStrategy.UNIFORM) {
            // 统一价格：生成所有笛卡尔积组合
            generateSkusWithUniformPrice(product, attributes, dto.getUniformPrice());
        } else {
            // 手动指定：根据提供的SKU列表生成
            generateSkusManually(product, dto.getSkus());
        }

        log.info("SKU生成成功: productId={}", productId);
    }

    /**
     * 生成SKU - 统一价格策略
     */
    private void generateSkusWithUniformPrice(Product product, List<ProductAttribute> attributes, BigDecimal uniformPrice) {
        // 1. 删除旧SKU
        productSkuRepository.deleteByProductId(product.getId());

        // 2. 生成笛卡尔积
        List<Map<String, String>> combinations = generateCartesianProduct(attributes);

        // 3. 批量保存SKU
        int sortOrder = 1;
        for (Map<String, String> combination : combinations) {
            ProductSku sku = new ProductSku();
            sku.setProductId(product.getId());
            sku.setSkuName(generateSkuName(product.getName(), combination));
            sku.setAttrValueMap(combination);
            sku.setPrice(uniformPrice);
            sku.setSortOrder(sortOrder++);
            sku.setStatus(Status.ACTIVE);
            productSkuRepository.save(sku);
        }
    }

    /**
     * 生成SKU - 手动指定策略
     */
    private void generateSkusManually(Product product, List<GenerateSkusDTO.SkuDTO> skuDTOs) {
        // 1. 删除旧SKU
        productSkuRepository.deleteByProductId(product.getId());

        // 2. 批量保存SKU
        int sortOrder = 1;
        for (GenerateSkusDTO.SkuDTO skuDTO : skuDTOs) {
            ProductSku sku = new ProductSku();
            sku.setProductId(product.getId());
            sku.setSkuName(generateSkuName(product.getName(), skuDTO.getAttrValueMap()));
            sku.setAttrValueMap(skuDTO.getAttrValueMap());
            sku.setPrice(skuDTO.getPrice());
            sku.setImageUrl(skuDTO.getImageUrl());
            sku.setSortOrder(sortOrder++);
            sku.setStatus(Status.ACTIVE);
            productSkuRepository.save(sku);
        }
    }

    /**
     * 生成笛卡尔积组合
     */
    private List<Map<String, String>> generateCartesianProduct(List<ProductAttribute> attributes) {
        List<Map<String, String>> result = new ArrayList<>();
        generateCartesianProductRecursive(attributes, 0, new HashMap<>(), result);
        return result;
    }

    /**
     * 递归生成笛卡尔积
     */
    private void generateCartesianProductRecursive(List<ProductAttribute> attributes,
                                                    int index,
                                                    Map<String, String> current,
                                                    List<Map<String, String>> result) {
        if (index == attributes.size()) {
            result.add(new HashMap<>(current));
            return;
        }

        ProductAttribute attribute = attributes.get(index);
        for (String value : attribute.getAttrValues()) {
            current.put(attribute.getAttrName(), value);
            generateCartesianProductRecursive(attributes, index + 1, current, result);
            current.remove(attribute.getAttrName());
        }
    }

    /**
     * 生成SKU名称 (格式: "商品名-属性值1-属性值2")
     */
    private String generateSkuName(String productName, Map<String, String> attrValueMap) {
        if (attrValueMap.isEmpty()) {
            return productName;
        }
        String attrPart = attrValueMap.values().stream()
                .collect(Collectors.joining("-"));
        return productName + "-" + attrPart;
    }

    /**
     * 查询商品列表
     *
     * @param keyword    关键词（名称模糊查询）
     * @param categoryId 分类ID筛选
     * @param pageable   分页参数
     * @return 商品分页列表
     */
    public Page<ProductListVO> listProducts(String keyword, Long categoryId, Pageable pageable) {
        log.info("查询商品列表: keyword={}, categoryId={}", keyword, categoryId);

        // 1. 使用 PredicateBuilder 构建动态查询条件
        Specification<Product> spec = PredicateBuilder.<Product>and()
                .equal("status", Status.ACTIVE)
                .equal(categoryId != null, "categoryId", categoryId)
                .like(StrUtil::isNotBlank, "name", keyword)
                .build();

        // 2. 分页查询
        Page<Product> productPage = productRepository.findAll(spec, pageable);

        // 3. 转换为VO
        Page<ProductListVO> voPage = productPage.map(productConverter::toListVO);

        // 4. 填充SKU数量
        List<Long> productIds = voPage.getContent().stream()
                .map(ProductListVO::getId)
                .collect(Collectors.toList());

        if (!productIds.isEmpty()) {
            Map<Long, Long> skuCountMap = productSkuRepository.countByProductIdGrouped(productIds);
            voPage.getContent().forEach(vo -> {
                vo.setSkuCount(skuCountMap.getOrDefault(vo.getId(), 0L).intValue());
            });
        }

        // 5. TODO: 批量查询分类名称
        // Map<Long, String> categoryMap = categoryService.batchGetCategoryNames(categoryIds);
        // voPage.getContent().forEach(vo -> vo.setCategoryName(categoryMap.get(vo.getCategoryId())));

        return voPage;
    }

    /**
     * 查询商品详情
     *
     * @param id 商品ID
     * @return 商品详情
     */
    public ProductDetailVO getProductDetail(Long id) {
        log.info("查询商品详情: {}", id);

        // 1. 查询商品
        Product product = productRepository.findByIdAndStatus(id, Status.ACTIVE)
                .orElseThrow(() -> new NotFoundException("商品不存在"));

        // 2. 转换为VO
        ProductDetailVO vo = productConverter.toDetailVO(product);

        // 3. 查询属性列表
        List<ProductAttribute> attributes = productAttributeRepository.findByProductIdAndStatusOrderBySortOrderAsc(id, Status.ACTIVE);
        vo.setAttributes(productConverter.toAttributeVOList(attributes));

        // 4. 查询SKU列表
        List<ProductSku> skus = productSkuRepository.findByProductIdAndStatusOrderBySortOrderAsc(id, Status.ACTIVE);
        vo.setSkus(productConverter.toSkuVOList(skus));

        // 5. TODO: 查询分类名称
        // String categoryName = categoryService.getCategoryName(product.getCategoryId());
        // vo.setCategoryName(categoryName);

        return vo;
    }

    /**
     * 更新商品信息
     *
     * @param id  商品ID
     * @param dto 更新商品DTO
     * @return 商品详情
     */
    @Transactional
    public ProductDetailVO updateProduct(Long id, UpdateProductDTO dto) {
        log.info("更新商品信息: {}", id);

        // 1. 查询商品
        Product product = productRepository.findByIdAndStatus(id, Status.ACTIVE)
                .orElseThrow(() -> new NotFoundException("商品不存在"));

        // 2. 更新字段（只更新非null字段）
        productConverter.updateEntity(dto, product);
        productRepository.save(product);

        log.info("商品信息更新成功: {}", id);

        // 3. 返回详情
        return getProductDetail(id);
    }

    /**
     * 删除商品（软删除+级联检查）
     *
     * @param id 商品ID
     */
    @Transactional
    public void deleteProduct(Long id) {
        log.info("删除商品: {}", id);

        // 1. 查询商品
        Product product = productRepository.findByIdAndStatus(id, Status.ACTIVE)
                .orElseThrow(() -> new NotFoundException("商品不存在"));

        // 2. TODO: 级联检查：查询是否有未结清的账单使用了该商品的SKU
        // long activeCount = ledgerItemRepository.countBySkuIdInAndLedgerStatusNotClearedAndStatus(...)
        // if (activeCount > 0) {
        //     throw new BusinessException("该商品有未结清账单在使用，无法删除");
        // }

        // 3. 软删除 SPU
        product.setStatus(Status.INACTIVE);
        productRepository.save(product);

        // 4. 软删除所有 SKU
        List<ProductSku> skus = productSkuRepository.findByProductIdAndStatusOrderBySortOrderAsc(id, Status.ACTIVE);
        skus.forEach(sku -> sku.setStatus(Status.INACTIVE));
        productSkuRepository.saveAll(skus);

        // 5. 软删除所有属性
        List<ProductAttribute> attributes = productAttributeRepository.findByProductIdAndStatusOrderBySortOrderAsc(id, Status.ACTIVE);
        attributes.forEach(attr -> attr.setStatus(Status.INACTIVE));
        productAttributeRepository.saveAll(attributes);

        log.info("商品删除成功: {}", id);
    }

    /**
     * 查询SKU列表（用于开单时选择商品）
     *
     * @param keyword  关键词（SKU名称模糊查询）
     * @param pageable 分页参数
     * @return SKU分页列表
     */
    public Page<SkuListVO> listSkusForBilling(String keyword, Pageable pageable) {
        log.info("查询SKU列表（开单）: keyword={}", keyword);

        Page<ProductSku> skuPage;
        if (StrUtil.isNotBlank(keyword)) {
            skuPage = productSkuRepository.findBySkuNameContainingAndStatus(keyword, Status.ACTIVE, pageable);
        } else {
            skuPage = productSkuRepository.findByStatus(Status.ACTIVE, pageable);
        }

        // 转换为VO
        Page<SkuListVO> voPage = skuPage.map(productConverter::toSkuListVO);

        // 批量查询商品信息
        List<Long> productIds = voPage.getContent().stream()
                .map(SkuListVO::getProductId)
                .distinct()
                .collect(Collectors.toList());

        if (!productIds.isEmpty()) {
            Map<Long, Product> productMap = productRepository.findAllById(productIds).stream()
                    .collect(Collectors.toMap(Product::getId, product -> product));

            voPage.getContent().forEach(vo -> {
                Product product = productMap.get(vo.getProductId());
                if (product != null) {
                    vo.setProductName(product.getName());
                    vo.setUnit(product.getUnit());
                    vo.setLocation(product.getLocation());
                }
            });
        }

        return voPage;
    }
}
