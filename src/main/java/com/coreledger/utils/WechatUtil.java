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
     * 解密微信加密数据
     * 用于解密手机号等敏感信息
     *
     * @param encryptedData 加密数据
     * @param sessionKey    会话密钥
     * @param iv            加密算法的初始向量
     * @return 解密后的数据
     */
    public JSONObject decryptData(String encryptedData, String sessionKey, String iv) {
        // TODO: 实现微信数据解密逻辑
        // 使用 AES-128-CBC 解密算法
        // 参考: https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/signature.html
        throw new BusinessException(BusinessCode.NOT_IMPLEMENTED, "微信数据解密功能待实现");
    }
}
