package com.lu.lupicturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lu.lupicturebackend.constant.UserConstant;
import com.lu.lupicturebackend.exception.BusinessException;
import com.lu.lupicturebackend.exception.ErrorCode;
import com.lu.lupicturebackend.exception.ThrowUtils;
import com.lu.lupicturebackend.model.dto.user.UserQueryRequest;
import com.lu.lupicturebackend.model.entity.User;
import com.lu.lupicturebackend.model.vo.LoginUserVO;
import com.lu.lupicturebackend.model.vo.UserVO;
import com.lu.lupicturebackend.service.UserService;
import com.lu.lupicturebackend.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 86186
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2025-05-29 11:50:40
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户id
     */
    @Override
    public Long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword, checkPassword), ErrorCode.PARAMS_ERROR, "参数为空");
        ThrowUtils.throwIf(userAccount.length() < 4, ErrorCode.PARAMS_ERROR, "用户账号过短");
        ThrowUtils.throwIf(userPassword.length() < 8 || checkPassword.length() < 8, ErrorCode.PARAMS_ERROR, "用户密码过短");
        ThrowUtils.throwIf(!userPassword.equals(checkPassword), ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        // 校验账户不能重复
        Long userAccount1 = this.baseMapper.selectCount(new QueryWrapper<User>()
                .eq("userAccount", userAccount));
        ThrowUtils.throwIf(userAccount1 > 0, ErrorCode.PARAMS_ERROR, "用户账号重复");

        // 2. 加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 3. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserRole("user");
        user.setUserName("无名之人");
        boolean save = this.save(user);
        ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR, "注册失败");
        // 4. 返回用户id
        return user.getId();
    }

    /**
     * 密码加密处理
     *
     * @param password
     * @return
     */
    @Override
    public String getEncryptPassword(String password) {
        final String salt = "lyx-picture";
        return DigestUtils.md5DigestAsHex((salt + password).getBytes());
    }

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return
     */
    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword), ErrorCode.PARAMS_ERROR, "参数为空");
        ThrowUtils.throwIf(userAccount.length() < 4, ErrorCode.PARAMS_ERROR, "用户账号错误");
        ThrowUtils.throwIf(userPassword.length() < 8, ErrorCode.PARAMS_ERROR, "用户密码错误");
        // 2. 登录校验
        String password = getEncryptPassword(userPassword);
        QueryWrapper<User> eq = new QueryWrapper<User>()
                .eq("userAccount", userAccount)
                .eq("userPassword", password);
        // 3. 获取用户信息
        User user = this.baseMapper.selectOne(eq);
        ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR, "用户不存在或者密码错误");
        log.info("用户登录成功");
        // 4. 封装用户信息
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    /**
     * 获取脱敏后的登录用户信息
     *
     * @param user
     * @return
     */
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        ;
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        ;
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return null;
        }
        return userList.stream()
                .map(user -> getUserVO(user))
                .collect(Collectors.toList());
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        Object attribute = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) attribute;
        if (currentUser == null || currentUser.getId() == null) {
            ThrowUtils.throwIf(true, ErrorCode.NOT_LOGIN_ERROR);
        }
        currentUser = this.getById(currentUser.getId());
        ThrowUtils.throwIf(currentUser == null, ErrorCode.NOT_LOGIN_ERROR);
        return currentUser;
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        Object attribute = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (attribute == null) {
            ThrowUtils.throwIf(true, ErrorCode.NOT_LOGIN_ERROR);
        }
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);

        return true;
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,  "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
        queryWrapper.like(StrUtil.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.eq(StrUtil.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.orderBy(StrUtil.isNotBlank(sortField),"ascend".equals(sortOrder),sortField);

        return queryWrapper;
    }
}




