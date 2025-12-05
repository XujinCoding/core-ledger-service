package com.coreledger.service;

import com.coreledger.common.mapper.product.ProductAttrConverter;
import com.coreledger.common.mapper.product.ProductAttrValueConverter;
import com.coreledger.dto.product.ProductAttrBatchUpdateDTO;
import com.coreledger.dto.product.ProductAttrCreateDTO;
import com.coreledger.dto.product.ProductAttrValueCreateDTO;
import com.coreledger.entity.ProductAttr;
import com.coreledger.entity.ProductAttrValue;
import com.coreledger.enums.BusinessCode;
import com.coreledger.enums.Status;
import com.coreledger.exception.BusinessException;
import com.coreledger.exception.NotFoundException;
import com.coreledger.repository.ProductAttrRepository;
import com.coreledger.repository.ProductAttrValueRepository;
import com.coreledger.repository.ProductSkuAttrRepository;
import com.coreledger.vo.product.ProductAttrVO;
import com.coreledger.vo.product.ProductAttrValueVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品属性Service
 *
 * <p>核心功能：</p>
 * <ul>
 *   <li>属性和属性值的CRUD管理</li>
 *   <li>触发SKU自动生成/更新</li>
 * </ul>
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductAttrService {

    private final ProductAttrRepository attrRepository;
    private final ProductAttrValueRepository attrValueRepository;
    private final ProductSkuAttrRepository skuAttrRepository;
    private final ProductAttrConverter attrConverter;
    private final ProductAttrValueConverter attrValueConverter;
    private final ProductSkuService skuService;


    /**
     * 获取商品所有属性及其值
     *
     * @param productId 商品ID
     * @return 属性列表
     */
    public List<ProductAttrVO> getProductAttrs(Long productId) {
        List<ProductAttr> attrs = attrRepository.findByProductIdAndStatusOrderBySortOrderAsc(
            productId, Status.ACTIVE);

        return attrs.stream()
            .map(attr -> {
                ProductAttrVO vo = attrConverter.toVO(attr);
                // 加载属性值列表
                List<ProductAttrValueVO> values = attrValueRepository
                    .findByProductAttrIdAndStatusOrderBySortOrderAsc(attr.getId(), Status.ACTIVE)
                    .stream()
                    .map(attrValueConverter::toVO)
                    .collect(Collectors.toList());
                vo.setValues(values);
                return vo;
            })
            .collect(Collectors.toList());
    }

    /**
     * 批量更新商品属性和属性值（智能识别新增、修改、删除）
     *
     * <p>核心逻辑：</p>
     * <ul>
     *   <li>新增：ID为null</li>
     *   <li>修改：ID存在且数据有变化</li>
     *   <li>删除：数据库中存在但未传入</li>
     *   <li>不变：ID存在且数据未变化</li>
     * </ul>
     *
     * @param productId 商品ID
     * @param dto 批量更新DTO
     * @return 属性列表
     */
    @Transactional
    public List<ProductAttrVO> batchUpdateAttrs(Long productId, ProductAttrBatchUpdateDTO dto) {
        // 1. 获取数据库中现有的所有属性和属性值
        List<ProductAttr> existingAttrs = attrRepository.findByProductIdAndStatusOrderBySortOrderAsc(
            productId, Status.ACTIVE);
        
        Map<Long, ProductAttr> existingAttrMap = existingAttrs.stream()
            .collect(Collectors.toMap(ProductAttr::getId, attr -> attr));

        //TODO 不要在循环里进行数据库查询, 统一查询之后再进行组织
        Map<Long, List<ProductAttrValue>> existingValueMap = new HashMap<>();
        for (ProductAttr attr : existingAttrs) {
            List<ProductAttrValue> values = attrValueRepository
                .findByProductAttrIdAndStatusOrderBySortOrderAsc(attr.getId(), Status.ACTIVE);
            existingValueMap.put(attr.getId(), values);
        }

        // 2. 收集传入的ID
        Set<Long> submittedAttrIds = new HashSet<>();
        Map<Long, Set<Long>> submittedValueIds = new HashMap<>();
        
        for (ProductAttrBatchUpdateDTO.AttrItem attrItem : dto.getAttrs()) {
            if (attrItem.getId() != null) {
                submittedAttrIds.add(attrItem.getId());
                Set<Long> valueIds = attrItem.getValues().stream()
                    .map(ProductAttrBatchUpdateDTO.AttrValueItem::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
                submittedValueIds.put(attrItem.getId(), valueIds);
            }
        }

        // 3. 识别需要删除的属性（数据库中存在但未传入）
        Set<Long> attrIdsToDelete = new HashSet<>(existingAttrMap.keySet());
        attrIdsToDelete.removeAll(submittedAttrIds);
        
        // 删除属性
        for (Long attrId : attrIdsToDelete) {
            ProductAttr attr = existingAttrMap.get(attrId);
            attr.setStatus(Status.INACTIVE);
            attrRepository.save(attr);
            
            // 删除该属性的所有属性值
            List<ProductAttrValue> values = existingValueMap.get(attrId);
            if (values != null) {
                values.forEach(value -> {
                    value.setStatus(Status.INACTIVE);
                    attrValueRepository.save(value);
                });
            }
            log.info("删除属性: {}", attr.getAttrName());
        }

        // 4. 处理传入的属性
        boolean hasChanges = !attrIdsToDelete.isEmpty();
        
        for (ProductAttrBatchUpdateDTO.AttrItem attrItem : dto.getAttrs()) {
            ProductAttr attr;
            
            if (attrItem.getId() == null) {
                // 新增属性
                attr = new ProductAttr();
                attr.setProductId(productId);
                attr.setAttrName(attrItem.getAttrName());
                attr.setSortOrder(attrItem.getSortOrder());
                attr.setStatus(Status.ACTIVE);
                attr = attrRepository.save(attr);
                log.info("新增属性: {}", attr.getAttrName());
                hasChanges = true;
            } else {
                // 修改属性
                attr = existingAttrMap.get(attrItem.getId());
                if (attr == null) {
                    throw new NotFoundException(BusinessCode.PRODUCT_ATTR_NOT_FOUND);
                }
                
                // 检查是否有变化
                boolean attrChanged = !Objects.equals(attr.getAttrName(), attrItem.getAttrName()) ||
                                     !Objects.equals(attr.getSortOrder(), attrItem.getSortOrder());
                
                if (attrChanged) {
                    attr.setAttrName(attrItem.getAttrName());
                    attr.setSortOrder(attrItem.getSortOrder());
                    attr = attrRepository.save(attr);
                    log.info("修改属性: {}", attr.getAttrName());
                    hasChanges = true;
                }
            }

            // 处理属性值
            Long attrId = attr.getId();
            List<ProductAttrValue> existingValues = existingValueMap.getOrDefault(attrId, new ArrayList<>());
            Map<Long, ProductAttrValue> existingValueMap2 = existingValues.stream()
                .collect(Collectors.toMap(ProductAttrValue::getId, value -> value));
            
            Set<Long> submittedValueIdSet = submittedValueIds.getOrDefault(attrId, new HashSet<>());
            
            // 识别需要删除的属性值
            Set<Long> valueIdsToDelete = new HashSet<>(existingValueMap2.keySet());
            valueIdsToDelete.removeAll(submittedValueIdSet);
            
            // 删除属性值
            for (Long valueId : valueIdsToDelete) {
                ProductAttrValue value = existingValueMap2.get(valueId);
                value.setStatus(Status.INACTIVE);
                attrValueRepository.save(value);
                log.info("删除属性值: {} - {}", attr.getAttrName(), value.getValue());
                hasChanges = true;
            }
            
            // 处理传入的属性值
            for (ProductAttrBatchUpdateDTO.AttrValueItem valueItem : attrItem.getValues()) {
                if (valueItem.getId() == null) {
                    // 新增属性值
                    ProductAttrValue value = new ProductAttrValue();
                    value.setProductAttrId(attrId);
                    value.setValue(valueItem.getValue());
                    value.setSortOrder(valueItem.getSortOrder());
                    value.setStatus(Status.ACTIVE);
                    attrValueRepository.save(value);
                    log.info("新增属性值: {} - {}", attr.getAttrName(), value.getValue());
                    hasChanges = true;
                } else {
                    // 修改属性值
                    ProductAttrValue value = existingValueMap2.get(valueItem.getId());
                    if (value == null) {
                        throw new NotFoundException(BusinessCode.PRODUCT_ATTR_VALUE_NOT_FOUND);
                    }
                    
                    // 检查是否有变化
                    boolean valueChanged = !Objects.equals(value.getValue(), valueItem.getValue()) ||
                                          !Objects.equals(value.getSortOrder(), valueItem.getSortOrder());
                    
                    if (valueChanged) {
                        value.setValue(valueItem.getValue());
                        value.setSortOrder(valueItem.getSortOrder());
                        attrValueRepository.save(value);
                        log.info("修改属性值: {} - {}", attr.getAttrName(), value.getValue());
                        hasChanges = true;
                    }
                }
            }
        }

        // 5. 如果有变化，则增量更新SKU
        if (hasChanges) {
            try {
                skuService.incrementalUpdateSkus(productId);
                log.info("批量更新属性成功并增量更新SKU, 商品ID: {}", productId);
            } catch (BusinessException e) {
                log.warn("批量更新属性成功，但增量更新SKU失败: {}", e.getMessage());
            }
        } else {
            log.info("属性和属性值无变化，跳过SKU更新, 商品ID: {}", productId);
        }

        // 6. 返回最新的属性列表
        return getProductAttrs(productId);
    }
}
