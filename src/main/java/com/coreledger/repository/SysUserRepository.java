package com.coreledger.repository;

import com.coreledger.entity.SysUser;
import com.coreledger.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 系统用户 Repository
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Repository
public interface SysUserRepository extends JpaRepository<SysUser, Long> {

    /**
     * 根据ID和状态查询用户
     *
     * @param id 用户ID
     * @param status 状态
     * @return 用户
     */
    Optional<SysUser> findByIdAndStatus(Long id, Status status);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户
     */
    Optional<SysUser> findByUsername(String username);


    /**
     * 根据微信OpenID查询用户
     *
     * @param wxOpenid 微信OpenID
     * @return 用户
     */
    Optional<SysUser> findByWxOpenid(String wxOpenid);

    /**
     * 根据用户名和状态查询用户
     *
     * @param username 用户名
     * @param status 状态
     * @return 用户
     */
    Optional<SysUser> findByUsernameAndStatus(String username, Status status);


    /**
     * 根据微信OpenID和状态查询用户
     *
     * @param wxOpenid 微信OpenID
     * @param status 状态
     * @return 用户
     */
    Optional<SysUser> findByWxOpenidAndStatus(String wxOpenid, Status status);


    /**
     * 检查微信OpenID是否存在
     *
     * @param wxOpenid 微信OpenID
     * @param status 状态
     * @return true=存在, false=不存在
     */
    boolean existsByWxOpenidAndStatus(String wxOpenid, Status status);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @param status 状态
     * @return true=存在, false=不存在
     */
    boolean existsByUsernameAndStatus(String username, Status status);
}
