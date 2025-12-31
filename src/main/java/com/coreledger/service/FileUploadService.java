package com.coreledger.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.coreledger.config.GitHubConfig;
import com.coreledger.enums.BusinessCode;
import com.coreledger.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.UUID;

/**
 * 文件上传服务（GitHub 图床）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final GitHubConfig gitHubConfig;

    private static final String GITHUB_API_URL = "https://api.github.com/repos/%s/%s/contents/%s%s";

    /**
     * 上传图片到 GitHub
     *
     * @param file 图片文件
     * @return 图片访问 URL
     */
    public String uploadImage(MultipartFile file) {
        try {
            // 1. 读取文件并转为 Base64
            InputStream inputStream = file.getInputStream();
            byte[] fileBytes = inputStream.readAllBytes();
            String fileBase64 = Base64.getEncoder().encodeToString(fileBytes);

            // 2. 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".png";
            String fileName = UUID.randomUUID().toString().replace("-", "") + extension;

            // 3. 构建请求参数
            JSONObject param = new JSONObject();
            param.set("message", "upload image: " + fileName);
            param.set("content", fileBase64);
            param.set("branch", gitHubConfig.getBranch());

            JSONObject committer = new JSONObject();
            committer.set("name", gitHubConfig.getName());
            committer.set("email", gitHubConfig.getEmail());
            param.set("committer", committer);

            // 4. 构建请求 URL
            String url = String.format(GITHUB_API_URL,
                    gitHubConfig.getOwner(),
                    gitHubConfig.getRepo(),
                    gitHubConfig.getPath(),
                    fileName);

            // 5. 发起 PUT 请求
            HttpResponse response = HttpRequest.put(url)
                    .header("Accept", "application/vnd.github+json")
                    .header("Authorization", "token " + gitHubConfig.getToken())
                    .header("X-GitHub-Api-Version", "2022-11-28")
                    .body(param.toString())
                    .timeout(30000)
                    .execute();

            log.info("GitHub 上传响应状态: {}", response.getStatus());

            if (response.isOk() || response.getStatus() == 201) {
                JSONObject jsonObject = JSONUtil.parseObj(response.body());
                JSONObject content = jsonObject.getJSONObject("content");
                String downloadUrl = content.getStr("download_url");
                log.info("图片上传成功: {}", downloadUrl);
                return downloadUrl;
            } else {
                log.error("GitHub 上传失败: {}", response.body());
                throw new BusinessException(BusinessCode.INTERNAL_SERVER_ERROR, "图片上传失败");
            }
        } catch (IOException e) {
            log.error("读取文件失败", e);
            throw new BusinessException(BusinessCode.INTERNAL_SERVER_ERROR, "读取文件失败");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("图片上传异常", e);
            throw new BusinessException(BusinessCode.INTERNAL_SERVER_ERROR, "图片上传失败: " + e.getMessage());
        }
    }
}
