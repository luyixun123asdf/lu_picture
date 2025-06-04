package com.lu.lupicturebackend.manager;

import cn.hutool.core.io.FileUtil;
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
import java.util.ArrayList;
import java.util.List;

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
        List<PicOperations.Rule> rules = new ArrayList<>();
        // 返回图片信息
        PicOperations picOperations = new PicOperations();
        picOperations.setIsPicInfo(1);

        if (file.length() > 2 * 1024){
            // 2、缩略图规则
            PicOperations.Rule thumbnailRule = new PicOperations.Rule();
            thumbnailRule.setBucket(cosClientConfig.getBucket());
            thumbnailRule.setFileId(FileUtil.mainName(key)+"_thumbnail"+FileUtil.getSuffix(key));
            thumbnailRule.setRule(String.format("imageMogr2/thumbnail/%sx%s>", 128, 128));
            rules.add(thumbnailRule);
        }
        // 压缩图片为webp格式
        PicOperations.Rule rule = new PicOperations.Rule();
        // 获取名字
        String webKey = FileUtil.mainName(key)+".webp";
        // 1\设置压缩规则
        rule.setRule("imageMogr2/format/webp");
        rule.setBucket(cosClientConfig.getBucket());
        rule.setFileId(webKey);
        // 设置
        rules.add(rule);

        // 构造参数
        picOperations.setRules(rules);
        putObjectRequest.setPicOperations(picOperations);

        return cosClient.putObject(putObjectRequest);

    }

    // 上传文件，不要保存为临时文件
    public PutObjectResult uploadToCOS(MultipartFile multipartFile, String bucketName, String key) throws Exception {
//        // 创建 COS 客户端
//        COSClient cosClient = createCOSClient();
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.NOT_FOUND_ERROR, "上传文件为空");

        try (InputStream inputStream = multipartFile.getInputStream()) {
            // 元信息配置
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(multipartFile.getSize());
            metadata.setContentType(multipartFile.getContentType());

            // 创建上传请求
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, inputStream, metadata);
            PicOperations picOperations = new PicOperations();
            picOperations.setIsPicInfo(1);
            List<PicOperations.Rule> rules = new ArrayList<>();
            // 1、压缩图片为webp格式
            PicOperations.Rule rule = new PicOperations.Rule();
            // 获取名字
            String webKey = FileUtil.mainName(key)+".webp";

            // 设置规则
            rule.setRule("imageMogr2/format/webp");
            rule.setBucket(cosClientConfig.getBucket());
            rule.setFileId(webKey);
            rules.add(rule);
            // 2、缩略图规则
            PicOperations.Rule thumbnailRule = new PicOperations.Rule();
            thumbnailRule.setBucket(cosClientConfig.getBucket());
            thumbnailRule.setFileId(FileUtil.mainName(key)+"_thumbnail"+FileUtil.getSuffix(key));
            thumbnailRule.setRule(String.format("imageMogr2/thumbnail/%sx%s>", 128, 128));
            rules.add(thumbnailRule);

            picOperations.setRules(rules);

            putObjectRequest.setPicOperations(picOperations);
            // 上传文件
            return cosClient.putObject(putObjectRequest);

//            // 生成访问链接
//            return "https://" + bucketName + ".cos." + cosClient.getClientConfig().getRegion().getRegionName()
//                    + ".myqcloud.com/" + key;
        } finally {
            cosClient.shutdown();
        }
    }


}
