package com.lu.lupicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * 批量导入图片请求
 */
@Data
public class PictureUploadByBatchRequest implements Serializable {
    /**
     * 抓取的内容
     */
    private String searchText;

    /**
     * 抓取的图片数量
     */

    private Integer count = 10;

    private static final long serialVersionUID = 1L;
}
