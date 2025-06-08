package com.lu.lupicturebackend.api.imagesearch;

import com.lu.lupicturebackend.api.imagesearch.model.ImageSearchResult;
import com.lu.lupicturebackend.api.imagesearch.sub.GetImageFirstUrlApi;
import com.lu.lupicturebackend.api.imagesearch.sub.GetImageListApi;
import com.lu.lupicturebackend.api.imagesearch.sub.GetImagePageUrlApi;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ImageSearchApiFacade {
    /**
     * 搜索图片
     *
     */
    public static List<ImageSearchResult> searchImage(String imageUrl) {
        // 第一步
        String imagePageUrl = GetImagePageUrlApi.getImagePageUrl(imageUrl);
        // 第二步
        String imageFirstUrl = GetImageFirstUrlApi.getImageFirstUrl(imagePageUrl);
        // 第三步
        List<ImageSearchResult> imageList = GetImageListApi.getImageList(imageFirstUrl);
        return imageList;
    }

    public static void main(String[] args) {
        List<ImageSearchResult> imageSearchResults = searchImage("https://www.codefather.cn/logo.png");
    }
}
