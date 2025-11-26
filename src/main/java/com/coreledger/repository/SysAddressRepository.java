package com.coreledger.repository;

import com.coreledger.entity.SysAddress;
import com.coreledger.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 地址库 Repository
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Repository
public interface SysAddressRepository extends JpaRepository<SysAddress, Long> {

    /**
     * 根据ID和状态查询地址
     *
     * @param id 地址ID
     * @param status 状态 (1=有效, 0=删除)
     * @return 地址
     */
    Optional<SysAddress> findByIdAndStatus(Long id, Status status);

    /**
     * 根据父级ID查询子级地址
     *
     * @param parentId 父级ID
     * @param status 状态 (1=有效)
     * @return 地址列表
     */
    List<SysAddress> findByParentIdAndStatus(Long parentId, Status status);

    /**
     * 根据层级查询地址
     *
     * @param level 层级 (1=省, 2=市, 3=区县, 4=镇/乡, 5=村)
     * @param status 状态 (1=有效)
     * @return 地址列表
     */
    List<SysAddress> findByLevelAndStatus(Integer level, Status status);
}
