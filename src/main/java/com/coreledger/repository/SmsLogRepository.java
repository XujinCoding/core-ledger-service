package com.coreledger.repository;

import com.coreledger.entity.SmsLog;
import com.coreledger.enums.SmsScene;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * 短信发送记录 Repository
 */
@Repository
public interface SmsLogRepository extends JpaRepository<SmsLog, Long> {

    /**
     * 统计指定手机号在指定时间之后的发送次数
     */
    @Query("SELECT COUNT(s) FROM SmsLog s WHERE s.phone = :phone AND s.createInstant >= :startTime")
    long countByPhoneAndCreateInstantAfter(@Param("phone") String phone, @Param("startTime") LocalDateTime startTime);

    /**
     * 查询最近一条发送记录
     */
    SmsLog findFirstByPhoneAndSceneOrderByCreateInstantDesc(String phone, SmsScene scene);
}
