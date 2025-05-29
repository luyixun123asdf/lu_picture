package com.lu.lupicturebackend.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lu.lupicturebackend.exception.ErrorCode;
import com.lu.lupicturebackend.exception.ThrowUtils;
import com.lu.lupicturebackend.model.entity.User;
import com.lu.lupicturebackend.service.UserService;
import com.lu.lupicturebackend.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
 * @author 86186
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2025-05-29 11:50:40
 */
@Service
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
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword, checkPassword),  ErrorCode.PARAMS_ERROR,"参数为空");
        ThrowUtils.throwIf(userAccount.length() < 4, ErrorCode.PARAMS_ERROR,"用户账号过短");
        ThrowUtils.throwIf(userPassword.length() < 8 || checkPassword.length() < 8, ErrorCode.PARAMS_ERROR,"用户密码过短");
        ThrowUtils.throwIf(!userPassword.equals(checkPassword), ErrorCode.PARAMS_ERROR,"两次输入的密码不一致");
        // 校验账户不能重复
        Long userAccount1 = this.baseMapper.selectCount(new QueryWrapper<User>()
                .eq("userAccount", userAccount));
        ThrowUtils.throwIf(userAccount1 > 0, ErrorCode.PARAMS_ERROR,"用户账号重复");

        // 2. 加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 3. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserRole("user");
        user.setUserName("无名之人");
        boolean save = this.save(user);
        ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR,"注册失败");
        // 4. 返回用户id
        return user.getId();
    }

    @Override
    public String getEncryptPassword(String password) {
        final String salt = "lyx-picture";
        return DigestUtils.md5DigestAsHex((salt+password).getBytes());
    }
}




