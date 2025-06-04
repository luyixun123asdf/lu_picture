package com.lu.lupicturebackend.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.NumberUtil;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.lu.lupicturebackend.config.CosClientConfig;
import com.lu.lupicturebackend.exception.BusinessException;
import com.lu.lupicturebackend.exception.ErrorCode;
import com.lu.lupicturebackend.exception.ThrowUtils;
import com.lu.lupicturebackend.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 文件服务
 *
 * @deprecated 废弃使用upload包的模板方法优化
 */
//@Service
@Slf4j
@Deprecated
public class FileManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    /**
     * 上传图片
     *
     * @param multipartFile    文件
     * @param uploadPathPrefix 上传路径前缀
     */
    public UploadPictureResult uploadPictureFile(MultipartFile multipartFile, String uploadPathPrefix) {
        // 校验文件
        validPicture(multipartFile);
        //　上传图片
//        File tempFile = null;
        String uuid = UUID.randomUUID().toString();
        String originalFilename = multipartFile.getOriginalFilename();
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFileName);
        try {
//            tempFile = File.createTempFile(uploadPath, null);
//            multipartFile.transferTo(tempFile); // 将文件写入临时文件
            // 上传图片
//            PutObjectResult pictureObject = cosManager.putPictureObject(uploadPath, tempFile);
            PutObjectResult pictureObject = cosManager.uploadToCOS(multipartFile, cosClientConfig.getBucket(), uploadPath); // 直接上传到对象存储，不要保存为临时文件
            // 获取图片信息
            ImageInfo imageInfo = pictureObject.getCiUploadResult().getOriginalInfo().getImageInfo();
            // 封装返回结果
            UploadPictureResult pictureResult = new UploadPictureResult();
            int width = imageInfo.getWidth();
            int height = imageInfo.getHeight();
            double picSale = NumberUtil.round(width * 1.0 / height, 2).doubleValue();
            pictureResult.setPicName(FileUtil.mainName(originalFilename));
            pictureResult.setPicWidth(width);
            pictureResult.setPicHeight(height);
            pictureResult.setPicScale(picSale);
            pictureResult.setPicFormat(imageInfo.getFormat());
            pictureResult.setPicSize(multipartFile.getSize());
            pictureResult.setUrl(cosClientConfig.getHost() + uploadPath);
            return pictureResult;
        } catch (Exception e) {
            log.error("图片上传到对象存储失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        } finally {
//            deleteTempFile(tempFile);
        }

    }

    /**
     * 图片校验
     *
     * @param file
     */

    private void validPicture(MultipartFile file) {
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

    }

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

    /**
     * 通过url下载图片的方法
     *
     * @param fileUrl
     * @param uploadPathPrefix
     * @return
     */
    public UploadPictureResult uploadByPicture(String fileUrl, String uploadPathPrefix) {
        // 校验文件
        validPicture(fileUrl);
        //　上传图片
        File tempFile = null;
        String uuid = UUID.randomUUID().toString();
//        String originalFilename = multipartFile.getOriginalFilename();
        String originalFilename = FileUtil.mainName(fileUrl);
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFileName);
        try {
            tempFile = File.createTempFile(uploadPath, null);

            HttpUtil.downloadFile(fileUrl, tempFile);
            // 上传图片
            PutObjectResult pictureObject = cosManager.putPictureObject(uploadPath, tempFile);
            // 获取图片信息
            ImageInfo imageInfo = pictureObject.getCiUploadResult().getOriginalInfo().getImageInfo();
            // 封装返回结果
            UploadPictureResult pictureResult = new UploadPictureResult();
            int width = imageInfo.getWidth();
            int height = imageInfo.getHeight();
            double picSale = NumberUtil.round(width * 1.0 / height, 2).doubleValue();
            pictureResult.setPicName(FileUtil.mainName(originalFilename));
            pictureResult.setPicWidth(width);
            pictureResult.setPicHeight(height);
            pictureResult.setPicScale(picSale);
            pictureResult.setPicFormat(imageInfo.getFormat());
            pictureResult.setPicSize(FileUtil.size(tempFile));
            pictureResult.setUrl(cosClientConfig.getHost() + uploadPath);
            return pictureResult;
        } catch (Exception e) {
            log.error("图片上传到对象存储失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        } finally {
            deleteTempFile(tempFile);
        }
    }

    /**
     * 通过url校验文件
     *
     * @param fileUrl
     */
    private void validPicture(String fileUrl) {
        // 校验非空
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "url不能为空");

        // 校验url格式
        try {
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "url格式错误");
        }
        //  校验url协议
        ThrowUtils.throwIf(!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://"), ErrorCode.PARAMS_ERROR, "仅支持http或者https协议的地址");
        // 发送HEAD 请求
        HttpResponse httpResponse = null;
        try {
            httpResponse = HttpUtil.createRequest(Method.HEAD, fileUrl).execute();
            if (!httpResponse.isOk()) {
                return;
            }
            // 存在 文件后缀校验
            String content_type = httpResponse.header("Content-Type");
            final List<String> allowSuffixList = Arrays.asList("image/png", "image/jpg", "image/jpeg", "image/gif", "image/webp", "image/svg");
            ThrowUtils.throwIf(!allowSuffixList.contains(content_type), ErrorCode.PARAMS_ERROR, "文件类型错误");
            // 存在 文件大小校验
            String content_length = httpResponse.header("Content-Length");
            if (content_length == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小异常");
            }
            try {
                long len = Long.parseLong(content_length);
                final long fileSize = 1024 * 1024;
                ThrowUtils.throwIf(len > 2 * fileSize, ErrorCode.PARAMS_ERROR, "文件大小不能超过 2M");
            } catch (NumberFormatException e) {
                throw new RuntimeException(e);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (httpResponse != null) {
                httpResponse.close();
            }
        }

    }

}
