package com.lu.lupicturebackend.controller;

import com.lu.lupicturebackend.annotation.AuthCheck;
import com.lu.lupicturebackend.common.BaseResponse;
import com.lu.lupicturebackend.common.ResultUtils;
import com.lu.lupicturebackend.constant.UserConstant;
import com.lu.lupicturebackend.exception.BusinessException;
import com.lu.lupicturebackend.exception.ErrorCode;
import com.lu.lupicturebackend.manager.CosManager;

import com.lu.lupicturebackend.model.dto.picture.PictureUploadRequest;
import com.lu.lupicturebackend.model.entity.User;
import com.lu.lupicturebackend.model.vo.PictureVO;
import com.lu.lupicturebackend.service.PictureService;
import com.lu.lupicturebackend.service.UserService;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private CosManager cosManager;

    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    /**
     * 文件上传
     * @param file
     * @return
     * @throws IOException
     */
    @PostMapping("/test/upload")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<String> upload(@RequestPart("file") MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        String filePath = String.format("test/%s", fileName);
        File tempFile = null;
        try {
            tempFile = File.createTempFile(filePath, fileName);
            file.transferTo(tempFile);
            cosManager.putObject(fileName, tempFile);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        } finally {
            if (tempFile != null) {
                boolean delete = tempFile.delete();// 删除临时文件
                if (!delete) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件删除失败");
                }
            }
        }

        return ResultUtils.success(filePath);


    }
    /**
     * 文件下载
     */
    @PostMapping("/test/download")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public void download(String fileName, HttpServletResponse response) throws IOException {
        COSObject objectUrl = cosManager.getObjectUrl(fileName);
        COSObjectInputStream inputStream = objectUrl.getObjectContent();
        // 设置响应
        response.setContentType("application/octet-stream;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        try {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) > 0) {
                response.getOutputStream().write(buffer, 0, len);
            }
            response.getOutputStream().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            inputStream.close();
        }
    }
    /**
     * 上传图片（可重新上传）
     */
    @PostMapping("/upload")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PictureVO> uploadPicture(
            @RequestPart("file") MultipartFile multipartFile,
            PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }
}
