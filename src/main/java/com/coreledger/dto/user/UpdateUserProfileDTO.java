package com.coreledger.dto.user;

import com.coreledger.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 更新用户个人信息DTO
 *
 * @author Core Ledger Team
 * @since 2.0.0
 */
@Data
@Schema(description = "更新用户个人信息请求")
public class UpdateUserProfileDTO {

    @Schema(description = "真实姓名", example = "张三")
    private String name;

    @Schema(description = "昵称", example = "小张")
    private String nickname;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatarUrl;

    @Schema(description = "性别", example = "MALE")
    private Gender gender;

    @Schema(description = "年龄", example = "25")
    private Integer age;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "地址ID", example = "1")
    private Long addressId;

    @Schema(description = "详细地址", example = "北京市朝阳区某某街道")
    private String addressDetail;
}
