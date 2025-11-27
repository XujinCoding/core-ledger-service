package com.coreledger.service;

import cn.hutool.core.util.StrUtil;
import com.coreledger.common.mapper.product.ProductCategoryConverter;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
     */
    @Transactional
    public CategoryVO createCategory(CategoryCreateDTO dto) {
        // 计算层级
        int level = 1;
        if (dto.getParentId() > 0) {
            ProductCategory parent = categoryRepository.findById(dto.getParentId())
                .orElseThrow(() -> new NotFoundException(BusinessCode.PRODUCT_CATEGORY_NOT_FOUND));
            
            level = parent.getLevel() + 1;
            
            // 检查层级限制
            if (level > MAX_LEVEL) {
                throw new BusinessException(BusinessCode.PRODUCT_CATEGORY_LEVEL_EXCEED);
            }
        }

        // 创建分类
        ProductCategory category = categoryConverter.toEntity(dto);
        category.setLevel(level);
        category.setStatus(Status.ACTIVE);

        category = categoryRepository.save(category);
        
        log.info("创建商品分类成功, ID: {}, 名称: {}, 层级: {}", category.getId(), category.getName(), level);
        return categoryConverter.toVO(category);
    }

    /**
     * 修改商品分类
     *
     * @param id 分类ID
     * @param dto 更新DTO
     * @return 分类VO
     * @throws NotFoundException 当分类不存在时抛出
     */
    @Transactional
    public CategoryVO updateCategory(Long id, CategoryUpdateDTO dto) {
        ProductCategory category = categoryRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(BusinessCode.PRODUCT_CATEGORY_NOT_FOUND));

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
     */
    @Transactional
    public void deleteCategory(Long id) {
        ProductCategory category = categoryRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(BusinessCode.PRODUCT_CATEGORY_NOT_FOUND));

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
     */
    public CategoryVO getCategory(Long id) {
        ProductCategory category = categoryRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(BusinessCode.PRODUCT_CATEGORY_NOT_FOUND));
        
        return categoryConverter.toVO(category);
    }

    /**
     * 获取分类列表（支持分页和父分类筛选）
     *
     * @param parentId 父分类ID（可选）
     * @param pageable 分页参数
     * @return 分类列表
     */
    public Page<CategoryVO> listCategories(Long parentId, Pageable pageable) {
        Specification<ProductCategory> spec = PredicateBuilder.<ProductCategory>and()
            .equal("status", Status.ACTIVE)
            .equal(Objects::nonNull, "parentId", parentId)
            .build();

        return categoryRepository.findAll(spec, pageable)
            .map(categoryConverter::toVO);
    }

    /**
     * 获取分类树
     *
     * @return 分类树
     */
    public List<CategoryTreeVO> getCategoryTree() {
        List<ProductCategory> allCategories = categoryRepository.findAll(
            PredicateBuilder.<ProductCategory>and()
                .equal("status", Status.ACTIVE)
                .build()
        );

        // 构建树形结构
        return buildTree(allCategories, 0L);
    }

    /**
     * 获取子分类列表
     *
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    public List<CategoryVO> getChildren(Long parentId) {
        List<ProductCategory> children = categoryRepository.findByParentIdAndStatus(parentId, Status.ACTIVE);
        return children.stream()
            .map(categoryConverter::toVO)
            .collect(Collectors.toList());
    }

    /**
     * 移动分类（修改父分类）
     *
     * @param id 分类ID
     * @param newParentId 新父分类ID
     * @return 分类VO
     * @throws NotFoundException 当分类或父分类不存在时抛出
     * @throws BusinessException 当层级超限时抛出
     */
    @Transactional
    public CategoryVO moveCategory(Long id, Long newParentId) {
        ProductCategory category = categoryRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(BusinessCode.PRODUCT_CATEGORY_NOT_FOUND));

        // 计算新层级
        int newLevel = 1;
        if (newParentId > 0) {
            ProductCategory newParent = categoryRepository.findById(newParentId)
                .orElseThrow(() -> new NotFoundException(BusinessCode.PRODUCT_CATEGORY_NOT_FOUND));
            
            newLevel = newParent.getLevel() + 1;
            
            // 检查层级限制
            if (newLevel > MAX_LEVEL) {
                throw new BusinessException(BusinessCode.PRODUCT_CATEGORY_LEVEL_EXCEED);
            }
        }

        // 更新分类
        category.setParentId(newParentId);
        category.setLevel(newLevel);
        category = categoryRepository.save(category);

        // 递归更新所有子分类的层级
        updateChildrenLevel(id);
        
        log.info("移动商品分类成功, ID: {}, 新父分类ID: {}, 新层级: {}", id, newParentId, newLevel);
        return categoryConverter.toVO(category);
    }

    /**
     * 启用/禁用分类
     *
     * @param id 分类ID
     * @param status 状态
     * @return 分类VO
     * @throws NotFoundException 当分类不存在时抛出
     */
    @Transactional
    public CategoryVO updateStatus(Long id, Status status) {
        ProductCategory category = categoryRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(BusinessCode.PRODUCT_CATEGORY_NOT_FOUND));

        category.setStatus(status);
        category = categoryRepository.save(category);
        
        log.info("更新商品分类状态成功, ID: {}, 状态: {}", id, status);
        return categoryConverter.toVO(category);
    }

    /**
     * 构建分类树
     *
     * @param allCategories 所有分类
     * @param parentId 父分类ID
     * @return 分类树
     */
    private List<CategoryTreeVO> buildTree(List<ProductCategory> allCategories, Long parentId) {
        List<CategoryTreeVO> tree = new ArrayList<>();
        
        for (ProductCategory category : allCategories) {
            if (Objects.equals(category.getParentId(), parentId)) {
                CategoryTreeVO node = categoryConverter.toTreeVO(category);
                // 递归查找子节点
                node.setChildren(buildTree(allCategories, category.getId()));
                tree.add(node);
            }
        }
        
        return tree;
    }

    /**
     * 递归更新子分类层级
     *
     * @param parentId 父分类ID
     */
    private void updateChildrenLevel(Long parentId) {
        List<ProductCategory> children = categoryRepository.findByParentIdAndStatus(parentId, Status.ACTIVE);
        
        for (ProductCategory child : children) {
            ProductCategory parent = categoryRepository.findById(child.getParentId())
                .orElseThrow(() -> new NotFoundException(BusinessCode.PRODUCT_CATEGORY_NOT_FOUND));
            
            child.setLevel(parent.getLevel() + 1);
            categoryRepository.save(child);
            
            // 递归更新子节点
            updateChildrenLevel(child.getId());
        }
    }
}
