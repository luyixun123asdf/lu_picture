package com.lu.lupicturebackend.manager;

import com.lu.lupicturebackend.config.CosClientConfig;
import com.lu.lupicturebackend.exception.ErrorCode;
import com.lu.lupicturebackend.exception.ThrowUtils;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.*;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.InputStream;

@Component
public class CosManager {
    @Resource
    private CosClientConfig cosClientConfig;
    @Resource
    private COSClient cosClient;

    // 将本地文件上传到 COS
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        return cosClient.putObject(putObjectRequest);

    }

    /**
     * 获取对象流
     *
     * @param key
     * @return
     */

    public COSObject getObjectUrl(String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        return cosClient.getObject(getObjectRequest);
    }

    /**
     * 上传图片,并获取图片信息
     *
     * @param key
     * @param file
     * @return
     */
    public PutObjectResult putPictureObject(String key, File file) {
        if ( file == null){
            ThrowUtils.throwIf(true, ErrorCode.NOT_FOUND_ERROR, "上传文件为空");
        }
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        PicOperations picOperations = new PicOperations();
        picOperations.setIsPicInfo(1);
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);

    }

    // 上传文件，不要保存为临时文件
    public PutObjectResult uploadToCOS(MultipartFile multipartFile, String bucketName, String key) throws Exception {
//        // 创建 COS 客户端
//        COSClient cosClient = createCOSClient();

        try (InputStream inputStream = multipartFile.getInputStream()) {
            // 元信息配置
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(multipartFile.getSize());
            metadata.setContentType(multipartFile.getContentType());

            // 创建上传请求
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, inputStream, metadata);
            PicOperations picOperations = new PicOperations();
            picOperations.setIsPicInfo(1);
            putObjectRequest.setPicOperations(picOperations);

            // 上传文件
//            cosClient.putObject(putObjectRequest);

            return cosClient.putObject(putObjectRequest);

//            // 生成访问链接
//            return "https://" + bucketName + ".cos." + cosClient.getClientConfig().getRegion().getRegionName()
//                    + ".myqcloud.com/" + key;
        } finally {
            cosClient.shutdown();
        }
    }


}
