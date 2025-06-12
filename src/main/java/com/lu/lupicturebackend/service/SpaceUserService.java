package com.lu.lupicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lu.lupicturebackend.model.dto.space.SpaceAddRequest;
import com.lu.lupicturebackend.model.dto.space.SpaceQueryRequest;
import com.lu.lupicturebackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.lu.lupicturebackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.lu.lupicturebackend.model.entity.Space;
import com.lu.lupicturebackend.model.entity.SpaceUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lu.lupicturebackend.model.entity.User;
import com.lu.lupicturebackend.model.vo.SpaceUserVO;
import com.lu.lupicturebackend.model.vo.SpaceVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 86186
 * @description 针对表【space_user(空间用户关联)】的数据库操作Service
 * @createDate 2025-06-11 11:00:07
 */
public interface SpaceUserService extends IService<SpaceUser> {


    /**
     * 创建空间成员
     *
     * @param spaceUserAddRequest
     * @return
     */
    long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);


    /**
     * 空间成员校验
     *
     * @param spaceUser
     */
    void validSpaceUser(SpaceUser spaceUser, boolean add);

    /**
     * 获取空间查询
     *
     * @param spaceUserQueryRequest
     * @return
     */
    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);

    /**
     * 空间成员返回封装，进行脱敏（单条）
     *
     * @param spaceUser
     * @param request
     * @return
     */
    SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request);

    /**
     * 分页获取空间成员多条
     *
     * @param spaceUsers
     * @return
     */
    List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUsers);


}
