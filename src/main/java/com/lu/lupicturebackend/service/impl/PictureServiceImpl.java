package com.lu.lupicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.StrFormatter;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lu.lupicturebackend.exception.ErrorCode;
import com.lu.lupicturebackend.exception.ThrowUtils;
import com.lu.lupicturebackend.manager.FileManager;
import com.lu.lupicturebackend.model.dto.file.UploadPictureResult;
import com.lu.lupicturebackend.model.dto.picture.PictureUploadRequest;
import com.lu.lupicturebackend.model.entity.Picture;
import com.lu.lupicturebackend.model.entity.User;
import com.lu.lupicturebackend.model.vo.PictureVO;
import com.lu.lupicturebackend.service.PictureService;
import com.lu.lupicturebackend.mapper.PictureMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author 86186
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2025-05-30 10:36:16
 */
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
        implements PictureService {
    @Resource
    private FileManager fileManager;

    /**
     * 上传图片
     *
     * @param multipartFile
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    @Override
    public PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser) {
        // 判断用户是否登录
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        // 判断是新增还是修改
        if (pictureUploadRequest == null) {
            ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
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
        UploadPictureResult uploadPictureResult = fileManager.uploadPictureFile(multipartFile, uploadPathPrefix);

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
        boolean save = this.saveOrUpdate(picture);

        ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR, "上传失败");
        return PictureVO.objToVo(picture);
    }
}




