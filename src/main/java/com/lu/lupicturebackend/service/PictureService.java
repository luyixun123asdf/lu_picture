package com.lu.lupicturebackend.service;

import com.lu.lupicturebackend.model.dto.picture.PictureUploadRequest;
import com.lu.lupicturebackend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lu.lupicturebackend.model.entity.User;
import com.lu.lupicturebackend.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

/**
* @author 86186
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-05-30 10:36:16
*/
public interface PictureService extends IService<Picture> {


    /**
     * 上传图片
     *
     * @param multipartFile
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(MultipartFile multipartFile,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);

}
