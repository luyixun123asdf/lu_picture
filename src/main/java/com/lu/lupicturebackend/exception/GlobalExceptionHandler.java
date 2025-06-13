package com.lu.lupicturebackend.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.lu.lupicturebackend.common.BaseResponse;
import com.lu.lupicturebackend.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException: " + e.getMessage(), e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> businessExceptionHandler(RuntimeException e) {
        log.error("RunTimeException: " + e.getMessage(), e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }

    @ExceptionHandler(NotLoginException.class)
    public BaseResponse<?> notLoginExceptionHandler(NotLoginException e) {
        log.error("NotLoginException: " + e.getMessage(), e);
        return ResultUtils.error(ErrorCode.NOT_LOGIN_ERROR, "未登录");
    }

    @ExceptionHandler(NotPermissionException.class)
    public BaseResponse<?> notPermissionExceptionHandler(NotPermissionException e) {
        log.error("NotPermissionException: " + e.getMessage(), e);
        return ResultUtils.error(ErrorCode.NO_AUTH_ERROR, "无权限");
    }


}
