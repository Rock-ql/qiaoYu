package cn.badminton.controller;

import cn.badminton.common.Result;
import cn.badminton.dto.auth.LoginRequest;
import cn.badminton.dto.auth.RegisterRequest;
import cn.badminton.dto.auth.WechatLoginRequest;
import cn.badminton.dto.user.UserResponse;
import cn.badminton.dto.user.UpdateUserRequest;
import cn.badminton.dto.common.EmptyRequest;
import cn.badminton.model.User;
import cn.badminton.service.AuthService;
import cn.badminton.service.UserService;
import cn.badminton.dto.auth.AuthResponse;
import cn.badminton.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证相关接口
 *
 * 作者: xiaolei
 */
@RestController
@Slf4j
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        log.info("[Auth] 注册入参: phone={}, nickname=***", req.getPhone());
        User user = authService.register(req.getPhone(), req.getNickname(), req.getPassword());
        String token = jwtUtil.generateToken(user.getId());
        return Result.ok(AuthResponse.of(user, token));
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        log.info("[Auth] 登录入参: phone={}", req.getPhone());
        User user = authService.login(req.getPhone(), req.getPassword());
        String token = jwtUtil.generateToken(user.getId());
        return Result.ok(AuthResponse.of(user, token));
    }

    /**
     * 微信授权登录（支持网页/小程序）
     */
    @PostMapping("/wechat")
    public Result<AuthResponse> wechat(@Valid @RequestBody WechatLoginRequest req) {
        log.info("[Auth] 微信登录入参: code={}***", req.getCode());
        User user;
        if (req.getEncryptedData() != null && !req.getEncryptedData().isEmpty()) {
            user = authService.wechatMiniProgramLogin(req.getCode(), req.getEncryptedData(), req.getIv());
        } else {
            user = authService.wechatLogin(req.getCode());
        }
        String token = jwtUtil.generateToken(user.getId());
        return Result.ok(AuthResponse.of(user, token));
    }

    /**
     * 获取当前登录用户信息
     * 前端调用时需要在请求头携带 Authorization: Bearer <token>
     */
    @PostMapping("/userinfo")
    public Result<UserResponse> userinfo(@RequestBody(required = false) EmptyRequest req) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalArgumentException("未登录或登录已过期");
        }
        String userId = String.valueOf(authentication.getPrincipal());
        User user = userService.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return Result.ok(UserResponse.from(user));
    }

    /**
     * 更新个人资料（昵称、头像等）
     */
    @PostMapping("/profile")
    public Result<UserResponse> updateProfile(@Valid @RequestBody UpdateUserRequest req) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalArgumentException("未登录或登录已过期");
        }
        String userId = String.valueOf(authentication.getPrincipal());
        // 只允许修改自己的资料
        req.setUserId(userId);
        User u = userService.findById(userId);
        if (u == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        u.setNickname(req.getNickname());
        u.setAvatar(req.getAvatar());
        User updated = userService.updateUser(u);
        return Result.ok(UserResponse.from(updated));
    }
}
