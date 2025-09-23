package cn.badminton.dto.common;

import jakarta.validation.constraints.NotBlank;

/**
 * 用户ID请求
 * 作者: xiaolei
 */
public class UserIdRequest {
    @NotBlank
    private String userId;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}

