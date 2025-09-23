package cn.badminton.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 更新头像请求
 * 作者: xiaolei
 */
@Data
public class UpdateAvatarRequest {
    @NotBlank
    private String userId;

    private String avatar = "";

    public void setAvatar(String avatar) { this.avatar = avatar == null ? "" : avatar; }
}
