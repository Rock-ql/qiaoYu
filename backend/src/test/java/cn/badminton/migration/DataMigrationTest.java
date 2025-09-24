package cn.badminton.migration;

import cn.badminton.model.User;
import cn.badminton.repository.UserRepository;
import cn.badminton.repository.jpa.UserJpaRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 数据迁移测试
 * 验证MySQL和Redis数据的一致性
 *
 * 作者: xiaolei
 */
@SpringBootTest
@ActiveProfiles("test")
@Slf4j
public class DataMigrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Test
    @Transactional
    public void testUserDataConsistency() {
        log.info("测试用户数据一致性...");

        // 创建测试用户
        User testUser = new User("13800138000", "测试用户", "password123");

        // 通过Repository保存（应该同时保存到MySQL和Redis）
        User savedUser = userRepository.save(testUser);

        assertNotNull(savedUser);
        assertNotNull(savedUser.getId());
        log.info("用户保存成功，ID: {}", savedUser.getId());

        // 验证MySQL中的数据
        java.util.Optional<User> dbUser = userJpaRepository.findById(savedUser.getId());
        assertTrue(dbUser.isPresent(), "MySQL中应该存在该用户");
        assertEquals("13800138000", dbUser.get().getPhone());
        assertEquals("测试用户", dbUser.get().getNickname());
        log.info("MySQL数据验证成功");

        // 验证通过Repository查询的数据
        User queriedUser = userRepository.findById(savedUser.getId());
        assertNotNull(queriedUser, "通过Repository应该能查询到用户");
        assertEquals("13800138000", queriedUser.getPhone());
        assertEquals("测试用户", queriedUser.getNickname());
        log.info("Repository查询验证成功");

        // 验证通过手机号查询
        User userByPhone = userRepository.findByPhone("13800138000");
        assertNotNull(userByPhone, "通过手机号应该能查询到用户");
        assertEquals(savedUser.getId(), userByPhone.getId());
        log.info("手机号查询验证成功");

        log.info("用户数据一致性测试通过！");
    }
}