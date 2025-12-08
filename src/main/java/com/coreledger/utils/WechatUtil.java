package com.coreledger.utils;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.coreledger.config.WechatConfig;
import com.coreledger.enums.BusinessCode;
import com.coreledger.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信工具类
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WechatUtil {

    private final WechatConfig wechatConfig;

    /**
     * 微信登录凭证校验
     * 通过 wx.login 接口获得临时登录凭证 code 后传到开发者服务器调用此接口完成登录流程
     *
     * @param code 登录时获取的 code
     * @return 包含 openid 和 session_key 的 JSON 对象
     * @throws BusinessException 调用失败时抛出异常
     */
    public JSONObject code2Session(String code) {
        Map<String, Object> params = new HashMap<>();
        params.put("appid", wechatConfig.getAppid());
        params.put("secret", wechatConfig.getSecret());
        params.put("js_code", code);
        params.put("grant_type", "authorization_code");

        try {
            String result = HttpUtil.get(wechatConfig.getCode2SessionUrl(), params);
            log.info("微信code2Session响应: {}", result);

            JSONObject jsonObject = JSONUtil.parseObj(result);

            // 检查是否有错误
            if (jsonObject.containsKey("errcode")) {
                Integer errcode = jsonObject.getInt("errcode");
                if (errcode != 0) {
                    String errmsg = jsonObject.getStr("errmsg");
                    log.error("微信code2Session失败: errcode={}, errmsg={}", errcode, errmsg);
                    throw new BusinessException(BusinessCode.WECHAT_API_ERROR, "微信登录失败: " + errmsg);
                }
            }

            // 返回包含 openid 和 session_key 的对象
            return jsonObject;
        } catch (Exception e) {
            log.error("调用微信code2Session接口失败", e);
            throw new BusinessException(BusinessCode.WECHAT_API_ERROR, "调用微信接口失败");
        }
    }

    /**
     * 解密微信加密数据（暂未使用，保留以供后续需要）
     * 用于解密手机号等敏感信息
     * 使用 AES-128-CBC 算法解密
     *
     * @param encryptedData 加密数据（Base64编码）
     * @param sessionKey    会话密钥（Base64编码）
     * @param iv            加密算法的初始向量（Base64编码）
     * @return 解密后的 JSON 数据
     * @throws BusinessException 解密失败时抛出异常
     */
    /*
    public JSONObject decryptData(String encryptedData, String sessionKey, String iv) {
        try {
            // 1. Base64 解码
            byte[] encryptedBytes = Base64.decode(encryptedData);
            byte[] sessionKeyBytes = Base64.decode(sessionKey);
            byte[] ivBytes = Base64.decode(iv);

            // 2. 创建 AES 密钥
            SecretKeySpec keySpec = new SecretKeySpec(sessionKeyBytes, 0, sessionKeyBytes.length, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

            // 3. 初始化 Cipher
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            // 4. 解密
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            String decryptedData = new String(decryptedBytes, StandardCharsets.UTF_8);

            log.debug("微信数据解密成功: {}", decryptedData);

            // 5. 解析为 JSON
            return JSONUtil.parseObj(decryptedData);
        } catch (Exception e) {
            log.error("微信数据解密失败", e);
            throw new BusinessException(BusinessCode.WECHAT_DECRYPT_FAILED, "微信数据解密失败");
        }
    }
    */
}
