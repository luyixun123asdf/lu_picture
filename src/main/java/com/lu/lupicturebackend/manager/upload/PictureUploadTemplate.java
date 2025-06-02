package com.lu.lupicturebackend.manager.upload;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.NumberUtil;
import com.lu.lupicturebackend.config.CosClientConfig;
import com.lu.lupicturebackend.exception.BusinessException;
import com.lu.lupicturebackend.exception.ErrorCode;
import com.lu.lupicturebackend.manager.CosManager;
import com.lu.lupicturebackend.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.File;
import java.util.Date;


@Slf4j
public abstract class PictureUploadTemplate {

    @Resource
    private CosClientConfig cosClientConfig;



    /**
     * 上传图片
     *
     * @param inputResource    文件
     * @param uploadPathPrefix 上传路径前缀
     */
    public UploadPictureResult uploadPictureFile(Object inputResource, String uploadPathPrefix) {
        // 校验文件
        String preFxi = validPicture(inputResource); //　url时会使用prefix
        //　上传图片

        String uuid = UUID.randomUUID().toString();
        String originalFilename = getOriginalFilename(inputResource,preFxi); // 完整的文件名
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFileName);
        try {
            // 构造对象
            UploadPictureResult pictureResult = new UploadPictureResult();
            PutObjectResult pictureObject = uploadToCOS(inputResource, cosClientConfig.getBucket(), uploadPath, pictureResult); // 直接上传到对象存储，不要保存为临时文件
            // 获取图片信息
            ImageInfo imageInfo = pictureObject.getCiUploadResult().getOriginalInfo().getImageInfo();

            return builderResult(inputResource, originalFilename, uploadPath, imageInfo, pictureResult);
        } catch (Exception e) {
            log.error("图片上传到对象存储失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        } finally {
//            deleteTempFile(tempFile);
        }

    }

    /**
     * 封装返回结果
     *
     * @param inputResource
     * @param originalFilename
     * @param uploadPath
     * @param imageInfo
     * @return
     */

    private UploadPictureResult builderResult(Object inputResource, String originalFilename, String uploadPath, ImageInfo imageInfo, UploadPictureResult pictureResult) {
        // 封装返回结果
//        UploadPictureResult pictureResult = new UploadPictureResult();
        int width = imageInfo.getWidth();
        int height = imageInfo.getHeight();
        double picSale = NumberUtil.round(width * 1.0 / height, 2).doubleValue();
        pictureResult.setPicName(FileUtil.mainName(originalFilename));
        pictureResult.setPicWidth(width);
        pictureResult.setPicHeight(height);
        pictureResult.setPicScale(picSale);
        pictureResult.setPicFormat(imageInfo.getFormat());
        pictureResult.setUrl(cosClientConfig.getHost() + uploadPath);
        return pictureResult;
    }


    /**
     * 上传文件
     *
     * @param inputResource
     * @param bucket
     * @param uploadPath
     * @return
     */
    protected abstract PutObjectResult uploadToCOS(Object inputResource, String bucket, String uploadPath, UploadPictureResult pictureResult);

    /**
     * 获取原始文件名
     *
     * @param inputResource
     * @return
     */
    protected abstract String getOriginalFilename(Object inputResource,String prefix);

    /**
     * 校验输入源
     *
     * @param inputResource
     */
    protected abstract String validPicture(Object inputResource);


    /**
     * 删除临时文件
     */
    public void deleteTempFile(File file) {
        if (file == null) {
            return;
        }
        // 删除临时文件
        boolean deleteResult = file.delete();
        if (!deleteResult) {
            log.error("file delete error, filepath = {}", file.getAbsolutePath());
        }

    }


}
