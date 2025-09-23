package cn.badminton.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 请求响应日志拦截器（简版）
 * 作者: xiaolei
 */
@Component
@Slf4j
public class LoggingInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        log.info("REQ {} {} traceId={} UA={} ", request.getMethod(), request.getRequestURI(), MDC.get("traceId"), request.getHeader("User-Agent"));
        return true;
    }
}
