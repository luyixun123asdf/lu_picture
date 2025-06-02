package com.lu.lupicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lu.lupicturebackend.model.dto.picture.PictureQueryRequest;
import com.lu.lupicturebackend.model.dto.picture.PictureReviewRequest;
import com.lu.lupicturebackend.model.dto.picture.PictureUploadByBatchRequest;
import com.lu.lupicturebackend.model.dto.picture.PictureUploadRequest;
import com.lu.lupicturebackend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lu.lupicturebackend.model.entity.User;
import com.lu.lupicturebackend.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 86186
 * @description 针对表【picture(图片)】的数据库操作Service
 * @createDate 2025-05-30 10:36:16
 */
public interface PictureService extends IService<Picture> {


    /**
     * 上传图片
     *
     * @param inputResource
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(Object inputResource,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);

    /**
     * 获取图片查询
     *
     * @param pictureQueryRequest
     * @return
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 图片返回封装，进行脱敏（单条）
     *
     * @param picture
     * @param request
     * @return
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    /**
     * 分页获取图片封装
     *
     * @param picturePage
     * @param request
     * @return
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    /**
     * 图片校验
     *
     * @param picture
     */
    public void validPicture(Picture picture);


    /**
     * 图片审核
     *
     * @param loginUser
     * @param pictureReviewRequest
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    /**
     * 图片填充审核参数
     *
     * @param picture
     * @param loginUser
     */
    void fillReviewParams(Picture picture, User loginUser);

    /**
     * 批量抓取图片
     * @param pictureUploadByBatchRequest
     * @param loginUser
     */
    Integer batchUploadPicture(PictureUploadByBatchRequest  pictureUploadByBatchRequest, User loginUser);
}
