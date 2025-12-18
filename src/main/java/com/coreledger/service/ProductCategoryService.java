package com.coreledger.service;

import com.coreledger.common.mapper.product.ProductCategoryConverter;
import com.coreledger.utils.AppSessionContext;
import com.coreledger.utils.specification.PredicateBuilder;
import com.coreledger.dto.product.CategoryCreateDTO;
import com.coreledger.dto.product.CategoryUpdateDTO;
import com.coreledger.entity.ProductCategory;
import com.coreledger.enums.BusinessCode;
import com.coreledger.enums.Status;
import com.coreledger.exception.BusinessException;
import com.coreledger.exception.NotFoundException;
import com.coreledger.repository.ProductCategoryRepository;
import com.coreledger.repository.ProductRepository;
import com.coreledger.vo.product.CategoryTreeVO;
import com.coreledger.vo.product.CategoryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 商品分类Service
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductCategoryService {

    private final ProductCategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductCategoryConverter categoryConverter;

    /** 最大分类层级 */
    private static final int MAX_LEVEL = 5;

    /**
     * 创建商品分类
     *
     * @param dto 创建DTO
     * @return 分类VO
     * @throws BusinessException 当父分类不存在或层级超限时抛出
     * @throws BusinessException 当商户ID为空时抛出
     */
    @Transactional
    public CategoryVO createCategory(CategoryCreateDTO dto) {
        // 获取当前商户ID
        Long merchantId = AppSessionContext.getMerchantId();
        if (merchantId == null) {
            throw new BusinessException(BusinessCode.INVALID_PARAMETER, "商户ID不能为空");
        }

        // 计算层级
        int level = 1;
        if (dto.getParentId() > 0) {
            ProductCategory parent = categoryRepository.findById(dto.getParentId())
                .orElseThrow(() -> new NotFoundException(BusinessCode.PRODUCT_CATEGORY_NOT_FOUND));
            
            // 验证父分类属于同一商户
            if (!parent.getMerchantId().equals(merchantId)) {
                throw new BusinessException(BusinessCode.UNAUTHORIZED_OPERATION, "无权访问其他商户的分类");
            }
            
            level = parent.getLevel() + 1;
            
            // 检查层级限制
            if (level > MAX_LEVEL) {
                throw new BusinessException(BusinessCode.PRODUCT_CATEGORY_LEVEL_EXCEED);
            }
        }

        // 创建分类
        ProductCategory category = categoryConverter.toEntity(dto);
        category.setMerchantId(merchantId);
        category.setLevel(level);
        category.setStatus(Status.ACTIVE);

        category = categoryRepository.save(category);
        
        log.info("创建商品分类成功, ID: {}, 名称: {}, 层级: {}, 商户ID: {}", category.getId(), category.getName(), level, merchantId);
        return categoryConverter.toVO(category);
    }

    /**
     * 修改商品分类
     *
     * @param id 分类ID
     * @param dto 更新DTO
     * @return 分类VO
     * @throws NotFoundException 当分类不存在时抛出
     * @throws BusinessException 当商户ID为空或无权访问时抛出
     */
    @Transactional
    public CategoryVO updateCategory(Long id, CategoryUpdateDTO dto) {
        ProductCategory category = getAndCheckCategory(id);

        categoryConverter.updateEntity(dto, category);
        category = categoryRepository.save(category);
        
        log.info("修改商品分类成功, ID: {}, 名称: {}", id, category.getName());
        return categoryConverter.toVO(category);
    }

    /**
     * 删除商品分类
     *
     * @param id 分类ID
     * @throws NotFoundException 当分类不存在时抛出
     * @throws BusinessException 当分类下存在子分类或商品时抛出
     * @throws BusinessException 当商户ID为空或无权访问时抛出
     */
    @Transactional
    public void deleteCategory(Long id) {
        ProductCategory category = getAndCheckCategory(id);


        // 检查是否有子分类
        long childCount = categoryRepository.countByParentIdAndStatus(id, Status.ACTIVE);
        if (childCount > 0) {
            throw new BusinessException(BusinessCode.PRODUCT_CATEGORY_HAS_CHILDREN);
        }

        // 检查是否有关联商品
        long productCount = productRepository.countByCategoryIdAndStatus(id, Status.ACTIVE);
        if (productCount > 0) {
            throw new BusinessException(BusinessCode.PRODUCT_CATEGORY_HAS_PRODUCTS);
        }

        // 逻辑删除
        category.setStatus(Status.INACTIVE);
        categoryRepository.save(category);
        
        log.info("删除商品分类成功, ID: {}, 名称: {}", id, category.getName());
    }

    /**
     * 获取分类详情
     *
     * @param id 分类ID
     * @return 分类VO
     * @throws NotFoundException 当分类不存在时抛出
     * @throws BusinessException 当商户ID为空或无权访问时抛出
     */
    public CategoryVO getCategory(Long id) {
        // 获取当前商户ID
        ProductCategory category = getAndCheckCategory(id);

        return categoryConverter.toVO(category);
    }

    private @NotNull ProductCategory getAndCheckCategory(Long id) {
        Long merchantId = AppSessionContext.getMerchantId();
        if (merchantId == null) {
            throw new BusinessException(BusinessCode.INVALID_PARAMETER, "商户ID不能为空");
        }

        ProductCategory category = categoryRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(BusinessCode.PRODUCT_CATEGORY_NOT_FOUND));

        // 验证分类属于当前商户
        if (!category.getMerchantId().equals(merchantId)) {
            throw new BusinessException(BusinessCode.UNAUTHORIZED_OPERATION, "无权访问其他商户的分类");
        }
        return category;
    }

    /**
     * 获取分类树
     *
     * @return 分类树
     */
    public List<CategoryTreeVO> getCategoryTree() {
        List<ProductCategory> allCategories = categoryRepository.findAll(
                PredicateBuilder.<ProductCategory>and()
                        .equal("merchantId", AppSessionContext.getMerchantId())
                        .equal("status", Status.ACTIVE)
                        .build()
        );

        // 构建树形结构
        return buildTree(allCategories, 0L);
    }


    /**
     * 构建分类树
     *
     * @param allCategories 所有分类
     * @return 分类树
     */
    private List<CategoryTreeVO> buildTree(List<ProductCategory> allCategories, Long rootParentId) {
        if (allCategories == null || allCategories.isEmpty()) {
            return Collections.emptyList();
        }

        // 构建所有节点的Map
        Map<Long, CategoryTreeVO> nodeMap = new HashMap<>();
        Map<Long, List<CategoryTreeVO>> childrenMap = new HashMap<>();

        // 第一遍遍历：创建所有节点并分组子节点
        for (ProductCategory category : allCategories) {
            CategoryTreeVO node = categoryConverter.toTreeVO(category);
            nodeMap.put(category.getId(), node);

            Long parentId = category.getParentId();
            if (parentId == null) {
                parentId = rootParentId; // 使用传入的根parentId
            }
            childrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(node);
        }

        // 第二遍遍历：构建树结构
        List<CategoryTreeVO> rootNodes = childrenMap.get(rootParentId);
        if (rootNodes == null) {
            return Collections.emptyList();
        }

        for (CategoryTreeVO node : rootNodes) {
            buildChildren(node, childrenMap);
        }

        return rootNodes;
    }

    private void buildChildren(CategoryTreeVO parent, Map<Long, List<CategoryTreeVO>> childrenMap) {
        List<CategoryTreeVO> children = childrenMap.get(parent.getId());
        if (children != null) {
            parent.setChildren(children);
            for (CategoryTreeVO child : children) {
                buildChildren(child, childrenMap);
            }
        } else {
            parent.setChildren(Collections.emptyList());
        }
    }
}
