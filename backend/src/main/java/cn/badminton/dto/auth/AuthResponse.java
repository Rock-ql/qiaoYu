package cn.badminton.dto.auth;

import cn.badminton.dto.user.UserResponse;
import cn.badminton.model.User;
import lombok.Data;

/**
 * 认证响应（包含用户与token）
 * 作者: xiaolei
 */
@Data
public class AuthResponse {
    private String token;
    private UserResponse user;

    public static AuthResponse of(User u, String token) {
        AuthResponse r = new AuthResponse();
        r.token = token;
        r.user = UserResponse.from(u);
        return r;
    }
}
