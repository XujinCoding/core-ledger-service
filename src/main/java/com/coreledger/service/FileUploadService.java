package com.coreledger.service;

import com.coreledger.config.TencentCosConfig;
import com.coreledger.enums.BusinessCode;
import com.coreledger.exception.BusinessException;
import com.coreledger.vo.file.FileUploadVO;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.model.GeneratePresignedUrlRequest;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

/**
 * 文件上传服务（腾讯云 COS）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final COSClient cosClient;
    private final TencentCosConfig cosConfig;

    /**
     * 上传图片到腾讯云 COS
     *
     * @param file 图片文件
     * @return 文件路径（objectKey）
     */
    public String uploadImage(MultipartFile file) {
        try {
            byte[] fileBytes = file.getInputStream().readAllBytes();
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String fileName = generateFileName(extension);
            String contentType = file.getContentType();
            
            return uploadBytes(fileBytes, fileName, contentType, null);
        } catch (IOException e) {
            log.error("读取文件失败", e);
            throw new BusinessException(BusinessCode.INTERNAL_SERVER_ERROR, "读取文件失败");
        }
    }

    /**
     * 上传图片并返回路径和预览URL
     *
     * @param file 图片文件
     * @return 文件上传结果（包含path和url）
     */
    public FileUploadVO uploadImageWithUrl(MultipartFile file) {
        String objectKey = uploadImage(file);
        String previewUrl = generatePresignedUrl(objectKey);
        return new FileUploadVO(objectKey, previewUrl);
    }

    /**
     * 上传字节数组到腾讯云 COS
     *
     * @param fileBytes 文件字节数组
     * @param fileName  文件名
     * @return 文件路径（objectKey）
     */
    public String uploadBytes(byte[] fileBytes, String fileName) {
        return uploadBytes(fileBytes, fileName, "image/png", null);
    }

    /**
     * 上传字节数组到腾讯云 COS
     *
     * @param fileBytes   文件字节数组
     * @param fileName    文件名
     * @param contentType 内容类型
     * @param filePath    自定义文件路径前缀
     * @return 文件路径（objectKey）
     */
    public String uploadBytes(byte[] fileBytes, String fileName, String contentType, String filePath) {
        try {
            // 构建对象键 (按日期分目录存储)
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String objectKey = cosConfig.getPathPrefix() + dateDir + "/" + fileName;
            if (filePath != null) {
                objectKey = filePath + dateDir + "/" + fileName;
            }

            // 设置元数据
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(fileBytes.length);
            if (contentType != null) {
                metadata.setContentType(contentType);
            }

            // 上传文件
            InputStream inputStream = new ByteArrayInputStream(fileBytes);
            PutObjectRequest putRequest = new PutObjectRequest(
                    cosConfig.getBucketName(),
                    objectKey,
                    inputStream,
                    metadata
            );

            PutObjectResult result = cosClient.putObject(putRequest);
            log.info("文件上传成功, ETag: {}, objectKey: {}", result.getETag(), objectKey);

            // 返回文件路径而不是URL
            return objectKey;

        } catch (Exception e) {
            log.error("文件上传异常", e);
            throw new BusinessException(BusinessCode.INTERNAL_SERVER_ERROR, "文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 生成预签名URL（带时效）
     *
     * @param objectKey 文件路径
     * @param expireDays 过期天数
     * @return 预签名URL
     */
    public String generatePresignedUrl(String objectKey, int expireDays) {
        if (objectKey == null || objectKey.isEmpty()) {
            return null;
        }
        
        try {
            Date expiration = Date.from(Instant.now().plus(expireDays, ChronoUnit.DAYS));
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
                    cosConfig.getBucketName(),
                    objectKey,
                    HttpMethodName.GET
            );
            request.setExpiration(expiration);
            
            String url = cosClient.generatePresignedUrl(request).toString();
            log.debug("生成预签名URL: objectKey={}, url={}", objectKey, url);
            return url;
        } catch (Exception e) {
            log.error("生成预签名URL失败: objectKey={}", objectKey, e);
            return null;
        }
    }

    /**
     * 生成预签名URL（默认7天有效期）
     *
     * @param objectKey 文件路径
     * @return 预签名URL
     */
    public String generatePresignedUrl(String objectKey) {
        return generatePresignedUrl(objectKey, 7);
    }

    /**
     * 删除文件
     *
     * @param objectKey 对象键
     */
    public void deleteFile(String objectKey) {
        try {
            cosClient.deleteObject(cosConfig.getBucketName(), objectKey);
            log.info("文件删除成功: {}", objectKey);
        } catch (Exception e) {
            log.error("文件删除失败: {}", objectKey, e);
            throw new BusinessException(BusinessCode.INTERNAL_SERVER_ERROR, "文件删除失败");
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf("."));
        }
        return ".png";
    }

    /**
     * 生成唯一文件名
     */
    private String generateFileName(String extension) {
        return UUID.randomUUID().toString().replace("-", "") + extension;
    }
}
