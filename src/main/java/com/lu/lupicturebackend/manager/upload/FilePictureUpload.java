package com.lu.lupicturebackend.manager.upload;


import cn.hutool.core.io.FileUtil;
import com.lu.lupicturebackend.config.CosClientConfig;
import com.lu.lupicturebackend.exception.BusinessException;
import com.lu.lupicturebackend.exception.ErrorCode;
import com.lu.lupicturebackend.exception.ThrowUtils;
import com.lu.lupicturebackend.manager.CosManager;
import com.lu.lupicturebackend.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * 文件图片上传
 */
@Service
public class FilePictureUpload extends PictureUploadTemplate {
    @Resource
    private CosManager cosManager;




    @Override
    protected PutObjectResult uploadToCOS(Object inputResource, String bucket, String uploadPath, UploadPictureResult pictureResult) {
        try {
            MultipartFile multipartFile =(MultipartFile) inputResource;
            PutObjectResult pictureObject = cosManager.uploadToCOS(multipartFile, bucket, uploadPath);
            pictureResult.setPicSize(multipartFile.getSize());
            return pictureObject;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }

    }

    @Override
    protected String getOriginalFilename(Object inputResource) {
        MultipartFile multipartFile = (MultipartFile) inputResource;
        return multipartFile.getOriginalFilename();
    }

    @Override
    protected String validPicture(Object inputResource) {
        MultipartFile file = (MultipartFile) inputResource;
        // 校验文件大小
        final long fileSize = 1024 * 1024;
        ThrowUtils.throwIf(file.getSize() > 2 * fileSize, ErrorCode.PARAMS_ERROR, "文件大小不能超过 1M");
        // 校验文件是否为空
        ThrowUtils.throwIf(file.isEmpty(), ErrorCode.PARAMS_ERROR, "文件不能为空");
        // 校验文件后缀
        String fileName = file.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        final List<String> allowSuffixList = Arrays.asList(".png", ".jpg", ".jpeg", ".gif", ".webp", ".svg");
        ThrowUtils.throwIf(!allowSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "文件类型错误");
        return "";
    }
}
