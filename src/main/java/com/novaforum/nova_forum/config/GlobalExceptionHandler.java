package com.novaforum.nova_forum.config;

import com.novaforum.nova_forum.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * 统一处理系统中的各种异常
 *
 * @author Nova Forum Team
 * @since 2025-11-05
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        log.warn("参数验证失败，路径: {}, 错误: {}", request.getRequestURI(), errors);
        
        return ResponseEntity.ok(ApiResponse.error("参数验证失败"));
    }

    /**
     * 处理业务异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
            RuntimeException ex, HttpServletRequest request) {
        
        log.error("业务异常，路径: {}, 错误: {}", request.getRequestURI(), ex.getMessage(), ex);
        
        return ResponseEntity.ok(ApiResponse.error(ex.getMessage()));
    }

    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        log.error("系统异常，路径: {}, 错误: {}", request.getRequestURI(), ex.getMessage(), ex);
        
        return ResponseEntity.ok(ApiResponse.error("系统内部错误，请稍后重试"));
    }

    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiResponse<Void>> handleNullPointerException(
            NullPointerException ex, HttpServletRequest request) {
        
        log.error("空指针异常，路径: {}, 错误: {}", request.getRequestURI(), ex.getMessage(), ex);
        
        return ResponseEntity.ok(ApiResponse.error("请求参数不能为空"));
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {
        
        log.warn("非法参数异常，路径: {}, 错误: {}", request.getRequestURI(), ex.getMessage());
        
        return ResponseEntity.ok(ApiResponse.error("参数非法: " + ex.getMessage()));
    }
}
