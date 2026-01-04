package com.coreledger.service;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import com.coreledger.config.GitHubConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.UUID;

/**
 * 二维码服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QrCodeService {

    private final FileUploadService fileUploadService;

    /**
     * 生成二维码并上传到 GitHub
     *
     * @param content 二维码内容
     * @return 二维码图片 URL
     */
    public String generateAndUploadQrCode(String content) {
        try {
            // 1. 生成二维码图片
            QrConfig config = new QrConfig(300, 300);
            config.setMargin(2);
            BufferedImage image = QrCodeUtil.generate(content, config);

            // 2. 转为 byte 数组
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImgUtil.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();

            // 3. 生成文件名
            String fileName = "qrcode_" + UUID.randomUUID().toString().replace("-", "") + ".png";

            // 4. 上传到 GitHub
            String url = fileUploadService.uploadBytes(imageBytes, fileName);
            log.info("二维码生成并上传成功: content={}, url={}", content, url);

            return url;
        } catch (Exception e) {
            log.error("生成二维码失败: content={}", content, e);
            return null;
        }
    }
}
