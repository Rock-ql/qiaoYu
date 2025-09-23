package cn.badminton.service;

import cn.badminton.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 认证服务
 * 负责用户登录、注册、微信授权登录等认证相关业务
 * 
 * 作者: xiaolei
 */
@Service
@Slf4j
public class AuthService {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private WechatService wechatService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 用户登录
     */
    public User login(String phone, String password) {
        log.info("用户登录，手机号: {}", phone);
        
        try {
            // 查找用户
            User user = userService.findByPhone(phone);
            if (user == null) {
                throw new IllegalArgumentException("用户不存在");
            }
            
            // 检查用户状态
            if (user.getStatus() != 1) {
                throw new IllegalArgumentException("用户账号已被禁用");
            }
            
            // 验证密码
            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new IllegalArgumentException("密码错误");
            }
            
            log.info("用户登录成功，用户ID: {}", user.getId());
            return user;
            
        } catch (Exception e) {
            log.error("用户登录失败，手机号: {}, 错误信息: {}", phone, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 用户注册
     */
    public User register(String phone, String nickname, String password) {
        log.info("用户注册，手机号: {}, 昵称: {}", phone, nickname);
        
        try {
            User user = userService.register(phone, nickname, password);
            log.info("用户注册成功，用户ID: {}", user.getId());
            return user;
            
        } catch (Exception e) {
            log.error("用户注册失败，手机号: {}, 错误信息: {}", phone, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 微信授权登录
     */
    public User wechatLogin(String code) {
        log.info("微信授权登录，授权码: {}", code);
        
        try {
            // 通过授权码获取微信用户信息
            WechatService.WechatUserInfo wechatUserInfo = wechatService.getUserInfoByCode(code);
            if (wechatUserInfo == null) {
                throw new IllegalArgumentException("微信授权失败，无法获取用户信息");
            }
            
            // 根据微信OpenID查找已存在的用户
            User existingUser = userService.findByWxOpenId(wechatUserInfo.getOpenId());
            
            if (existingUser != null) {
                // 用户已存在，直接登录
                log.info("微信用户已存在，直接登录，用户ID: {}", existingUser.getId());
                return existingUser;
            } else {
                // 新用户，创建账号
                User newUser = createWechatUser(wechatUserInfo);
                log.info("微信新用户注册成功，用户ID: {}", newUser.getId());
                return newUser;
            }
            
        } catch (Exception e) {
            log.error("微信授权登录失败，授权码: {}, 错误信息: {}", code, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 微信快捷登录（小程序）
     */
    public User wechatMiniProgramLogin(String code, String encryptedData, String iv) {
        log.info("微信小程序快捷登录");
        
        try {
            // 通过小程序授权获取用户信息
            WechatService.WechatUserInfo wechatUserInfo = wechatService.getMiniProgramUserInfo(code, encryptedData, iv);
            if (wechatUserInfo == null) {
                throw new IllegalArgumentException("微信小程序授权失败");
            }
            
            // 根据微信OpenID查找已存在的用户
            User existingUser = userService.findByWxOpenId(wechatUserInfo.getOpenId());
            
            if (existingUser != null) {
                // 用户已存在，直接登录
                log.info("微信小程序用户已存在，直接登录，用户ID: {}", existingUser.getId());
                return existingUser;
            } else {
                // 新用户，创建账号
                User newUser = createWechatUser(wechatUserInfo);
                log.info("微信小程序新用户注册成功，用户ID: {}", newUser.getId());
                return newUser;
            }
            
        } catch (Exception e) {
            log.error("微信小程序快捷登录失败，错误信息: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 绑定微信账号
     */
    public void bindWechat(String userId, String code) {
        log.info("绑定微信账号，用户ID: {}", userId);
        
        try {
            // 获取微信用户信息
            WechatService.WechatUserInfo wechatUserInfo = wechatService.getUserInfoByCode(code);
            if (wechatUserInfo == null) {
                throw new IllegalArgumentException("微信授权失败，无法获取用户信息");
            }
            
            // 绑定微信
            userService.bindWechat(userId, wechatUserInfo.getOpenId(), wechatUserInfo.getUnionId());
            
            log.info("绑定微信账号成功，用户ID: {}", userId);
            
        } catch (Exception e) {
            log.error("绑定微信账号失败，用户ID: {}, 错误信息: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 修改密码
     */
    public void changePassword(String userId, String oldPassword, String newPassword) {
        log.info("修改密码，用户ID: {}", userId);
        
        try {
            // 获取用户
            User user = userService.findById(userId);
            if (user == null) {
                throw new IllegalArgumentException("用户不存在");
            }
            
            // 验证旧密码
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                throw new IllegalArgumentException("原密码错误");
            }
            
            // 验证新密码
            if (newPassword == null || newPassword.length() < 6) {
                throw new IllegalArgumentException("新密码长度不能少于6位");
            }
            
            // 更新密码
            user.setPassword(passwordEncoder.encode(newPassword));
            userService.updateUser(user);
            
            log.info("修改密码成功，用户ID: {}", userId);
            
        } catch (Exception e) {
            log.error("修改密码失败，用户ID: {}, 错误信息: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 重置密码
     */
    public void resetPassword(String phone, String newPassword) {
        log.info("重置密码，手机号: {}", phone);
        
        try {
            // 查找用户
            User user = userService.findByPhone(phone);
            if (user == null) {
                throw new IllegalArgumentException("用户不存在");
            }
            
            // 验证新密码
            if (newPassword == null || newPassword.length() < 6) {
                throw new IllegalArgumentException("新密码长度不能少于6位");
            }
            
            // 更新密码
            user.setPassword(passwordEncoder.encode(newPassword));
            userService.updateUser(user);
            
            log.info("重置密码成功，手机号: {}", phone);
            
        } catch (Exception e) {
            log.error("重置密码失败，手机号: {}, 错误信息: {}", phone, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 验证用户身份
     */
    public boolean validateUser(String userId) {
        log.debug("验证用户身份，用户ID: {}", userId);
        
        try {
            User user = userService.findById(userId);
            boolean isValid = user != null && user.getStatus() == 1;
            
            log.debug("用户身份验证结果: {}", isValid ? "有效" : "无效");
            return isValid;
            
        } catch (Exception e) {
            log.error("验证用户身份失败，用户ID: {}, 错误信息: {}", userId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 创建微信用户
     */
    private User createWechatUser(WechatService.WechatUserInfo wechatUserInfo) {
        // 交给 UserService 以合规方式创建（生成有效手机号、编码密码、唯一性校验）
        return userService.createWechatUser(
                wechatUserInfo.getNickname(),
                wechatUserInfo.getOpenId(),
                wechatUserInfo.getUnionId(),
                wechatUserInfo.getAvatarUrl() == null ? "" : wechatUserInfo.getAvatarUrl()
        );
    }
}
