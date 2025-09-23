package cn.badminton.controller;

import cn.badminton.common.Result;
import cn.badminton.dto.common.EmptyRequest;
import cn.badminton.dto.common.UserIdRequest;
import cn.badminton.dto.user.UpdateAvatarRequest;
import cn.badminton.dto.user.UpdateUserRequest;
import cn.badminton.dto.user.UserResponse;
import cn.badminton.model.User;
import cn.badminton.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户控制器
 * 作者: xiaolei
 */
@RestController
@Slf4j
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/detail")
    public Result<UserResponse> detail(@Valid @RequestBody UserIdRequest req) {
        User u = userService.findById(req.getUserId());
        return Result.ok(u == null ? null : UserResponse.from(u));
    }

    @PostMapping("/list")
    public Result<List<UserResponse>> list(@RequestBody(required = false) EmptyRequest req) {
        List<UserResponse> list = userService.getAllUsers().stream().map(UserResponse::from).collect(Collectors.toList());
        return Result.ok(list);
    }

    @PostMapping("/updateProfile")
    public Result<UserResponse> updateProfile(@Valid @RequestBody UpdateUserRequest req) {
        log.info("[User] 更新资料: userId={}, nickname=***", req.getUserId());
        User u = userService.findById(req.getUserId());
        if (u == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        u.setNickname(req.getNickname());
        u.setAvatar(req.getAvatar());
        User updated = userService.updateUser(u);
        return Result.ok(UserResponse.from(updated));
    }

    @PostMapping("/updateAvatar")
    public Result<Void> updateAvatar(@Valid @RequestBody UpdateAvatarRequest req) {
        log.info("[User] 更新头像: userId={}", req.getUserId());
        userService.updateAvatar(req.getUserId(), req.getAvatar());
        return Result.ok();
    }

    @PostMapping("/disable")
    public Result<Void> disable(@Valid @RequestBody UserIdRequest req) {
        userService.disableUser(req.getUserId());
        return Result.ok();
    }

    @PostMapping("/enable")
    public Result<Void> enable(@Valid @RequestBody UserIdRequest req) {
        userService.enableUser(req.getUserId());
        return Result.ok();
    }
}
