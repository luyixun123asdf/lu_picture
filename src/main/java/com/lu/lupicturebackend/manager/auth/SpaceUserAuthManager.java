package com.lu.lupicturebackend.manager.auth;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.lu.lupicturebackend.exception.ErrorCode;
import com.lu.lupicturebackend.exception.ThrowUtils;
import com.lu.lupicturebackend.manager.auth.model.SpaceUserAuthConfig;
import com.lu.lupicturebackend.manager.auth.model.SpaceUserPermissionConstant;
import com.lu.lupicturebackend.manager.auth.model.SpaceUserRole;
import com.lu.lupicturebackend.model.entity.Space;
import com.lu.lupicturebackend.model.entity.SpaceUser;
import com.lu.lupicturebackend.model.entity.User;
import com.lu.lupicturebackend.model.enums.SpaceRoleEnum;
import com.lu.lupicturebackend.model.enums.SpaceTypeEnum;
import com.lu.lupicturebackend.service.SpaceUserService;
import com.lu.lupicturebackend.service.UserService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 空间用成员权限管理
 */
@Component
public class SpaceUserAuthManager {

    @Resource
    private UserService userService;

    @Resource
    private SpaceUserService spaceUserService;

    public static final SpaceUserAuthConfig SPACE_USER_AUTH_CONFIG;

    static {
        String s = ResourceUtil.readUtf8Str("biz/spaceUserAuthConfig.json");
        SPACE_USER_AUTH_CONFIG = JSONUtil.toBean(s, SpaceUserAuthConfig.class);
    }

    /**
     * 通过角色获取权限
     *
     * @param role
     * @return
     */

    public List<String> getPermissionsByRole(String role) {
        ThrowUtils.throwIf(StrUtil.isBlank(role), ErrorCode.NOT_FOUND_ERROR);
        SpaceUserRole spaceUserRole = SPACE_USER_AUTH_CONFIG.getRoles().stream()
                .filter(SpaceRole -> SpaceRole.getKey().equals(role))
                .findFirst()
                .orElse(null
                );
        if (spaceUserRole == null) {
            return null;
        }
        return spaceUserRole.getPermissions();
    }


    /**
     * 获取权限列表
     */
    public List<String> getPermissionList(Space space, User loginUser) {
        if (loginUser == null) {
            return new ArrayList<>();
        }
        // 管理员权限
        List<String> ADMIN_PERMISSIONS = getPermissionsByRole(SpaceRoleEnum.ADMIN.getValue());
        // 公共图库
        if (space == null) {
            if (userService.isAdmin(loginUser)) {
                return ADMIN_PERMISSIONS;
            }
            return Collections.singletonList(SpaceUserPermissionConstant.PICTURE_VIEW);
        }
        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(space.getSpaceType());
        if (spaceTypeEnum == null) {
            return new ArrayList<>();
        }
        // 根据空间获取对应的权限
        switch (spaceTypeEnum) {
            case PRIVATE:
                // 私有空间本人和管理员才有权限
                if (space.getUserId().equals(loginUser.getId()) || userService.isAdmin(loginUser)) {
                    return ADMIN_PERMISSIONS;
                } else {
                    return new ArrayList<>();
                }
            case TEAM:
                //  团队空间
                SpaceUser spaceUser = spaceUserService.lambdaQuery()
                        .eq(SpaceUser::getSpaceId, space.getId())
                        .eq(SpaceUser::getUserId, loginUser.getId())
                        .one();
                if (spaceUser == null) {
                    return new ArrayList<>();
                } else {
                    return getPermissionsByRole(spaceUser.getSpaceRole());
                }


        }
        return new ArrayList<>();
    }
}
