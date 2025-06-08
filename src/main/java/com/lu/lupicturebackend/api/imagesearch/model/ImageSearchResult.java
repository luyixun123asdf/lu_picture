package com.lu.lupicturebackend.api.imagesearch.model;


import lombok.Data;

/**
 * 以图片搜图片请求
 *
 * @author lu
 */
@Data
public class ImageSearchResult {
    /**
     * 缩略图url
     */
    private String thumbUrl;

    /**
     * 搜索来源
     */
    private String fromUrl;
}
