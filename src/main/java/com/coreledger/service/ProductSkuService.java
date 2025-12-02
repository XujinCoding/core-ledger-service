package com.coreledger.service;

import cn.hutool.core.collection.CollUtil;
import com.coreledger.common.mapper.product.ProductSkuConverter;
import com.coreledger.dto.product.SkuPriceUpdateDTO;
import com.coreledger.entity.*;
import com.coreledger.enums.BusinessCode;
import com.coreledger.enums.PriceStatus;
import com.coreledger.enums.Status;
import com.coreledger.exception.BusinessException;
import com.coreledger.exception.NotFoundException;
import com.coreledger.repository.*;
import com.coreledger.vo.product.ProductSkuVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品SKU Service
 *
 * <p>核心功能：</p>
 * <ul>
 *   <li>SKU笛卡尔积生成算法</li>
 *   <li>SKU定价管理</li>
 *   <li>SKU查询（支持定价状态筛选）</li>
 * </ul>
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductSkuService {

    private final ProductSkuRepository skuRepository;
    private final ProductSkuAttrRepository skuAttrRepository;
    private final ProductRepository productRepository;
    private final ProductAttrRepository attrRepository;
    private final ProductAttrValueRepository attrValueRepository;
    private final ProductSkuConverter skuConverter;

    /** SKU数量警告阈值 */
    private static final int SKU_COUNT_WARNING_THRESHOLD = 100;

    /**
     * 生成商品SKU（笛卡尔积算法）
     *
     * @param productId 商品ID
     * @throws NotFoundException 当商品不存在时抛出
     * @throws BusinessException 当商品无属性或SKU数量过多时抛出
     */
    @Transactional
    public void generateSkus(Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new NotFoundException(BusinessCode.PRODUCT_NOT_FOUND));

        // 1. 查询商品所有有效属性
        List<ProductAttr> attrs = attrRepository.findByProductIdAndStatusOrderBySortOrderAsc(
            productId, Status.ACTIVE);

        if (CollUtil.isEmpty(attrs)) {
            throw new BusinessException(BusinessCode.PRODUCT_NO_ATTRS);
        }

        // 2. 查询每个属性的有效属性值
        Map<ProductAttr, List<ProductAttrValue>> attrValueMap = new LinkedHashMap<>();
        for (ProductAttr attr : attrs) {
            List<ProductAttrValue> values = attrValueRepository
                .findByProductAttrIdAndStatusOrderBySortOrderAsc(attr.getId(), Status.ACTIVE);
            if (CollUtil.isNotEmpty(values)) {
                attrValueMap.put(attr, values);
            }
        }

        if (attrValueMap.isEmpty()) {
            log.warn("商品{}没有有效的属性值，无法生成SKU", productId);
            return;
        }

        // 3. 计算SKU数量并检查
        long skuCount = calculateSkuCount(attrValueMap);
        if (skuCount > SKU_COUNT_WARNING_THRESHOLD) {
            throw new BusinessException(BusinessCode.PRODUCT_SKU_TOO_MANY,
                String.format("将生成%d个SKU，超过建议上限%d", skuCount, SKU_COUNT_WARNING_THRESHOLD));
        }

        // 4. 先删除现有SKU
        List<ProductSku> existingSkus = skuRepository.findByProductIdAndStatusOrderBySortOrderAsc(
            productId, Status.ACTIVE);
        for (ProductSku sku : existingSkus) {
            skuAttrRepository.deleteBySkuId(sku.getId());
        }
        skuRepository.deleteByProductId(productId);

        // 5. 笛卡尔积生成SKU组合
        List<List<ProductAttrValue>> cartesianProduct = cartesianProduct(
            new ArrayList<>(attrValueMap.values()));

        int sortOrder = 0;
        for (List<ProductAttrValue> combination : cartesianProduct) {
            // 构建SKU名称
            String skuName = buildSkuName(product.getName(), combination, attrValueMap);

            // 创建SKU
            ProductSku sku = new ProductSku();
            sku.setProductId(productId);
            sku.setSkuName(skuName);
            sku.setPriceStatus(PriceStatus.UNPRICED);
            sku.setPrice(BigDecimal.ZERO);
            sku.setSortOrder(sortOrder++);
            sku.setStatus(Status.ACTIVE);
            sku = skuRepository.save(sku);

            // 创建SKU属性关联
            int attrSortOrder = 0;
            for (ProductAttrValue attrValue : combination) {
                ProductAttr attr = findAttrByValueId(attrValueMap, attrValue.getId());
                if (attr != null) {
                    ProductSkuAttr skuAttr = new ProductSkuAttr();
                    skuAttr.setSkuId(sku.getId());
                    skuAttr.setProductAttrId(attr.getId());
                    skuAttr.setProductAttrName(attr.getAttrName());
                    skuAttr.setProductAttrValueId(attrValue.getId());
                    skuAttr.setProductAttrValueName(attrValue.getValue());
                    skuAttr.setSortOrder(attrSortOrder++);
                    skuAttr.setStatus(Status.ACTIVE);
                    skuAttrRepository.save(skuAttr);
                }
            }
        }

        log.info("商品{}生成SKU成功，共生成{}个SKU", productId, cartesianProduct.size());
    }

    /**
     * 获取商品SKU列表
     *
     * @param productId 商品ID
     * @param priceStatus 定价状态（可选）
     * @return SKU列表
     */
    public List<ProductSkuVO> getProductSkus(Long productId, PriceStatus priceStatus) {
        List<ProductSku> skus;
        
        if (priceStatus != null) {
            skus = skuRepository.findByProductIdAndPriceStatusAndStatusOrderBySortOrderAsc(
                productId, priceStatus, Status.ACTIVE);
        } else {
            skus = skuRepository.findByProductIdAndStatusOrderBySortOrderAsc(
                productId, Status.ACTIVE);
        }

        return skus.stream()
            .map(skuConverter::toVO)
            .collect(Collectors.toList());
    }

    /**
     * 获取已定价SKU列表（业务专用）
     *
     * @param productId 商品ID
     * @return 已定价SKU列表
     */
    public List<ProductSkuVO> getPricedSkus(Long productId) {
        return getProductSkus(productId, PriceStatus.PRICED);
    }

    public List<ProductSkuVO> searchPricedSkusByName(String skuName) {
        List<ProductSku> skus = skuRepository
            .findBySkuNameContainingIgnoreCaseAndPriceStatusAndStatusOrderBySortOrderAsc(
                skuName, PriceStatus.PRICED, Status.ACTIVE);
        return skus.stream().map(skuConverter::toVO).collect(Collectors.toList());
    }

    /**
     * 获取未定价SKU列表（定价管理专用）
     *
     * @param productId 商品ID
     * @return 未定价SKU列表
     */
    public List<ProductSkuVO> getUnpricedSkus(Long productId) {
        return getProductSkus(productId, PriceStatus.UNPRICED);
    }

    /**
     * 获取SKU详情
     *
     * @param id SKU ID
     * @return SKU VO
     * @throws NotFoundException 当SKU不存在时抛出
     */
    public ProductSkuVO getSkuDetail(Long id) {
        ProductSku sku = skuRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(BusinessCode.PRODUCT_SKU_NOT_FOUND));
        
        return skuConverter.toVO(sku);
    }

    /**
     * 修改SKU价格
     *
     * @param id SKU ID
     * @param price 价格
     * @return SKU VO
     * @throws NotFoundException 当SKU不存在时抛出
     * @throws BusinessException 当价格无效时抛出
     */
    @Transactional
    public ProductSkuVO updateSkuPrice(Long id, BigDecimal price) {
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(BusinessCode.PRODUCT_SKU_PRICE_INVALID);
        }

        ProductSku sku = skuRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(BusinessCode.PRODUCT_SKU_NOT_FOUND));

        sku.setPrice(price);
        sku.setPriceStatus(PriceStatus.PRICED);
        sku = skuRepository.save(sku);

        log.info("修改SKU价格成功, ID: {}, 价格: {}", id, price);
        return skuConverter.toVO(sku);
    }

    /**
     * 批量定价
     *
     * @param dto 批量定价DTO
     * @return 定价成功数量
     */
    @Transactional
    public int batchUpdatePrice(SkuPriceUpdateDTO dto) {
        int successCount = 0;

        for (SkuPriceUpdateDTO.SkuPriceItem item : dto.getSkuPrices()) {
            try {
                updateSkuPrice(item.getSkuId(), item.getPrice());
                successCount++;
            } catch (Exception e) {
                log.error("批量定价失败, SKU ID: {}, 错误: {}", item.getSkuId(), e.getMessage());
            }
        }

        log.info("批量定价完成，成功{}个，失败{}个", 
            successCount, dto.getSkuPrices().size() - successCount);
        return successCount;
    }

    /**
     * 启用/禁用SKU
     *
     * @param id SKU ID
     * @param status 状态
     * @return SKU VO
     * @throws NotFoundException 当SKU不存在时抛出
     */
    @Transactional
    public ProductSkuVO updateSkuStatus(Long id, Status status) {
        ProductSku sku = skuRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(BusinessCode.PRODUCT_SKU_NOT_FOUND));

        sku.setStatus(status);
        sku = skuRepository.save(sku);

        log.info("更新SKU状态成功, ID: {}, 状态: {}", id, status);
        return skuConverter.toVO(sku);
    }

    /**
     * 删除SKU
     *
     * @param id SKU ID
     * @throws NotFoundException 当SKU不存在时抛出
     */
    @Transactional
    public void deleteSku(Long id) {
        ProductSku sku = skuRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(BusinessCode.PRODUCT_SKU_NOT_FOUND));

        // 逻辑删除
        sku.setStatus(Status.INACTIVE);
        skuRepository.save(sku);

        log.info("删除SKU成功, ID: {}", id);
    }

    /**
     * 笛卡尔积算法
     *
     * @param lists 属性值列表的列表
     * @return 笛卡尔积结果
     */
    private List<List<ProductAttrValue>> cartesianProduct(List<List<ProductAttrValue>> lists) {
        if (CollUtil.isEmpty(lists)) {
            return new ArrayList<>();
        }

        List<List<ProductAttrValue>> result = new ArrayList<>();
        cartesianProductHelper(lists, result, 0, new ArrayList<>());
        return result;
    }

    /**
     * 笛卡尔积递归辅助方法
     */
    private void cartesianProductHelper(List<List<ProductAttrValue>> lists,
                                       List<List<ProductAttrValue>> result,
                                       int depth,
                                       List<ProductAttrValue> current) {
        if (depth == lists.size()) {
            result.add(new ArrayList<>(current));
            return;
        }

        for (ProductAttrValue value : lists.get(depth)) {
            current.add(value);
            cartesianProductHelper(lists, result, depth + 1, current);
            current.remove(current.size() - 1);
        }
    }

    /**
     * 构建SKU名称
     *
     * @param productName 商品名称
     * @param combination 属性值组合
     * @param attrValueMap 属性值映射
     * @return SKU名称
     */
    private String buildSkuName(String productName, 
                                List<ProductAttrValue> combination,
                                Map<ProductAttr, List<ProductAttrValue>> attrValueMap) {
        StringBuilder skuName = new StringBuilder(productName);
        
        for (ProductAttrValue attrValue : combination) {
            skuName.append("-").append(attrValue.getValue());
        }
        
        // 限制最大长度
        if (skuName.length() > 150) {
            return skuName.substring(0, 150);
        }
        
        return skuName.toString();
    }

    /**
     * 根据属性值ID查找对应的属性
     */
    private ProductAttr findAttrByValueId(Map<ProductAttr, List<ProductAttrValue>> attrValueMap,
                                         Long attrValueId) {
        for (Map.Entry<ProductAttr, List<ProductAttrValue>> entry : attrValueMap.entrySet()) {
            for (ProductAttrValue value : entry.getValue()) {
                if (Objects.equals(value.getId(), attrValueId)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    /**
     * 计算SKU数量
     */
    private long calculateSkuCount(Map<ProductAttr, List<ProductAttrValue>> attrValueMap) {
        long count = 1;
        for (List<ProductAttrValue> values : attrValueMap.values()) {
            count *= values.size();
        }
        return count;
    }

    /**
     * 增量更新商品SKU（不删除已有SKU，只增加新的或更新名称）
     *
     * <p>核心策略：</p>
     * <ul>
     *   <li>对于没有变化的属性和属性值，保留原有SKU（SKU ID不变）</li>
     *   <li>对于新增的属性值组合，生成新SKU</li>
     *   <li>对于已删除的属性值组合，逻辑删除对应SKU</li>
     *   <li>对于属性名或属性值名称改变，更新SKU名称</li>
     * </ul>
     *
     * @param productId 商品ID
     * @throws NotFoundException 当商品不存在时抛出
     * @throws BusinessException 当商品无属性时抛出
     */
    @Transactional
    public void incrementalUpdateSkus(Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new NotFoundException(BusinessCode.PRODUCT_NOT_FOUND));

        // 1. 查询商品所有有效属性
        List<ProductAttr> attrs = attrRepository.findByProductIdAndStatusOrderBySortOrderAsc(
            productId, Status.ACTIVE);

        if (CollUtil.isEmpty(attrs)) {
            // 如果没有属性，删除所有SKU
            List<ProductSku> existingSkus = skuRepository.findByProductIdAndStatusOrderBySortOrderAsc(
                productId, Status.ACTIVE);
            for (ProductSku sku : existingSkus) {
                sku.setStatus(Status.INACTIVE);
                skuRepository.save(sku);
            }
            log.info("商品{}没有属性，删除所有SKU", productId);
            return;
        }

        // 2. 查询每个属性的有效属性值
        Map<ProductAttr, List<ProductAttrValue>> attrValueMap = new LinkedHashMap<>();
        for (ProductAttr attr : attrs) {
            List<ProductAttrValue> values = attrValueRepository
                .findByProductAttrIdAndStatusOrderBySortOrderAsc(attr.getId(), Status.ACTIVE);
            if (CollUtil.isNotEmpty(values)) {
                attrValueMap.put(attr, values);
            }
        }

        if (attrValueMap.isEmpty()) {
            // 如果没有属性值，删除所有SKU
            List<ProductSku> existingSkus = skuRepository.findByProductIdAndStatusOrderBySortOrderAsc(
                productId, Status.ACTIVE);
            for (ProductSku sku : existingSkus) {
                sku.setStatus(Status.INACTIVE);
                skuRepository.save(sku);
            }
            log.warn("商品{}没有有效的属性值，删除所有SKU", productId);
            return;
        }

        // 3. 生成应该存在的SKU组合（笛卡尔积）
        List<List<ProductAttrValue>> expectedCombinations = cartesianProduct(
            new ArrayList<>(attrValueMap.values()));

        // 4. 获取现有的SKU及其属性组合
        List<ProductSku> existingSkus = skuRepository.findByProductIdAndStatusOrderBySortOrderAsc(
            productId, Status.ACTIVE);
        
        Map<String, ProductSku> existingSkuMap = new HashMap<>();
        for (ProductSku sku : existingSkus) {
            List<ProductSkuAttr> skuAttrs = skuAttrRepository.findBySkuIdAndStatusOrderBySortOrderAsc(
                sku.getId(), Status.ACTIVE);
            String key = buildSkuKey(skuAttrs);
            existingSkuMap.put(key, sku);
        }

        // 5. 处理应该存在的SKU
        Set<String> processedKeys = new HashSet<>();
        int sortOrder = 0;
        
        for (List<ProductAttrValue> combination : expectedCombinations) {
            String key = buildSkuKeyFromValues(combination);
            processedKeys.add(key);
            
            if (existingSkuMap.containsKey(key)) {
                // SKU已存在，检查是否需要更新名称
                ProductSku existingSku = existingSkuMap.get(key);
                String newSkuName = buildSkuName(product.getName(), combination, attrValueMap);
                
                if (!Objects.equals(existingSku.getSkuName(), newSkuName)) {
                    existingSku.setSkuName(newSkuName);
                    existingSku.setPriceStatus(PriceStatus.UNPRICED);
                    skuRepository.save(existingSku);
                    log.info("更新SKU名称: {} -> {}", existingSku.getId(), newSkuName);
                }
                
                // 更新SKU属性的排序
                existingSku.setSortOrder(sortOrder++);
                skuRepository.save(existingSku);
            } else {
                // 新增SKU
                String skuName = buildSkuName(product.getName(), combination, attrValueMap);
                
                ProductSku sku = new ProductSku();
                sku.setProductId(productId);
                sku.setSkuName(skuName);
                sku.setPriceStatus(PriceStatus.UNPRICED);
                sku.setPrice(product.getPrice());
                sku.setSortOrder(sortOrder++);
                sku.setStatus(Status.ACTIVE);
                sku = skuRepository.save(sku);
                
                // 创建SKU属性关联
                int attrSortOrder = 0;
                for (ProductAttrValue attrValue : combination) {
                    ProductAttr attr = findAttrByValueId(attrValueMap, attrValue.getId());
                    if (attr != null) {
                        ProductSkuAttr skuAttr = new ProductSkuAttr();
                        skuAttr.setSkuId(sku.getId());
                        skuAttr.setProductAttrId(attr.getId());
                        skuAttr.setProductAttrName(attr.getAttrName());
                        skuAttr.setProductAttrValueId(attrValue.getId());
                        skuAttr.setProductAttrValueName(attrValue.getValue());
                        skuAttr.setSortOrder(attrSortOrder++);
                        skuAttr.setStatus(Status.ACTIVE);
                        skuAttrRepository.save(skuAttr);
                    }
                }
                
                log.info("新增SKU: {}", skuName);
            }
        }

        // 6. 删除不应该存在的SKU
        for (Map.Entry<String, ProductSku> entry : existingSkuMap.entrySet()) {
            if (!processedKeys.contains(entry.getKey())) {
                ProductSku sku = entry.getValue();
                sku.setStatus(Status.INACTIVE);
                skuRepository.save(sku);
                log.info("删除SKU: {} ({})", sku.getId(), sku.getSkuName());
            }
        }

        log.info("商品{}增量更新SKU完成", productId);
    }

    /**
     * 根据SKU属性构建SKU唯一键
     */
    private String buildSkuKey(List<ProductSkuAttr> skuAttrs) {
        return skuAttrs.stream()
            .sorted(Comparator.comparing(ProductSkuAttr::getProductAttrId))
            .map(attr -> attr.getProductAttrId() + ":" + attr.getProductAttrValueId())
            .collect(Collectors.joining(","));
    }

    /**
     * 根据属性值列表构建SKU唯一键
     */
    private String buildSkuKeyFromValues(List<ProductAttrValue> values) {
        return values.stream()
            .sorted(Comparator.comparing(ProductAttrValue::getProductAttrId))
            .map(value -> value.getProductAttrId() + ":" + value.getId())
            .collect(Collectors.joining(","));
    }
}
