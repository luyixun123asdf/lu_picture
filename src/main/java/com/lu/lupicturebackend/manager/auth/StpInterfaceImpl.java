package com.lu.lupicturebackend.manager.auth;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Component
public class StpInterfaceImpl implements StpInterface {

    // 默认是api
    @Value("${server.servlet.context-path}")
    private String contextPath;


    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return new ArrayList<>();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return new ArrayList<>();
    }

    /**
     * 从请求中获取上下文对象
     */
    private SpaceUserAuthContext getAuthContext() {
        // 获取request
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        String contentType = request.getHeader(Header.CONTENT_TYPE.getValue());
        SpaceUserAuthContext spaceUserAuthContext;
        // 获取请求参数
        if (ContentType.JSON.getValue().equals(contentType)) { // 说明是json
            String body = ServletUtil.getBody(request);
            spaceUserAuthContext = JSONUtil.toBean(body, SpaceUserAuthContext.class);
        } else {
            Map<String, String> paramMap = ServletUtil.getParamMap(request);
            spaceUserAuthContext = BeanUtil.toBean(paramMap, SpaceUserAuthContext.class);
        }
        Long id = spaceUserAuthContext.getId();
        if (id != null) {
            String requestURI = request.getRequestURI();
            // 先替换上下文，剩下的就是前缀了
            String replace = requestURI.replace(contextPath + "/", "");
            String moduleName = StrUtil.subBefore(replace, "/", false);
            switch (moduleName) {
                case "picture":
                    spaceUserAuthContext.setPictureId(id);
                    break;
                case "spaceUser":
                    spaceUserAuthContext.setSpaceUserId(id);
                    break;
                case "space":
                    spaceUserAuthContext.setSpaceId(id);
                    break;
                default:
            }
        }
        return spaceUserAuthContext;
    }
}
