package com.lu.lupicturebackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lu.lupicturebackend.annotation.AuthCheck;
import com.lu.lupicturebackend.common.BaseResponse;
import com.lu.lupicturebackend.common.DeleteRequest;
import com.lu.lupicturebackend.common.ResultUtils;
import com.lu.lupicturebackend.constant.UserConstant;
import com.lu.lupicturebackend.exception.ErrorCode;
import com.lu.lupicturebackend.exception.ThrowUtils;
import com.lu.lupicturebackend.model.dto.user.*;
import com.lu.lupicturebackend.model.entity.User;
import com.lu.lupicturebackend.model.vo.LoginUserVO;
import com.lu.lupicturebackend.model.vo.UserVO;
import com.lu.lupicturebackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        Long register = userService.userRegister(userRegisterRequest.getUserAccount(), userRegisterRequest.getUserPassword(), userRegisterRequest.getCheckPassword());
        return ResultUtils.success(register);

    }

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        LoginUserVO loginUserVO = userService.userLogin(userLoginRequest.getUserAccount(), userLoginRequest.getUserPassword(), request);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        LoginUserVO loginUserVO = userService.getLoginUserVO(userService.getLoginUser(request));
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 退出登录
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 创建用户,管理员
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest user) {
        ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR);
        User newUser = new User();
        BeanUtils.copyProperties(user, newUser);
        // 加密
        final String DEFAULT_PASSWORD = "12345678";
        newUser.setUserPassword(userService.getEncryptPassword(DEFAULT_PASSWORD));
        boolean result = userService.save(newUser);
        if (!result) {
            ThrowUtils.throwIf(true, ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(newUser.getId());
    }

    /**
     * 删除用户,管理员
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        boolean result = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(result);
    }

    /**
     * 更新用户,管理员
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest user) {
        ThrowUtils.throwIf(user == null || user.getId() <= 0, ErrorCode.PARAMS_ERROR);
        User oldUser = userService.getById(user.getId());
        if (oldUser == null) {
            ThrowUtils.throwIf(true, ErrorCode.NOT_FOUND_ERROR);
        }
        User newUser = new User();
        BeanUtils.copyProperties(user, newUser);
        boolean result = userService.updateById(newUser);
        ThrowUtils.throwIf(!result, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 获取用户,管理员
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(@RequestParam long id) {
        User user = userService.getById(id);
        return ResultUtils.success(user);
    }

    /**
     * 获取用户包装类
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(@RequestParam long id) {
        User user = userService.getById(id);
        UserVO userVO = userService.getUserVO(user);
        return ResultUtils.success(userVO);
    }

    /**
     * 分页获取用户封装类
     * @param userQueryRequest
     * @return
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<User> userPage = userService.page(new Page<>(current, size), userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotal());
        List<UserVO> userVO = userService.getUserVOList(userPage.getRecords());
        userVOPage.setRecords(userVO);
        return ResultUtils.success(userVOPage);
    }


}
