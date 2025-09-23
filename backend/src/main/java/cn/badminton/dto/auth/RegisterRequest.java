package cn.badminton.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户注册请求
 * 作者: xiaolei
 */
@Data
public class RegisterRequest {
    @NotBlank
    private String phone;

    @NotBlank
    @Size(min = 2, max = 20)
    private String nickname;

    @NotBlank
    @Size(min = 6, max = 100)
    private String password;
}
