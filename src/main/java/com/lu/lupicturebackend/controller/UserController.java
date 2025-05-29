package com.lu.lupicturebackend.controller;

import com.lu.lupicturebackend.common.BaseResponse;
import com.lu.lupicturebackend.common.ResultUtils;
import com.lu.lupicturebackend.exception.ErrorCode;
import com.lu.lupicturebackend.exception.ThrowUtils;
import com.lu.lupicturebackend.model.dto.UserRegisterRequest;
import com.lu.lupicturebackend.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;


    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        Long register = userService.userRegister(userRegisterRequest.getUserAccount(), userRegisterRequest.getUserPassword(), userRegisterRequest.getCheckPassword());
        return ResultUtils.success(register);

    }

}
