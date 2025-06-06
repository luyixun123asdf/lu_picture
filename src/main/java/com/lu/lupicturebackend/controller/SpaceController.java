package com.lu.lupicturebackend.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lu.lupicturebackend.annotation.AuthCheck;
import com.lu.lupicturebackend.common.BaseResponse;
import com.lu.lupicturebackend.common.DeleteRequest;
import com.lu.lupicturebackend.common.ResultUtils;
import com.lu.lupicturebackend.constant.UserConstant;
import com.lu.lupicturebackend.exception.ErrorCode;
import com.lu.lupicturebackend.exception.ThrowUtils;
import com.lu.lupicturebackend.model.dto.space.*;
import com.lu.lupicturebackend.model.entity.Space;
import com.lu.lupicturebackend.model.entity.User;
import com.lu.lupicturebackend.model.enums.SpaceLevelEnum;
import com.lu.lupicturebackend.model.vo.SpaceVO;
import com.lu.lupicturebackend.service.SpaceService;
import com.lu.lupicturebackend.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;

import org.springframework.web.bind.annotation.*;


import javax.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/space")
@AllArgsConstructor
public class SpaceController {

    private final SpaceService spaceService;

    private final UserService userService;

    /**
     * 添加空间
     *
     * @param spaceAddRequest
     * @param request
     */
    @PostMapping("/add")
    public BaseResponse<Long> addSpace(@RequestBody SpaceAddRequest spaceAddRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(spaceAddRequest == null, ErrorCode.PARAMS_ERROR);
        long l = spaceService.addSpace(spaceAddRequest, loginUser);
        return ResultUtils.success(l);
    }
    /**
     * 空间删除功能
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
        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取用户
        User loginUser = userService.getLoginUser(request);
        // 只有管理员或者空间上传者才允许删除
        if (!oldSpace.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            ThrowUtils.throwIf(true, ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean b = spaceService.removeById(id);
        ThrowUtils.throwIf(!b, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新空间（仅管理员可用）
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest,
                                             HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUpdateRequest == null || spaceUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        // 构造空间对象
        Space space = new Space();
        // 获取空间
        Long id = spaceUpdateRequest.getId();
        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        BeanUtils.copyProperties(spaceUpdateRequest, space);

        // 数据校验
        spaceService.validSpace(space,false);
        // 自动数据填充
        spaceService.fillSpaceBySpaceLevel(space);
        // 操作数据库
        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);

    }

    /**
     * 根据 id 获取空间（封装类）
     */
    @GetMapping("/get/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<SpaceVO> getSpaceVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(spaceService.getSpaceVO(space, request));
    }

    /**
     * 根据 id 获取空间（仅管理员可用）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Space> getSpaceById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(space);
    }

    /**
     * z45
     * 分页获取空间列表（仅管理员可用）
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Space>> listSpaceByPage(@RequestBody SpaceQueryRequest spaceQueryRequest) {
        ThrowUtils.throwIf(spaceQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 获取分页参数,可以根据各个字段进行查询，没有的字段默认为空
        Page<Space> page = spaceService.page(new Page<>(spaceQueryRequest.getCurrent(), spaceQueryRequest.getPageSize()), spaceService.getQueryWrapper(spaceQueryRequest));
        return ResultUtils.success(page);
    }

    /**
     * 分页获取空间列表（封装类）
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<SpaceVO>> listSpaceVOByPage(@RequestBody SpaceQueryRequest spaceQueryRequest, HttpServletRequest request) {
        long current = spaceQueryRequest.getCurrent();
        long size = spaceQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);

        // 查询数据库
        Page<Space> spacePage = spaceService.page(new Page<>(current, size), spaceService.getQueryWrapper(spaceQueryRequest));
        // 获取封装类
        Page<SpaceVO> spaceVOPage = spaceService.getSpaceVOPage(spacePage, request);

        return ResultUtils.success(spaceVOPage);

    }

    /**
     * 编辑空间（给用户使用）
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editSpace(@RequestBody SpaceEditRequest spaceEditRequest, HttpServletRequest request) {
        // 校验参数
        ThrowUtils.throwIf(spaceEditRequest == null || spaceEditRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        // 开始构造空间对象
        Space space = new Space();
        BeanUtils.copyProperties(spaceEditRequest, space);
        // 设置编辑时间
        space.setEditTime(new Date());
        // 数据校验
        spaceService.validSpace(space,false);
        // 数据填充
        spaceService.fillSpaceBySpaceLevel(space);
        // 权限校验
        User loginUser = userService.getLoginUser(request);
        Space oldSpace = spaceService.getById(spaceEditRequest.getId());
        //  只有管理员或者空间上传者才允许修改
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NO_AUTH_ERROR);
        if (!oldSpace.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            ThrowUtils.throwIf(true, ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 获取空间级别列表，便于前端展示
     * @return
     */
    @GetMapping("/list/level")
    public  BaseResponse<List<SpaceLevel>> listSpaceLevel() {
        List<SpaceLevel> spaceLevelList = Arrays.stream(SpaceLevelEnum.values())
                .map(spaceLevelEnum -> new SpaceLevel(spaceLevelEnum.getValue(),
                        spaceLevelEnum.getText(),
                        spaceLevelEnum.getMaxCount(),
                        spaceLevelEnum.getMaxSize()))
                .collect(Collectors.toList());
        return ResultUtils.success(spaceLevelList);
    }


}
