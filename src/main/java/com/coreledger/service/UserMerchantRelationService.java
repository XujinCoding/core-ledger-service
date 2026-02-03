package com.coreledger.service;

import com.coreledger.entity.UserMerchantRelation;
import com.coreledger.enums.Identity;
import com.coreledger.enums.Status;
import com.coreledger.repository.UserMerchantRelationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户-商户关系服务
 *
 * @author Core Ledger Team
 * @since 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserMerchantRelationService {

    private final UserMerchantRelationRepository relationRepository;

    /**
     * 创建用户-商户关系
     *
     * @param userId     用户ID
     * @param merchantId 商户ID
     * @param identity   身份
     * @return 关系实体
     */
    @Transactional(rollbackFor = Exception.class)
    public UserMerchantRelation createRelation(Long userId, Long merchantId, Identity identity) {
        // 检查关系是否已存在
        Optional<UserMerchantRelation> existing = relationRepository
                .findByUserIdAndMerchantIdAndIdentityAndStatus(userId, merchantId, identity, Status.ACTIVE);

        if (existing.isPresent()) {
            log.info("用户-商户关系已存在: userId={}, merchantId={}, identity={}", userId, merchantId, identity);
            return existing.get();
        }

        UserMerchantRelation relation = new UserMerchantRelation();
        relation.setUserId(userId);
        relation.setMerchantId(merchantId);
        relation.setIdentity(identity);
        relation.setBindTime(LocalDateTime.now());
        relation.setStatus(Status.ACTIVE);

        UserMerchantRelation saved = relationRepository.save(relation);
        log.info("创建用户-商户关系成功: userId={}, merchantId={}, identity={}, relationId={}",
                userId, merchantId, identity, saved.getId());

        return saved;
    }

    /**
     * 查询用户在特定身份下的所有商户ID列表
     *
     * @param userId   用户ID
     * @param identity 身份
     * @return 商户ID列表
     */
    public List<Long> findMerchantIdsByUserIdAndIdentity(Long userId, Identity identity) {
        List<UserMerchantRelation> relations = relationRepository
                .findByUserIdAndIdentityAndStatus(userId, identity, Status.ACTIVE);

        return relations.stream()
                .map(UserMerchantRelation::getMerchantId)
                .toList();
    }

    /**
     * 查询用户在特定身份下的所有关系
     *
     * @param userId   用户ID
     * @param identity 身份
     * @return 关系列表
     */
    public List<UserMerchantRelation> findByUserIdAndIdentity(Long userId, Identity identity) {
        return relationRepository.findByUserIdAndIdentityAndStatus(userId, identity, Status.ACTIVE);
    }

    /**
     * 查询商户的特定身份用户关系列表
     *
     * @param merchantId 商户ID
     * @param identity   身份
     * @return 关系列表
     */
    public List<UserMerchantRelation> findByMerchantIdAndIdentity(Long merchantId, Identity identity) {
        return relationRepository.findByMerchantIdAndIdentityAndStatus(merchantId, identity, Status.ACTIVE);
    }

    /**
     * 检查用户-商户关系是否存在
     *
     * @param userId     用户ID
     * @param merchantId 商户ID
     * @param identity   身份
     * @return 是否存在
     */
    public boolean checkRelationExists(Long userId, Long merchantId, Identity identity) {
        return relationRepository.existsByUserIdAndMerchantIdAndIdentityAndStatus(
                userId, merchantId, identity, Status.ACTIVE);
    }

    /**
     * 查询用户在商户中的特定身份关系
     *
     * @param userId     用户ID
     * @param merchantId 商户ID
     * @param identity   身份
     * @return 关系
     */
    public Optional<UserMerchantRelation> findByUserIdAndMerchantIdAndIdentity(
            Long userId, Long merchantId, Identity identity) {
        return relationRepository.findByUserIdAndMerchantIdAndIdentityAndStatus(
                userId, merchantId, identity, Status.ACTIVE);
    }

    /**
     * 查询用户的所有关系
     *
     * @param userId 用户ID
     * @return 关系列表
     */
    public List<UserMerchantRelation> findByUserId(Long userId) {
        return relationRepository.findByUserIdAndStatus(userId, Status.ACTIVE);
    }
}
