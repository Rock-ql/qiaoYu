package cn.badminton.aop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 控制器层统一日志切面
 * - 记录入参（脱敏）与耗时
 * - 异常时打印错误日志，包含入参JSON
 * 作者: xiaolei
 */
@Aspect
@Component
@Order(10)
@Slf4j
public class ApiLogAspect {

    private final ObjectMapper mapper = new ObjectMapper();

    @Around("within(cn.badminton.controller..*)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        String classMethod = method.getDeclaringClass().getSimpleName() + "." + method.getName();

        Map<String, Object> params = collectParams(signature.getParameterNames(), pjp.getArgs());
        String inJson = safeJson(params);

        try {
            Object ret = pjp.proceed();
            long cost = System.currentTimeMillis() - start;
            log.info("API ok {} cost={}ms in={}", classMethod, cost, inJson);
            return ret;
        } catch (Throwable e) {
            long cost = System.currentTimeMillis() - start;
            log.error("API err {} cost={}ms in={}", classMethod, cost, inJson, e);
            throw e;
        }
    }

    private Map<String, Object> collectParams(String[] names, Object[] args) {
        Map<String, Object> map = new HashMap<>();
        if (names == null || args == null) return map;
        int n = Math.min(names.length, args.length);
        for (int i = 0; i < n; i++) {
            String name = names[i];
            Object val = args[i];
            if (name != null && isSensitive(name)) {
                map.put(name, mask(val));
            } else {
                map.put(name == null ? ("arg" + i) : name, val);
            }
        }
        return map;
    }

    private boolean isSensitive(String name) {
        String n = name.toLowerCase();
        return n.contains("password") || n.contains("pwd") || n.contains("secret") || n.contains("token");
    }

    private Object mask(Object v) { return v == null ? null : "***"; }

    private String safeJson(Object o) {
        try { return mapper.writeValueAsString(o); } catch (JsonProcessingException e) { return String.valueOf(o); }
    }
}

