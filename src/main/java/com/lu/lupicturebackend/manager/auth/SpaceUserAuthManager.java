package com.lu.lupicturebackend.manager.auth;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.lu.lupicturebackend.exception.ErrorCode;
import com.lu.lupicturebackend.exception.ThrowUtils;
import com.lu.lupicturebackend.manager.auth.model.SpaceUserAuthConfig;
import com.lu.lupicturebackend.manager.auth.model.SpaceUserRole;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 空间用成员权限管理
 */
@Component
public class SpaceUserAuthManager {
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
}
