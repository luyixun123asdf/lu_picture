package com.lu.lupicturebackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;

import com.lu.lupicturebackend.exception.BusinessException;
import com.lu.lupicturebackend.exception.ErrorCode;
import com.lu.lupicturebackend.exception.ThrowUtils;
import com.lu.lupicturebackend.manager.CosManager;
import com.lu.lupicturebackend.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * url文件上传
 */
@Service
public class UrlPictureUpload extends PictureUploadTemplate {
    @Resource
    private CosManager cosManager;

//    @Resource
//    private CosClientConfig cosClientConfig;

    @Override
    protected PutObjectResult uploadToCOS(Object inputResource, String bucket, String uploadPath, UploadPictureResult pictureResult) {
        File tempFile = null;
        try {
            tempFile = File.createTempFile(uploadPath, null);
            String fileUrl = (String) inputResource;
            HttpUtil.downloadFile(fileUrl, tempFile);
            // 上传图片
            PutObjectResult pictureObject = cosManager.putPictureObject(uploadPath, tempFile);
            pictureResult.setPicSize(FileUtil.size(tempFile));
            return pictureObject;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        } finally {
            deleteTempFile(tempFile);
        }

    }

    @Override
    protected String getOriginalFilename(Object inputResource) {
        String fileUrl = (String) inputResource;
        return FileUtil.mainName(fileUrl);
    }

    @Override
    protected String validPicture(Object inputResource) {
        String fileUrl = (String) inputResource;
        // 校验非空
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "url不能为空");

        // 校验url格式
        try {
            URL url = new URL(fileUrl);
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
                return "";
            }
            // 存在 文件后缀校验
            String content_type = httpResponse.header("Content-Type");
            final Map<String, Boolean> allowSuffixList = new HashMap<>();
            allowSuffixList.put("image/png", true);
            allowSuffixList.put("image/jpg", true);
            allowSuffixList.put("image/jpeg", true);
            allowSuffixList.put("image/gif", true);
            allowSuffixList.put("image/webp", true);
            allowSuffixList.put("image/svg", true);


//            final List<String> allowSuffixList = Arrays.asList("image/png", "image/jpg", "image/jpeg", "image/gif", "image/webp", "image/svg");
            ThrowUtils.throwIf(!allowSuffixList.get(content_type), ErrorCode.PARAMS_ERROR, "文件类型错误");
            // 返回后缀
            return content_type.split("/")[1];

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (httpResponse != null) {
                httpResponse.close();
            }
        }

    }
}
