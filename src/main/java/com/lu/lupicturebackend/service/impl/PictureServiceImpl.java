package com.lu.lupicturebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lu.lupicturebackend.model.entity.Picture;
import com.lu.lupicturebackend.service.PictureService;
import com.lu.lupicturebackend.mapper.PictureMapper;
import org.springframework.stereotype.Service;

/**
* @author 86186
* @description 针对表【picture(图片)】的数据库操作Service实现
* @createDate 2025-05-30 10:36:16
*/
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService{

}




