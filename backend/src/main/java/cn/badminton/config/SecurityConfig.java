package cn.badminton.config;

import cn.badminton.filter.JwtAuthenticationFilter;
import cn.badminton.security.RestAccessDeniedHandler;
import cn.badminton.security.RestAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.http.HttpMethod;

import org.springframework.beans.factory.annotation.Value;
import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 基础配置
 * - CORS、安全头
 * - JWT 解析过滤
 * 作者: xiaolei
 */
@Configuration
public class SecurityConfig {

    @Autowired private JwtAuthenticationFilter jwtAuthenticationFilter;
    @Autowired private RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    @Autowired private RestAccessDeniedHandler restAccessDeniedHandler;

    @Value("${app.security.cors.allowed-origins:*}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/**")
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .headers(h -> h.frameOptions(frame -> frame.disable()))
            .exceptionHandling(eh -> eh
                .authenticationEntryPoint(restAuthenticationEntryPoint)
                .accessDeniedHandler(restAccessDeniedHandler)
            )
            .authorizeHttpRequests(auth -> auth
                // 允许跨域预检请求
                .requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()
                // 认证开放接口
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .cors(cors -> cors.configurationSource(req -> buildCorsConfiguration()));

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private CorsConfiguration buildCorsConfiguration() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        if ("*".equals(allowedOrigins)) {
            // 支持凭证时不允许 setAllowedOrigins("*")，需使用通配模式
            config.addAllowedOriginPattern("*");
        } else {
            Arrays.stream(allowedOrigins.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .forEach(config::addAllowedOrigin);
        }
        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        // 暴露部分头，便于前端读取
        config.setExposedHeaders(List.of("Authorization", "Content-Disposition"));
        return config;
    }
}
