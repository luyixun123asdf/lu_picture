package com.lu.lupicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lu.lupicturebackend.model.dto.user.UserQueryRequest;
import com.lu.lupicturebackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lu.lupicturebackend.model.vo.LoginUserVO;
import com.lu.lupicturebackend.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 86186
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2025-05-29 11:50:40
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户id
     */
    Long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 获取加密后的密码
     *
     * @param password
     * @return
     */
    String getEncryptPassword(String password);

    /**
     * 登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 得到脱敏后的登录用户信息
     *
     * @param user
     * @return
     */

    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取脱敏后的用户信息
     *
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 获取当前登录用户(系统内部使用)
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取查询
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

}
