package cn.badminton.dto.common;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户ID请求
 * 作者: xiaolei
 */
@Data
public class UserIdRequest {
    @NotBlank
    private String userId;
}
