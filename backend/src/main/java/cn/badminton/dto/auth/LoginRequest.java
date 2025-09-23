package cn.badminton.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 用户登录请求
 * 作者: xiaolei
 */
public class LoginRequest {
    @NotBlank
    private String phone;

    @NotBlank
    @Size(min = 6, max = 100)
    private String password;

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
