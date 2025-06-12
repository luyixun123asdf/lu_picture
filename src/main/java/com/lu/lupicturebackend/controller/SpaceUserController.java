package com.lu.lupicturebackend.controller;

import com.lu.lupicturebackend.common.BaseResponse;
import com.lu.lupicturebackend.common.DeleteRequest;
import com.lu.lupicturebackend.common.ResultUtils;
import com.lu.lupicturebackend.exception.ErrorCode;
import com.lu.lupicturebackend.exception.ThrowUtils;
import com.lu.lupicturebackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.lu.lupicturebackend.model.dto.spaceuser.SpaceUserEditRequest;
import com.lu.lupicturebackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.lu.lupicturebackend.model.entity.SpaceUser;
import com.lu.lupicturebackend.model.entity.User;
import com.lu.lupicturebackend.model.vo.SpaceUserVO;
import com.lu.lupicturebackend.service.SpaceUserService;
import com.lu.lupicturebackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/spaceUser")
@RequiredArgsConstructor
public class SpaceUserController {

    private final SpaceUserService spaceUserService;

    private final UserService userService;


    /**
     * 添加空间成员
     *
     * @param spaceUserAddRequest
     * @param request
     */
    @PostMapping("/add")
    public BaseResponse<Long> addSpaceUser(@RequestBody SpaceUserAddRequest spaceUserAddRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(spaceUserAddRequest == null, ErrorCode.PARAMS_ERROR);
        long l = spaceUserService.addSpaceUser(spaceUserAddRequest);
        return ResultUtils.success(l);
    }


    /**
     * 删除空间成员功能
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    // TODO 目前只删除了数据库的记录数据，没有删除对象存储中的空间
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteSpace(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        // 校验参数
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        // 获取空间
        Long id = deleteRequest.getId();
        SpaceUser spaceUser = spaceUserService.getById(id);
        ThrowUtils.throwIf(spaceUser == null, ErrorCode.NOT_FOUND_ERROR);

        // 操作数据库
        boolean b = spaceUserService.removeById(id);
        ThrowUtils.throwIf(!b, ErrorCode.OPERATION_ERROR, "删除失败");
        return ResultUtils.success(true);
    }


    /**
     * 获取空间用户信息
     *
     * @param spaceUserQueryRequest
     * @param request
     */
    @GetMapping("/get")
    public BaseResponse<SpaceUser> getSpaceUser(SpaceUserQueryRequest spaceUserQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUserQueryRequest == null || spaceUserQueryRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        Long spaceId = spaceUserQueryRequest.getSpaceId();
        Long userId = spaceUserQueryRequest.getUserId();

        // 查询数据库
        SpaceUser spaceUser = spaceUserService.getOne(spaceUserService.getQueryWrapper(spaceUserQueryRequest));
        ThrowUtils.throwIf(spaceUser == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(spaceUser);
    }

    /**
     * 获取空间成员列表
     */
    @PostMapping("/list")
    public BaseResponse<List<SpaceUserVO>> listSpaceUser(@RequestBody SpaceUserQueryRequest spaceuserQueryRequest) {
        ThrowUtils.throwIf(spaceuserQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 获取列表
        List<SpaceUser> spaceUserList = spaceUserService.list(spaceUserService.getQueryWrapper(spaceuserQueryRequest));
        ThrowUtils.throwIf(spaceUserList == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(spaceUserService.getSpaceUserVOList(spaceUserList));
    }

    /**
     * 编辑空间成员
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editSpaceUser(@RequestBody SpaceUserEditRequest spaceUserEditRequest, HttpServletRequest request) {
        // 校验参数
        ThrowUtils.throwIf(spaceUserEditRequest == null || spaceUserEditRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        // 开始构造空间用户对象
        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(spaceUserEditRequest, spaceUser);
        // 参数校验
        spaceUserService.validSpaceUser(spaceUser, false);
        // 是否存在
        spaceUser = spaceUserService.getById(spaceUser.getId());
        ThrowUtils.throwIf(spaceUser == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = spaceUserService.updateById(spaceUser);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 查询加入我加入的团队列表
     */
    @PostMapping("/list/my")
    public BaseResponse<List<SpaceUserVO>> listMyTeamSpace(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_FOUND_ERROR);
        SpaceUserQueryRequest spaceUserQueryRequest = new SpaceUserQueryRequest();
        spaceUserQueryRequest.setUserId(loginUser.getId());
        List<SpaceUser> spaceUserList = spaceUserService.list(
                spaceUserService.getQueryWrapper(spaceUserQueryRequest)
        );
        return ResultUtils.success(spaceUserService.getSpaceUserVOList(spaceUserList));
    }
}
