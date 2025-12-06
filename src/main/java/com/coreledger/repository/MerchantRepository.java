package com.coreledger.repository;

import com.coreledger.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 商户仓储
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Long> {

    /**
     * 根据商户编号查询
     */
    Optional<Merchant> findByMerchantNo(String merchantNo);

    /**
     * 根据邀请码查询
     */
    Optional<Merchant> findByInviteCode(String inviteCode);

    /**
     * 根据所有者ID查询所有商户
     */
    List<Merchant> findByOwnerUserId(Long ownerUserId);

    /**
     * 根据所有者ID和状态查询
     */
    List<Merchant> findByOwnerUserIdAndStatus(Long ownerUserId, Integer status);

    /**
     * 检查商户编号是否存在
     */
    boolean existsByMerchantNo(String merchantNo);

    /**
     * 检查邀请码是否存在
     */
    boolean existsByInviteCode(String inviteCode);
}
