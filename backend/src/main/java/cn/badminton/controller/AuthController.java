package cn.badminton.controller;

import cn.badminton.common.Result;
import cn.badminton.dto.auth.LoginRequest;
import cn.badminton.dto.auth.RegisterRequest;
import cn.badminton.dto.auth.WechatLoginRequest;
import cn.badminton.dto.user.UserResponse;
import cn.badminton.model.User;
import cn.badminton.service.AuthService;
import cn.badminton.dto.auth.AuthResponse;
import cn.badminton.util.JwtUtil;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

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
}
