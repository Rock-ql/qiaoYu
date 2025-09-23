package cn.badminton.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 更新用户资料请求
 * 作者: xiaolei
 */
public class UpdateUserRequest {
    @NotBlank
    private String userId;

    @NotBlank
    @Size(min = 2, max = 20)
    private String nickname;

    private String avatar = "";

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar == null ? "" : avatar; }
}

