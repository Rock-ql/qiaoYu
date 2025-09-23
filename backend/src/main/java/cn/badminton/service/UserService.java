package cn.badminton.service;

import cn.badminton.model.User;
import cn.badminton.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.math.BigDecimal;
import java.util.regex.Pattern;
import java.security.SecureRandom;

/**
 * 用户管理服务
 * 负责用户的增删改查、状态管理等核心业务逻辑
 * 
 * 作者: xiaolei
 */
@Service
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    // 手机号正则表达式
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 用户注册
     */
    public User register(String phone, String nickname, String password) {
        logger.info("开始用户注册，手机号: {}, 昵称: {}", phone, nickname);
        
        try {
            // 参数验证
            if (!isValidPhone(phone)) {
                throw new IllegalArgumentException("手机号格式不正确");
            }
            
            if (nickname == null || nickname.trim().isEmpty()) {
                throw new IllegalArgumentException("昵称不能为空");
            }
            
            if (password == null || password.length() < 6) {
                throw new IllegalArgumentException("密码长度不能少于6位");
            }
            
            // 检查手机号是否已存在
            if (userRepository.existsByPhone(phone)) {
                throw new IllegalArgumentException("手机号已被注册");
            }
            
            // 创建用户
            User user = new User(phone, nickname.trim(), passwordEncoder.encode(password));
            user = userRepository.save(user);
            
            logger.info("用户注册成功，用户ID: {}", user.getId());
            return user;
            
        } catch (Exception e) {
            logger.error("用户注册失败，手机号: {}, 错误信息: {}", phone, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 创建微信用户（用于微信授权首次登录时建档）
     * 说明：
     * - 自动生成有效的11位手机号（1开头，第二位3-9），避免触发手机号正则限制；
     * - 使用随机密码并进行加密；
     * - 设置微信OpenID/UnionID与头像；
     */
    public User createWechatUser(String nickname, String wxOpenId, String wxUnionId, String avatarUrl) {
        logger.info("创建微信新用户，昵称: {}", nickname);

        try {
            if (wxOpenId == null || wxOpenId.trim().isEmpty()) {
                throw new IllegalArgumentException("wxOpenId不能为空");
            }

            // 若已存在则直接返回
            User existing = userRepository.findByWxOpenId(wxOpenId);
            if (existing != null) {
                return existing;
            }

            String phone = generateValidPhone();
            while (userRepository.existsByPhone(phone)) {
                phone = generateValidPhone();
            }

            String rawPwd = "wx" + System.currentTimeMillis();
            User user = new User(phone, nickname == null ? "微信用户" : nickname.trim(), passwordEncoder.encode(rawPwd));
            user.setAvatar(avatarUrl == null ? "" : avatarUrl);
            user.setWxOpenId(wxOpenId);
            user.setWxUnionId(wxUnionId == null ? "" : wxUnionId);

            return userRepository.save(user);
        } catch (Exception e) {
            logger.error("创建微信用户失败，昵称: {}, 错误信息: {}", nickname, e.getMessage(), e);
            throw e;
        }
    }

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 生成合法的11位中国手机号：1 + [3-9] + 9位数字
     */
    private String generateValidPhone() {
        int second = 3 + RANDOM.nextInt(7); // 3..9
        StringBuilder sb = new StringBuilder();
        sb.append('1').append(second);
        for (int i = 0; i < 9; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * 根据手机号查找用户
     */
    public User findByPhone(String phone) {
        logger.debug("根据手机号查找用户: {}", phone);
        
        if (!isValidPhone(phone)) {
            return null;
        }
        
        try {
            User user = userRepository.findByPhone(phone);
            logger.debug("查找用户结果: {}", user != null ? "找到" : "未找到");
            return user;
            
        } catch (Exception e) {
            logger.error("根据手机号查找用户异常，手机号: {}, 错误信息: {}", phone, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 根据ID查找用户
     */
    public User findById(String userId) {
        logger.debug("根据ID查找用户: {}", userId);
        
        if (userId == null || userId.trim().isEmpty()) {
            return null;
        }
        
        try {
            User user = userRepository.findById(userId);
            logger.debug("查找用户结果: {}", user != null ? "找到" : "未找到");
            return user;
            
        } catch (Exception e) {
            logger.error("根据ID查找用户异常，用户ID: {}, 错误信息: {}", userId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 根据微信OpenID查找用户
     */
    public User findByWxOpenId(String wxOpenId) {
        logger.debug("根据微信OpenID查找用户: {}", wxOpenId);
        
        if (wxOpenId == null || wxOpenId.trim().isEmpty()) {
            return null;
        }
        
        try {
            User user = userRepository.findByWechatOpenId(wxOpenId);
            logger.debug("查找用户结果: {}", user != null ? "找到" : "未找到");
            return user;
            
        } catch (Exception e) {
            logger.error("根据微信OpenID查找用户异常，wxOpenId: {}, 错误信息: {}", wxOpenId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 更新用户信息
     */
    public User updateUser(User user) {
        logger.info("更新用户信息，用户ID: {}", user.getId());
        
        try {
            // 验证用户是否存在
            User existingUser = userRepository.findById(user.getId());
            if (existingUser == null) {
                throw new IllegalArgumentException("用户不存在");
            }
            
            // 验证数据有效性
            if (!user.isValid()) {
                throw new IllegalArgumentException("用户数据无效");
            }
            
            // 如果手机号发生变更，需要检查新手机号是否已被使用
            if (!existingUser.getPhone().equals(user.getPhone())) {
                if (userRepository.existsByPhone(user.getPhone())) {
                    throw new IllegalArgumentException("新手机号已被其他用户使用");
                }
            }
            
            User updatedUser = userRepository.save(user);
            logger.info("用户信息更新成功，用户ID: {}", user.getId());
            return updatedUser;
            
        } catch (Exception e) {
            logger.error("更新用户信息失败，用户ID: {}, 错误信息: {}", user.getId(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 更新用户头像
     */
    public void updateAvatar(String userId, String avatarUrl) {
        logger.info("更新用户头像，用户ID: {}", userId);
        
        try {
            User user = userRepository.findById(userId);
            if (user == null) {
                throw new IllegalArgumentException("用户不存在");
            }
            
            user.setAvatar(avatarUrl);
            userRepository.save(user);
            
            logger.info("用户头像更新成功，用户ID: {}", userId);
            
        } catch (Exception e) {
            logger.error("更新用户头像失败，用户ID: {}, 错误信息: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 更新用户昵称
     */
    public void updateNickname(String userId, String nickname) {
        logger.info("更新用户昵称，用户ID: {}", userId);
        
        try {
            if (nickname == null || nickname.trim().isEmpty()) {
                throw new IllegalArgumentException("昵称不能为空");
            }
            
            User user = userRepository.findById(userId);
            if (user == null) {
                throw new IllegalArgumentException("用户不存在");
            }
            
            user.setNickname(nickname.trim());
            userRepository.save(user);
            
            logger.info("用户昵称更新成功，用户ID: {}", userId);
            
        } catch (Exception e) {
            logger.error("更新用户昵称失败，用户ID: {}, 错误信息: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 绑定微信
     */
    public void bindWechat(String userId, String wxOpenId, String wxUnionId) {
        logger.info("绑定微信，用户ID: {}", userId);
        
        try {
            User user = userRepository.findById(userId);
            if (user == null) {
                throw new IllegalArgumentException("用户不存在");
            }
            
            // 检查微信是否已被其他用户绑定
            User existingWxUser = userRepository.findByWechatOpenId(wxOpenId);
            if (existingWxUser != null && !existingWxUser.getId().equals(userId)) {
                throw new IllegalArgumentException("该微信账号已被其他用户绑定");
            }
            
            user.setWxOpenId(wxOpenId);
            user.setWxUnionId(wxUnionId);
            userRepository.save(user);
            
            logger.info("微信绑定成功，用户ID: {}", userId);
            
        } catch (Exception e) {
            logger.error("绑定微信失败，用户ID: {}, 错误信息: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 增加用户活动参与次数
     */
    public void incrementUserActivities(String userId) {
        logger.debug("增加用户活动参与次数，用户ID: {}", userId);
        
        try {
            User user = userRepository.findById(userId);
            if (user != null) {
                user.incrementActivities();
                userRepository.save(user);
                logger.debug("用户活动参与次数增加成功，用户ID: {}", userId);
            }
            
        } catch (Exception e) {
            logger.error("增加用户活动参与次数失败，用户ID: {}, 错误信息: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * 增加用户消费金额
     */
    public void addUserExpense(String userId, BigDecimal amount) {
        logger.debug("增加用户消费金额，用户ID: {}, 金额: {}", userId, amount);
        
        try {
            User user = userRepository.findById(userId);
            if (user != null) {
                user.addExpense(amount);
                userRepository.save(user);
                logger.debug("用户消费金额增加成功，用户ID: {}", userId);
            }
            
        } catch (Exception e) {
            logger.error("增加用户消费金额失败，用户ID: {}, 错误信息: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * 禁用用户
     */
    public void disableUser(String userId) {
        logger.info("禁用用户，用户ID: {}", userId);
        
        try {
            userRepository.updateStatus(userId, 2); // 2表示禁用
            logger.info("用户禁用成功，用户ID: {}", userId);
            
        } catch (Exception e) {
            logger.error("禁用用户失败，用户ID: {}, 错误信息: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 启用用户
     */
    public void enableUser(String userId) {
        logger.info("启用用户，用户ID: {}", userId);
        
        try {
            userRepository.updateStatus(userId, 1); // 1表示正常
            logger.info("用户启用成功，用户ID: {}", userId);
            
        } catch (Exception e) {
            logger.error("启用用户失败，用户ID: {}, 错误信息: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 获取所有用户
     */
    public List<User> getAllUsers() {
        logger.debug("获取所有用户");
        
        try {
            List<User> users = userRepository.findAll();
            logger.debug("获取所有用户成功，用户数量: {}", users.size());
            return users;
            
        } catch (Exception e) {
            logger.error("获取所有用户失败，错误信息: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 验证手机号格式
     */
    private boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }
}
