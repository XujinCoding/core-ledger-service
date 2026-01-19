package com.coreledger.vo.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件上传响应VO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文件上传响应")
public class FileUploadVO {

    /**
     * 文件路径（objectKey），用于保存到数据库
     */
    @Schema(description = "文件路径（objectKey）", example = "images/2025/01/19/abc123.jpg")
    private String path;

    /**
     * 预览URL（预签名URL），用于前端立即显示
     */
    @Schema(description = "预览URL（7天有效期）", example = "https://bucket.cos.ap-guangzhou.myqcloud.com/images/2025/01/19/abc123.jpg?sign=xxx")
    private String url;
}
