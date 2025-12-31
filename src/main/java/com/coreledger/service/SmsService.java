package com.coreledger.service;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.coreledger.entity.SmsLog;
import com.coreledger.enums.BusinessCode;
import com.coreledger.enums.SmsSendStatus;
import com.coreledger.enums.SmsScene;
import com.coreledger.exception.BusinessException;
import com.coreledger.repository.SmsLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

/**
 * 短信服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {

    private final SmsLogRepository smsLogRepository;
    private final StringRedisTemplate stringRedisTemplate;

    @Value("${sms.api-url:https://push.spug.cc/send/Dn6ywmOPAMj17lod}")
    private String smsApiUrl;

    @Value("${sms.expire-minutes:5}")
    private int expireMinutes;

    @Value("${sms.daily-limit:10}")
    private int dailyLimit;

    @Value("${sms.interval-seconds:60}")
    private int intervalSeconds;

    @Value("${sms.verify-max-attempts:3}")
    private int verifyMaxAttempts;

    // Redis Key 前缀
    private static final String SMS_CODE_KEY = "sms:code:%s:%s";           // sms:code:{scene}:{phone}
    private static final String SMS_LIMIT_KEY = "sms:limit:phone:%s";      // sms:limit:phone:{phone}
    private static final String SMS_LOCK_KEY = "sms:lock:%s:%s";           // sms:lock:{scene}:{phone}
    private static final String SMS_VERIFY_FAIL_KEY = "sms:verify:fail:%s:%s"; // sms:verify:fail:{scene}:{phone}

    /**
     * 发送短信验证码
     */
    @Transactional
    public void sendSmsCode(String phone, SmsScene scene) {
        // 1. 检查发送间隔（60秒限制）
        String lockKey = String.format(SMS_LOCK_KEY, scene.name(), phone);
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(lockKey))) {
            throw new BusinessException(BusinessCode.SMS_SEND_TOO_FREQUENT);
        }

        // 2. 检查每日发送次数
        String limitKey = String.format(SMS_LIMIT_KEY, phone);
        String countStr = stringRedisTemplate.opsForValue().get(limitKey);
        int count = countStr == null ? 0 : Integer.parseInt(countStr);
        if (count >= dailyLimit) {
            throw new BusinessException(BusinessCode.SMS_SEND_LIMIT_EXCEEDED);
        }

        // 3. 生成验证码
        String code = RandomUtil.randomNumbers(6);

        // 4. 创建发送记录
        SmsLog smsLog = new SmsLog();
        smsLog.setPhone(phone);
        smsLog.setCode(code);
        smsLog.setScene(scene);
        smsLog.setSendStatus(SmsSendStatus.PENDING);

        // 5. 调用第三方发送
        try {
            String url = String.format("%s?code=%s&number=10&targets=%s", smsApiUrl, code, phone);
            HttpResponse response = HttpRequest.get(url)
                    .timeout(10000)
                    .execute();

            String body = response.body();
            smsLog.setResponseMsg(body);

            if (response.isOk()) {
                smsLog.setSendStatus(SmsSendStatus.SUCCESS);

                // 6. 存储验证码到Redis
                String codeKey = String.format(SMS_CODE_KEY, scene.name(), phone);
                stringRedisTemplate.opsForValue().set(codeKey, code, expireMinutes, TimeUnit.MINUTES);

                // 7. 设置发送间隔锁
                stringRedisTemplate.opsForValue().set(lockKey, "1", intervalSeconds, TimeUnit.SECONDS);

                // 8. 增加每日发送计数
                stringRedisTemplate.opsForValue().increment(limitKey);
                // 设置过期时间到当天结束
                if (count == 0) {
                    LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
                    long secondsUntilEndOfDay = Duration.between(LocalDateTime.now(), endOfDay).getSeconds();
                    stringRedisTemplate.expire(limitKey, secondsUntilEndOfDay, TimeUnit.SECONDS);
                }

                log.info("短信发送成功: phone={}, scene={}", phone, scene);
            } else {
                smsLog.setSendStatus(SmsSendStatus.FAILED);
                log.error("短信发送失败: phone={}, response={}", phone, body);
                throw new BusinessException(BusinessCode.SMS_SEND_FAILED);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            smsLog.setSendStatus(SmsSendStatus.FAILED);
            smsLog.setResponseMsg(e.getMessage());
            log.error("短信发送异常: phone={}", phone, e);
            throw new BusinessException(BusinessCode.SMS_SEND_FAILED);
        } finally {
            smsLogRepository.save(smsLog);
        }
    }

    /**
     * 验证短信验证码
     */
    public boolean verifySmsCode(String phone, String code, SmsScene scene) {
        // 1. 检查验证失败次数
        String failKey = String.format(SMS_VERIFY_FAIL_KEY, scene.name(), phone);
        String failCountStr = stringRedisTemplate.opsForValue().get(failKey);
        int failCount = failCountStr == null ? 0 : Integer.parseInt(failCountStr);
        if (failCount >= verifyMaxAttempts) {
            throw new BusinessException(BusinessCode.SMS_VERIFY_LIMIT_EXCEEDED);
        }

        // 2. 获取存储的验证码
        String codeKey = String.format(SMS_CODE_KEY, scene.name(), phone);
        String storedCode = stringRedisTemplate.opsForValue().get(codeKey);

        if (storedCode == null) {
            throw new BusinessException(BusinessCode.SMS_CODE_EXPIRED);
        }

        // 3. 验证
        if (!storedCode.equals(code)) {
            // 增加失败次数
            stringRedisTemplate.opsForValue().increment(failKey);
            stringRedisTemplate.expire(failKey, 15, TimeUnit.MINUTES);
            throw new BusinessException(BusinessCode.SMS_CODE_INVALID);
        }

        // 4. 验证成功，删除验证码和失败计数
        stringRedisTemplate.delete(codeKey);
        stringRedisTemplate.delete(failKey);

        log.info("验证码验证成功: phone={}, scene={}", phone, scene);
        return true;
    }

    /**
     * 验证验证码（不抛异常，返回布尔值）
     */
    public boolean checkSmsCode(String phone, String code, SmsScene scene) {
        try {
            return verifySmsCode(phone, code, scene);
        } catch (BusinessException e) {
            return false;
        }
    }
}
