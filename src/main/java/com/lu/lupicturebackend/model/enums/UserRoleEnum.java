package com.lu.lupicturebackend.model.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

/**
 * 角色枚举
 */
@Getter
public enum UserRoleEnum {
    ADMIN("admin", "管理员"),
    USER("user", "普通用户");

    private final String value;
    private final String text;
    UserRoleEnum(String value, String text) {
        this.value = value;
        this.text = text;
    }

    /**
     * 根据value获取枚举
     * @param value
     * @return
     */
    public static UserRoleEnum getEnumByValue(String value) {
        if (ObjectUtil.isEmpty(value)){
            return null;
        }
        for (UserRoleEnum roleEnum : UserRoleEnum.values()) {
            if (roleEnum.value.equals(value)) {
                return roleEnum;
            }
        }
        return null;

    }
}
