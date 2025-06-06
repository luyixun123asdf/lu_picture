package com.lu.lupicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lu.lupicturebackend.model.dto.space.SpaceAddRequest;
import com.lu.lupicturebackend.model.dto.space.SpaceQueryRequest;
import com.lu.lupicturebackend.model.entity.Space;
import com.lu.lupicturebackend.model.entity.User;
import com.lu.lupicturebackend.model.vo.SpaceVO;


import javax.servlet.http.HttpServletRequest;

/**
 * @author 86186
 * @description 针对表【space(空间)】的数据库操作Service
 * @createDate 2025-06-05 10:16:19
 */
public interface SpaceService extends IService<Space> {

    /**
     * 创建空间
     *
     * @param spaceAddRequest
     * @param loginUser
     * @return
     */
    public long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);


    /**
     * 空间校验
     *
     * @param space
     */
    public void validSpace(Space space,boolean add);

    /**
     * 获取空间查询
     *
     * @param spaceQueryRequest
     * @return
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    /**
     * 空间返回封装，进行脱敏（单条）
     *
     * @param space
     * @param request
     * @return
     */
    SpaceVO getSpaceVO(Space space, HttpServletRequest request);

    /**
     * 分页获取空间封装
     *
     * @param spacePage
     * @param request
     * @return
     */
    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);

    /**
     *
     * 根据空间级别填充空间信息
     * @param space
     */
    void fillSpaceBySpaceLevel(Space space);


    /**
     * 删除空间同时删除修相关的照片
     */
    void deleteSpaceAndPicture(long spaceId,  User loginUser);


}
