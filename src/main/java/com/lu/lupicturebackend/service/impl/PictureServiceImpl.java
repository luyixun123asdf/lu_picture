package com.lu.lupicturebackend.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lu.lupicturebackend.exception.ErrorCode;
import com.lu.lupicturebackend.exception.ThrowUtils;
import com.lu.lupicturebackend.manager.FileManager;
import com.lu.lupicturebackend.manager.upload.FilePictureUpload;
import com.lu.lupicturebackend.manager.upload.PictureUploadTemplate;
import com.lu.lupicturebackend.manager.upload.UrlPictureUpload;
import com.lu.lupicturebackend.model.dto.file.UploadPictureResult;
import com.lu.lupicturebackend.model.dto.picture.PictureQueryRequest;
import com.lu.lupicturebackend.model.dto.picture.PictureReviewRequest;
import com.lu.lupicturebackend.model.dto.picture.PictureUploadRequest;
import com.lu.lupicturebackend.model.entity.Picture;
import com.lu.lupicturebackend.model.entity.User;
import com.lu.lupicturebackend.model.enums.PictureReviewStatusEnum;
import com.lu.lupicturebackend.model.vo.PictureVO;
import com.lu.lupicturebackend.model.vo.UserVO;
import com.lu.lupicturebackend.service.PictureService;
import com.lu.lupicturebackend.mapper.PictureMapper;
import com.lu.lupicturebackend.service.UserService;
import jdk.nashorn.internal.ir.IfNode;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 86186
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2025-05-30 10:36:16
 */
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
        implements PictureService {
//    @Resource
//    private FileManager fileManager;

    @Resource
    private UserService userService;

    @Resource
    private FilePictureUpload filePictureUpload;
    @Resource
    private UrlPictureUpload urlPictureUpload;

    /**
     * 上传图片
     *
     * @param inputSource
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {
        // 判断用户是否登录
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        // 判断是新增还是修改
        if (pictureUploadRequest == null) {
            ThrowUtils.throwIf(true, ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
        if (pictureUploadRequest.getId() != null) {  // 修改
            Long id = pictureUploadRequest.getId();
            // 查询数据库
            boolean exists = this.lambdaQuery()
                    .eq(Picture::getId, id)
                    .exists();
            if (!exists) { //　
                ThrowUtils.throwIf(true, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            }
        }
        // 新增
        // 上传
        String uploadPathPrefix = String.format("public/%s", loginUser.getId());
        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        if (inputSource instanceof String) {
            pictureUploadTemplate = urlPictureUpload;
        }
//        UploadPictureResult uploadPictureResult = fileManager.uploadPictureFile(multipartFile, uploadPathPrefix);
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPictureFile(inputSource, uploadPathPrefix);
        // 构造对象存入数据库
        Picture picture = new Picture();
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setName(uploadPictureResult.getPicName());
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setUserId(loginUser.getId());
        // 操作数据库
        // 如果pictureID  不为空，则修改
        if (pictureUploadRequest.getId() != null) {
            picture.setId(pictureUploadRequest.getId());
            picture.setEditTime(new Date());
        }
        //　填充审核参数
        this.fillReviewParams(picture, loginUser);
        boolean save = this.saveOrUpdate(picture);

        ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR, "上传失败");
        return PictureVO.objToVo(picture);
    }

    /**
     * 获取图片查询
     *
     * @param pictureQueryRequest
     * @return
     */

    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (pictureQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        Long reviewerId = pictureQueryRequest.getReviewerId();
        String reviewMessage = pictureQueryRequest.getReviewMessage();

        // 从多字段中搜索
        if (StrUtil.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("name", searchText)
                    .or()
                    .like("introduction", searchText)
            );
        }
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        queryWrapper.like(StrUtil.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), "reviewerId", reviewerId);

        // JSON 数组查询
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    /**
     * 图片脱敏 (脱敏)
     *
     * @param picture
     * @param request
     * @return
     */
    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
        // 对象转封装类
        PictureVO pictureVO = PictureVO.objToVo(picture);
        // 关联查询用户信息
        Long userId = picture.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            pictureVO.setUser(userVO);
        }
        return pictureVO;
    }

    /**
     * 分页获取图片封装
     *
     * @param picturePage
     * @param request
     * @return
     */
    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize());
        // 获取图片列表
        List<Picture> pictureList = picturePage.getRecords();
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }
        // 将图片列表转为图片VO列表
        List<PictureVO> pictureVOList = pictureList.stream()
                .map(PictureVO::objToVo)
                .collect(Collectors.toList());
        // 获取用户信息
        Set<Long> collect = pictureList.stream()
                .map(Picture::getUserId)
                .collect(Collectors.toSet());
        // 查询用户信息
        Map<Long, List<User>> userMap = userService.listByIds(collect) // where id in (1,2,3)
                .stream().collect(Collectors.groupingBy(User::getId));
        // 填充信息
        pictureVOList.forEach(pictureVO -> {
            Long userId = pictureVO.getUserId();
            if (userId != null && userId > 0) {
                List<User> users = userMap.get(userId);
                if (CollUtil.isNotEmpty(users)) {
                    User user = users.get(0);
                    UserVO userVO = userService.getUserVO(user);
                    pictureVO.setUser(userVO);
                }
            }
        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }

    /**
     * 校验图片数据
     *
     * @param picture
     */
    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        // 修改数据时，id 不能为空，有参数则校验
        ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "id 不能为空");
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url 过长");
        }
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "简介过长");
        }
    }

    /**
     * 图片审核
     *
     * @param pictureReviewRequest
     * @param loginUser
     */
    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
        // 校验参数
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(pictureReviewRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        Long reviewRequestId = pictureReviewRequest.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        PictureReviewStatusEnum enumByValue = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
        if (enumByValue == null || reviewStatus == null || PictureReviewStatusEnum.REVIEWING.equals(enumByValue)) {
            ThrowUtils.throwIf(true, ErrorCode.PARAMS_ERROR, "参数错误");
        }
        // 判断是否存在图片
        Picture OldPicture = this.getById(reviewRequestId);
        ThrowUtils.throwIf(OldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        // 审核状态是否重复
        ThrowUtils.throwIf(OldPicture.getReviewStatus().equals(reviewStatus), ErrorCode.PARAMS_ERROR, "审核状态重复");
        // 数据库操作
        Picture UpdatePicture = new Picture();
        BeanUtils.copyProperties(pictureReviewRequest, UpdatePicture);
        // 更新审核状态
        UpdatePicture.setReviewerId(loginUser.getId());
        UpdatePicture.setReviewTime(new Date());
        boolean result = this.updateById(UpdatePicture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    @Override
    public void fillReviewParams(Picture picture, User loginUser) {
        if (userService.isAdmin(loginUser)) {
            picture.setReviewerId(loginUser.getId());
            picture.setReviewTime(new Date());
            // 管理员自动过审
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEW_PASS.getValue());
            picture.setReviewMessage("管理员自动过审");
        } else {
            // 非管理员，创建或编辑都要改为待审核
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }


}




