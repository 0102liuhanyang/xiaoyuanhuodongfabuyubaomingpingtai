package com.work.xiaoyuanhuodongfabuyubaomingpingtai.common;

import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ApiResponse<?> handleValidation(Exception ex) {
        String msg = "参数错误";
        if (ex instanceof MethodArgumentNotValidException manv) {
            var fe = manv.getBindingResult().getFieldError();
            if (fe != null) {
                msg = fe.getDefaultMessage();
            }
        } else if (ex instanceof BindException be) {
            var fe = be.getFieldError();
            if (fe != null) {
                msg = fe.getDefaultMessage();
            }
        }
        return ApiResponse.failure(msg);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResponse<?> handleBadFormat(HttpMessageNotReadableException ex) {
        return ApiResponse.failure("请求体格式错误");
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<?> handleOther(Exception ex) {
        return ApiResponse.failure("系统错误: " + ex.getMessage());
    }
}
