package cn.badminton.repository;

import cn.badminton.config.RedisConfig;
import cn.badminton.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * UserRepository测试类
 * 测试LocalDateTime序列化/反序列化修复
 * 
 * 作者: xiaolei
 */
@ExtendWith(MockitoExtension.class)
public class UserRepositoryTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testFindByPhoneWithLocalDateTimeDeserialization() {
        // 模拟用户数据 - 时间字段以字符串形式存储
        String testPhone = "18569660001";
        String userId = "test-user-id";
        LocalDateTime now = LocalDateTime.now();
        
        Map<Object, Object> userMap = new HashMap<>();
        userMap.put("id", userId);
        userMap.put("phone", testPhone);
        userMap.put("nickname", "测试用户");
        userMap.put("password", "encrypted-password");
        userMap.put("avatar", "");
        userMap.put("status", "1");
        userMap.put("totalActivities", "0");
        userMap.put("totalExpense", "0.00");
        userMap.put("wxOpenId", "");
        userMap.put("wxUnionId", "");
        userMap.put("tenant", "1");
        userMap.put("state", "1");
        userMap.put("createdAt", now.toString()); // 以字符串形式存储
        userMap.put("updatedAt", now.toString()); // 以字符串形式存储
        userMap.put("deletedAt", null);
        userMap.put("organizationId", "0");

        // 模拟Redis调用
        when(valueOperations.get("badminton:index:phone:" + testPhone))
            .thenReturn(userId);
        when(hashOperations.entries(RedisConfig.RedisKeys.userKey(userId)))
            .thenReturn(userMap);

        // 执行测试
        User result = userRepository.findByPhone(testPhone);

        // 验证结果
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals(testPhone, result.getPhone());
        assertEquals("测试用户", result.getNickname());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        assertEquals(now.withNano(0), result.getCreatedAt().withNano(0)); // 忽略纳秒精度差异
        assertEquals(now.withNano(0), result.getUpdatedAt().withNano(0));
        assertNull(result.getDeletedAt());
    }
}