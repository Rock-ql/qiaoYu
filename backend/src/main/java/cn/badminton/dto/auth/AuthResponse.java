package cn.badminton.dto.auth;

import cn.badminton.dto.user.UserResponse;
import cn.badminton.model.User;

/**
 * 认证响应（包含用户与token）
 * 作者: xiaolei
 */
public class AuthResponse {
    private String token;
    private UserResponse user;

    public static AuthResponse of(User u, String token) {
        AuthResponse r = new AuthResponse();
        r.token = token;
        r.user = UserResponse.from(u);
        return r;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public UserResponse getUser() { return user; }
    public void setUser(UserResponse user) { this.user = user; }
}

