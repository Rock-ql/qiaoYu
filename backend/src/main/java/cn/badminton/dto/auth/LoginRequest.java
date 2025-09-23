package cn.badminton.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户登录请求
 * 作者: xiaolei
 */
@Data
public class LoginRequest {
    @NotBlank
    private String phone;

    @NotBlank
    @Size(min = 6, max = 100)
    private String password;
}
