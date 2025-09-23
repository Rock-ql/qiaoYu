package cn.badminton.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis配置类
 * 配置RedisTemplate的序列化策略
 * 
 * 作者: xiaolei
 */
@Configuration
public class RedisConfig {

    /**
     * 配置RedisTemplate
     * 使用String序列化器处理key，JSON序列化器处理value
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 设置key的序列化器
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // 设置value的序列化器
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer()); // Hash值使用String序列化，保持简单

        // 启用事务支持
        template.setEnableTransactionSupport(true);

        template.afterPropertiesSet();
        return template;
    }
}