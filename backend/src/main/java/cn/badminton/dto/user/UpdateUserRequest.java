package cn.badminton.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新用户资料请求
 * 作者: xiaolei
 */
@Data
public class UpdateUserRequest {
    @NotBlank
    private String userId;

    @NotBlank
    @Size(min = 2, max = 20)
    private String nickname;

    private String avatar = "";

    public void setAvatar(String avatar) { this.avatar = avatar == null ? "" : avatar; }
}
