package com.lu.lupicturebackend.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.lu.lupicturebackend.annotation.AuthCheck;
import com.lu.lupicturebackend.api.aliyunai.AliYunAiApi;
import com.lu.lupicturebackend.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.lu.lupicturebackend.api.aliyunai.model.GetOutPaintingTaskResponse;
import com.lu.lupicturebackend.api.imagesearch.ImageSearchApiFacade;
import com.lu.lupicturebackend.api.imagesearch.model.ImageSearchResult;
import com.lu.lupicturebackend.common.BaseResponse;
import com.lu.lupicturebackend.common.DeleteRequest;
import com.lu.lupicturebackend.common.ResultUtils;
import com.lu.lupicturebackend.constant.UserConstant;
import com.lu.lupicturebackend.exception.BusinessException;
import com.lu.lupicturebackend.exception.ErrorCode;
import com.lu.lupicturebackend.exception.ThrowUtils;
import com.lu.lupicturebackend.manager.cache.CacheManager;
import com.lu.lupicturebackend.model.dto.picture.*;
import com.lu.lupicturebackend.model.entity.Picture;
import com.lu.lupicturebackend.model.entity.Space;
import com.lu.lupicturebackend.model.entity.User;
import com.lu.lupicturebackend.model.enums.PictureReviewStatusEnum;
import com.lu.lupicturebackend.model.vo.PictureTagCategory;
import com.lu.lupicturebackend.model.vo.PictureVO;
import com.lu.lupicturebackend.service.PictureService;
import com.lu.lupicturebackend.service.SpaceService;
import com.lu.lupicturebackend.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/picture")
@AllArgsConstructor
public class PictureController {

    private final PictureService pictureService;

    private final UserService userService;

//    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 本地缓存caffeine
     */
//    private final Cache<String, String> LOCAL_CACHE =
//            Caffeine.newBuilder().initialCapacity(1024)
//                    .maximumSize(10000L) // 最大1w条
//                    // 缓存 5 分钟移除
//                    .expireAfterWrite(5L, TimeUnit.MINUTES)
//                    .build();
//
//    private final CacheManager cacheManager;

    private final SpaceService spaceService;


    @Resource
    private AliYunAiApi aliYunAiApi;


    /**
     * 根据文件上传图片（可重新上传）
     */
    @PostMapping("/upload")
    public BaseResponse<PictureVO> uploadPicture(@RequestPart("file") MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 根据url上传图片（可重新上传）
     */
    @PostMapping("/upload/url")
    public BaseResponse<PictureVO> uploadPictureByUrl(@RequestBody PictureUploadRequest pictureUploadRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        // 获取url
        String fileUrl = pictureUploadRequest.getFileUrl();
        PictureVO pictureVO = pictureService.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 批量抓取图片并创建图片
     */
    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> uploadPictureByBatch(@RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);

        Integer uploadCount = pictureService.batchUploadPicture(pictureUploadByBatchRequest, loginUser);
        return ResultUtils.success(uploadCount);
    }

    /**
     * 图片删除功能
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    // TODO 目前只删除了数据库的记录数据，没有删除对象存储中的图片
    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        // 校验参数
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        // 获取图片
        Long id = deleteRequest.getId();
        // 获取用户
        User loginUser = userService.getLoginUser(request);
        pictureService.deletePicture(id, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 更新图片（仅管理员可用）
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest,
                                               HttpServletRequest request) {
        ThrowUtils.throwIf(pictureUpdateRequest == null || pictureUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        // 构造图片对象
        Picture picture = new Picture();
        // 获取图片
        Long id = pictureUpdateRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        BeanUtils.copyProperties(pictureUpdateRequest, picture);
        // 注意将 list 转为 string
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));
        // 数据校验
        pictureService.validPicture(picture);
        // 补充审核参数
        User loginUser = userService.getLoginUser(request);
        pictureService.fillReviewParams(picture, loginUser);
        // 操作数据库
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);

    }

    /**
     * 根据 id 获取图片（封装类）
     */
    @GetMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Picture picture = pictureService.getById(id);
        //　空间权限校验
        User loginUser = userService.getLoginUser(request);
        pictureService.checkPictureAuth(picture, loginUser);


        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(pictureService.getPictureVO(picture, request));
    }

    /**
     * 根据 id 获取图片（仅管理员可用）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPictureById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(picture);
    }

    /**
     * z45
     * 分页获取图片列表（仅管理员可用）
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 获取分页参数,可以根据各个字段进行查询，没有的字段默认为空
        Page<Picture> page = pictureService.page(new Page<>(pictureQueryRequest.getCurrent(), pictureQueryRequest.getPageSize()), pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(page);
    }

//    /**
//     * 分页获取图片列表（封装类）使用缓存
//     */
//    @PostMapping("/list/page/vo")
//    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
//        long current = pictureQueryRequest.getCurrent();
//        long size = pictureQueryRequest.getPageSize();
//        // 1、限制爬虫
//        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
//        // 2、普通用户只能查看审核通过的图片
//        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.REVIEW_PASS.getValue());
//        // 3、查询缓存，没有再查询数据库
//        String queryCondition  = JSONUtil.toJsonStr(pictureQueryRequest); // 构建key
//        String hashKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
//        String cacheKey = String.format("luPicture:PictureVOByPage:%s", hashKey);
//        // 空间权限校验
//        Long spaceId = pictureQueryRequest.getSpaceId();
//        if (spaceId == null) {
//            // 公共图片
//            pictureQueryRequest.setNullSpaceId(true);
//        }else {
//            // 私有空间
//            User loginUser = userService.getLoginUser(request);
//            Space space = spaceService.getById(spaceId);
//            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR,  "空间不存在");
//            if (!space.getUserId().equals(loginUser.getId())){
//                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无空间权限");
//            }
//        }
//
//        return cacheManager.queryWithCache(LOCAL_CACHE, cacheKey, stringRedisTemplate, () -> {
//            Page<Picture> picturePage = pictureService.page(new Page<>(current, size), pictureService.getQueryWrapper(pictureQueryRequest));
//            Page<PictureVO> pictureVOPage = pictureService.getPictureVOPage(picturePage, request);
//            return pictureVOPage;
//        });
//
//    }

    /**
     * 分页获取图片列表（封装类）
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);

        // 空间权限校验
        Long spaceId = pictureQueryRequest.getSpaceId();
        if (spaceId == null) {
            // 公共图片
            // 普通用户只能查看审核通过的图片
            pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.REVIEW_PASS.getValue());
            pictureQueryRequest.setNullSpaceId(true);
        } else {
            // 私有空间
            User loginUser = userService.getLoginUser(request);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            if (!space.getUserId().equals(loginUser.getId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无空间权限");
            }
        }
        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size), pictureService.getQueryWrapper(pictureQueryRequest));
        // 获取封装类
        Page<PictureVO> pictureVOPage = pictureService.getPictureVOPage(picturePage, request);

        return ResultUtils.success(pictureVOPage);

    }


    /**
     * 编辑图片（给用户使用）
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
        // 校验参数
        ThrowUtils.throwIf(pictureEditRequest == null || pictureEditRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        pictureService.editPicture(pictureEditRequest, request);
        return ResultUtils.success(true);
    }

    /**
     * 获取预置标签和分类
     *
     * @return
     */
    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
        List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }

    /**
     * 图片审核
     */
    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureQueryRequest == null || pictureQueryRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.doPictureReview(pictureQueryRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 以图搜图
     */
    @PostMapping("/search/picture")
    public BaseResponse<List<ImageSearchResult>> searchPictureByPicture(@RequestBody SearchPictureByPictureRequest searchPictureByPictureRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(searchPictureByPictureRequest == null || searchPictureByPictureRequest.getPictureId() <= 0, ErrorCode.PARAMS_ERROR);
        Long pictureId = searchPictureByPictureRequest.getPictureId();
        ThrowUtils.throwIf(pictureId == null, ErrorCode.PARAMS_ERROR, "图片id不能为空");
        Picture picture = pictureService.getById(pictureId);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        if (picture.getPicFormat().equals("webp")) {
            picture.setUrl(picture.getThumbnailUrl());
        }
        return ResultUtils.success(ImageSearchApiFacade.searchImage(picture.getUrl()));
    }

    /**
     * 按照颜色搜图
     */

    @PostMapping("/search/color")
    public BaseResponse<List<PictureVO>> searchPictureByColor(@RequestBody SearchPictureByColorRequest searchPictureByColorRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(searchPictureByColorRequest == null || searchPictureByColorRequest.getSpaceId() <= 0, ErrorCode.PARAMS_ERROR);
        Long spaceId = searchPictureByColorRequest.getSpaceId();
        String picColor = searchPictureByColorRequest.getPicColor();
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(pictureService.searchPictureByColor(spaceId, picColor, loginUser));
    }

    /**
     * 批量编辑图片
     */
    @PostMapping("/edit/batch")
    public BaseResponse<Boolean> editPictureBatch(@RequestBody PictureEditByBatchRequest pictureEditBatchRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureEditBatchRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.editPictureByBatch(pictureEditBatchRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 创建 AI 扩图任务
     */
    @PostMapping("/out_painting/create_task")
    public BaseResponse<CreateOutPaintingTaskResponse> createPictureOutPaintingTask(
            @RequestBody CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest,
            HttpServletRequest request) {
        if (createPictureOutPaintingTaskRequest == null || createPictureOutPaintingTaskRequest.getPictureId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        CreateOutPaintingTaskResponse response = pictureService.createPictureOutPaintingTask(createPictureOutPaintingTaskRequest, loginUser);
        return ResultUtils.success(response);
    }

    /**
     * 查询 AI 扩图任务
     */
    @GetMapping("/out_painting/get_task")
    public BaseResponse<GetOutPaintingTaskResponse> getPictureOutPaintingTask(String taskId) {
        ThrowUtils.throwIf(StrUtil.isBlank(taskId), ErrorCode.PARAMS_ERROR);
        GetOutPaintingTaskResponse task = aliYunAiApi.getOutPaintingTask(taskId);
        return ResultUtils.success(task);
    }

}
