package com.lu.lupicturebackend.manager.websocket;

import com.lu.lupicturebackend.exception.ErrorCode;
import com.lu.lupicturebackend.exception.ThrowUtils;
import com.lu.lupicturebackend.manager.auth.SpaceUserAuthManager;
import com.lu.lupicturebackend.manager.auth.model.SpaceUserPermissionConstant;
import com.lu.lupicturebackend.model.entity.Picture;
import com.lu.lupicturebackend.model.entity.Space;
import com.lu.lupicturebackend.model.entity.User;
import com.lu.lupicturebackend.model.enums.SpaceTypeEnum;
import com.lu.lupicturebackend.service.PictureService;
import com.lu.lupicturebackend.service.SpaceService;
import com.lu.lupicturebackend.service.SpaceUserService;
import com.lu.lupicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 自定义握手拦截器
 */
@Slf4j
@Component
public class WsHandshakeInterceptor implements HandshakeInterceptor {

    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    @Resource
    private SpaceService spaceService;

    @Resource
    private SpaceUserService spaceUserService;

    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;

    /**
     * 建立连接之前，进行校验
     * @param request
     * @param response
     * @param wsHandler
     * @param attributes  给websocket会话设置属性
     * @return
     * @throws Exception
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        if (request instanceof ServletServerHttpRequest) {
            HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
            // 获取当前用户
            String pictureId = servletRequest.getParameter("pictureId");
            if (pictureId == null) {
                ThrowUtils.throwIf(true, ErrorCode.PARAMS_ERROR, "缺失参数，拒绝握手");
                return false;
            }
            User loginUser = userService.getLoginUser(servletRequest);
            if (loginUser == null){
                ThrowUtils.throwIf(true, ErrorCode.NOT_LOGIN_ERROR, "未登录，拒绝握手");
                return false;
            }
            // 是否有编辑当前图片的权限
            Picture picture = pictureService.getById(pictureId);
            if (picture == null){
                log.error("图片不存在,拒绝握手");
                return false;
            }
            // 是团队空间，且有编辑权限，才建立连接
            Space space;
            if (picture.getSpaceId() != null){
                Long spaceId = picture.getSpaceId();
                space = spaceService.getById(spaceId);
                if (space == null){
                    log.error("空间不存在,拒绝握手");
                    return false;
                }
                if (space.getSpaceType() != SpaceTypeEnum.TEAM.getValue()){
                    log.error("非团队空间，拒绝握手");
                    return false;
                }
                List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
                if (!permissionList.contains(SpaceUserPermissionConstant.PICTURE_EDIT)){
                    log.error("无编辑权限，拒绝握手");
                    return false;
                }
                attributes.put("user", loginUser);
                attributes.put("userId", loginUser.getId());
                attributes.put("pictureId",  Long.parseLong(pictureId));
            }
        }
        // 保存登录信息到websocket的session中
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
