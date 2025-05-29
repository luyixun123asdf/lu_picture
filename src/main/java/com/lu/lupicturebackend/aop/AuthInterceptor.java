package com.lu.lupicturebackend.aop;

import com.lu.lupicturebackend.annotation.AuthCheck;
import com.lu.lupicturebackend.exception.ErrorCode;
import com.lu.lupicturebackend.exception.ThrowUtils;
import com.lu.lupicturebackend.model.entity.User;
import com.lu.lupicturebackend.model.enums.UserRoleEnum;
import com.lu.lupicturebackend.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Aspect //  表示当前类是一个切面
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    /**
     * 执行拦截
     *
     * @param joinPoint
     * @param authCheck
     * @return
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String s = authCheck.mustRole();
        RequestAttributes currentRequestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) currentRequestAttributes).getRequest();
        //  获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        UserRoleEnum enumByValue = UserRoleEnum.getEnumByValue(s);
        // 不需要权限放行
        if (enumByValue == null) {
            return joinPoint.proceed();
        }
        UserRoleEnum enumByValue1 = UserRoleEnum.getEnumByValue(loginUser.getUserRole());

        ThrowUtils.throwIf(enumByValue1 == null, ErrorCode.NO_AUTH_ERROR); //  没有权限
        // 匹配权限
        ThrowUtils.throwIf(!enumByValue.equals(enumByValue1), ErrorCode.NO_AUTH_ERROR);
        return joinPoint.proceed();
    }


}
