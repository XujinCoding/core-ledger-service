package com.coreledger.repository;

import com.coreledger.entity.UserMerchantRelation;
import com.coreledger.enums.Identity;
import com.coreledger.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户-商户关系 Repository
 *
 * @author Core Ledger Team
 * @since 2.0.0
 */
@Repository
public interface UserMerchantRelationRepository extends JpaRepository<UserMerchantRelation, Long> {

    /**
     * 查询用户的所有关系
     *
     * @param userId 用户ID
     * @param status 状态
     * @return 关系列表
     */
    List<UserMerchantRelation> findByUserIdAndStatus(Long userId, Status status);

    /**
     * 查询商户的特定身份用户
     *
     * @param merchantId 商户ID
     * @param identity   身份
     * @param status     状态
     * @return 关系列表
     */
    List<UserMerchantRelation> findByMerchantIdAndIdentityAndStatus(
            Long merchantId, Identity identity, Status status);

    /**
     * 查询用户在商户中的特定身份关系
     *
     * @param userId     用户ID
     * @param merchantId 商户ID
     * @param identity   身份
     * @param status     状态
     * @return 关系
     */
    Optional<UserMerchantRelation> findByUserIdAndMerchantIdAndIdentityAndStatus(
            Long userId, Long merchantId, Identity identity, Status status);

    /**
     * 检查关系是否存在
     *
     * @param userId     用户ID
     * @param merchantId 商户ID
     * @param identity   身份
     * @param status     状态
     * @return 是否存在
     */
    boolean existsByUserIdAndMerchantIdAndIdentityAndStatus(
            Long userId, Long merchantId, Identity identity, Status status);

    /**
     * 查询用户作为特定身份的所有商户关系
     *
     * @param userId   用户ID
     * @param identity 身份
     * @param status   状态
     * @return 关系列表
     */
    List<UserMerchantRelation> findByUserIdAndIdentityAndStatus(
            Long userId, Identity identity, Status status);
}
