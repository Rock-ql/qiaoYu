package cn.badminton.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis配置类
 * 配置Redis连接、序列化方式和缓存策略
 * 
 * 作者: xiaolei
 */
@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * 配置RedisTemplate
     * 设置键值序列化方式
     */
    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = createJacksonSerializer();

        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // 设置各种序列化器
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        // 开启事务支持
        template.setEnableTransactionSupport(true);
        
        template.afterPropertiesSet();
        return template;
    }

    /**
     * 配置缓存管理器
     * 设置缓存过期时间和序列化方式
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 创建Jackson序列化器
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = createJacksonSerializer();

        // 配置序列化方式
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30)) // 默认缓存30分钟
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }

    /**
     * 创建Jackson序列化器
     * 配置对象映射规则
     */
    private Jackson2JsonRedisSerializer<Object> createJacksonSerializer() {
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper om = new ObjectMapper();
        
        // 设置可见性
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        
        // 启用默认类型
        om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        
        // 注册Java时间模块，支持LocalDateTime等类型
        om.registerModule(new JavaTimeModule());
        
        jackson2JsonRedisSerializer.setObjectMapper(om);
        return jackson2JsonRedisSerializer;
    }

    /**
     * Redis键名常量
     */
    public static class RedisKeys {
        // 用户相关键名
        public static final String USER_PREFIX = "badminton:user:";
        public static final String USER_ACTIVITIES_PREFIX = "badminton:user:activities:";
        public static final String USER_FRIENDS_PREFIX = "badminton:user:friends:";
        public static final String USER_DEBTS_PREFIX = "badminton:user:debts:";
        
        // 活动相关键名
        public static final String ACTIVITY_PREFIX = "badminton:activity:";
        public static final String ACTIVITY_PARTICIPANTS_PREFIX = "badminton:activity:participants:";
        public static final String ACTIVITY_EXPENSES_PREFIX = "badminton:activity:expenses:";
        public static final String ACTIVITIES_BY_DATE_PREFIX = "badminton:activities:by_date:";
        public static final String ACTIVITIES_BY_VENUE_PREFIX = "badminton:activities:by_venue:";
        
        // 参与记录相关键名
        public static final String PARTICIPATION_PREFIX = "badminton:participation:";
        
        // 费用相关键名
        public static final String EXPENSE_PREFIX = "badminton:expense:";
        public static final String EXPENSE_SHARES_PREFIX = "badminton:expense:shares:";
        public static final String SHARE_PREFIX = "badminton:share:";
        
        // 认证相关键名
        public static final String TOKEN_PREFIX = "badminton:token:";
        public static final String VERIFICATION_CODE_PREFIX = "badminton:verification:";
        
        // 系统配置键名
        public static final String SYSTEM_CONFIG_PREFIX = "badminton:config:";

        /**
         * 生成用户键名
         */
        public static String userKey(String userId) {
            return USER_PREFIX + userId;
        }

        /**
         * 生成用户活动列表键名
         */
        public static String userActivitiesKey(String userId) {
            return USER_ACTIVITIES_PREFIX + userId;
        }

        /**
         * 生成活动键名
         */
        public static String activityKey(String activityId) {
            return ACTIVITY_PREFIX + activityId;
        }

        /**
         * 生成活动参与者列表键名
         */
        public static String activityParticipantsKey(String activityId) {
            return ACTIVITY_PARTICIPANTS_PREFIX + activityId;
        }

        /**
         * 生成费用记录键名
         */
        public static String expenseKey(String expenseId) {
            return EXPENSE_PREFIX + expenseId;
        }

        /**
         * 生成费用分摊键名
         */
        public static String shareKey(String shareId) {
            return SHARE_PREFIX + shareId;
        }

        /**
         * 生成JWT令牌键名
         */
        public static String tokenKey(String userId) {
            return TOKEN_PREFIX + userId;
        }

        /**
         * 生成验证码键名
         */
        public static String verificationCodeKey(String phone) {
            return VERIFICATION_CODE_PREFIX + phone;
        }
    }

    /**
     * Redis过期时间常量（秒）
     */
    public static class RedisTTL {
        public static final long USER_SESSION = 7 * 24 * 60 * 60;        // 用户会话：7天
        public static final long VERIFICATION_CODE = 5 * 60;             // 验证码：5分钟
        public static final long ACTIVITY_CACHE = 24 * 60 * 60;          // 活动缓存：1天
        public static final long USER_CACHE = 60 * 60;                   // 用户缓存：1小时
        public static final long CANCELLED_ACTIVITY = 30 * 24 * 60 * 60; // 已取消活动：30天
        public static final long COMPLETED_ACTIVITY = 365 * 24 * 60 * 60; // 已完成活动：1年
    }
}