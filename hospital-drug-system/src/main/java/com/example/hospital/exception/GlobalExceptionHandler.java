package com.example.hospital.exception;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理 @Valid 注解验证失败抛出的异常
     * @param ex MethodArgumentNotValidException
     * @return 包含详细字段错误的响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            if (error instanceof FieldError) {
                String fieldName = ((FieldError) error).getField();
                String errorMessage = error.getDefaultMessage();
                fieldErrors.put(fieldName, errorMessage);
            } else {
                // 处理非字段错误（较少见）
                fieldErrors.put(error.getObjectName(), error.getDefaultMessage());
            }
        });

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", "error");
        responseBody.put("message", "输入参数验证失败"); // General validation failure message
        responseBody.put("errors", fieldErrors); // Detailed field-specific errors

        return responseBody;
    }

    // 你可以在这里添加更多的异常处理方法来处理其他类型的异常
    // 例如：处理 EntityNotFoundException 等

    /**
     * 处理一个通用的服务器内部错误
     * @param ex Exception
     * @return 通用错误响应
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleGenericException(Exception ex) {
        // 在实际应用中，这里应该记录详细的异常日志
        // 为了调试方便，暂时打印堆栈跟踪
        ex.printStackTrace(); 

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", "error");
        // 注意：生产环境不应直接暴露原始异常信息给客户端
        responseBody.put("message", "服务器内部错误，请联系管理员"); 
        // 可以考虑添加一个唯一的错误ID供追踪
        // responseBody.put("errorId", generateUniqueErrorId()); 

        return responseBody;
    }
} 