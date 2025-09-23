package cn.badminton.exception;

import cn.badminton.common.Result;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 * 将异常转换为统一的Result响应
 * 作者: xiaolei
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("业务参数异常: {}", e.getMessage(), e);
        return Result.fail(4001, e.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, HttpMessageNotReadableException.class, JsonProcessingException.class, InvalidTypeIdException.class})
    public Result<Void> handleValidation(Exception e) {
        log.warn("请求校验失败: {}", e.getMessage(), e);
        return Result.fail(4000, "请求参数不合法");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleOther(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        return Result.fail(5000, "系统异常，请稍后重试");
    }
}
