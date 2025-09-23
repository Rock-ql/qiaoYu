package cn.badminton.dto.user;

import jakarta.validation.constraints.NotBlank;

/**
 * 更新头像请求
 * 作者: xiaolei
 */
public class UpdateAvatarRequest {
    @NotBlank
    private String userId;

    private String avatar = "";

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar == null ? "" : avatar; }
}

