package com.coreledger.controller;

import com.coreledger.common.Result;
import com.coreledger.service.FileUploadService;
import com.coreledger.vo.file.FileUploadVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器
 */
@Tag(name = "文件上传", description = "图片上传接口")
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadService fileUploadService;

    /**
     * 上传图片
     */
    @Operation(summary = "上传图片", description = "上传图片到腾讯云COS，返回文件路径和预览URL")
    @PostMapping("/upload/image")
    public Result<FileUploadVO> uploadImage(@RequestParam("file") MultipartFile file) {
        FileUploadVO result = fileUploadService.uploadImageWithUrl(file);
        return Result.success(result);
    }
}
