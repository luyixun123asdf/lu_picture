package com.lu.lupicturebackend.model.enums;

/**
 *  图片审核状态枚举
 */
public enum PictureReviewStatusEnum {

    REVIEWING("审核中", 0),

    REVIEW_PASS("审核通过", 1),

    REVIEW_REJECT("审核拒绝", 2);
    private String text;
    private int value;

    PictureReviewStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }
    /**
     *  根据value获取枚举
     *   @param value
     *   @return  枚举
     */
    public static PictureReviewStatusEnum getEnumByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (PictureReviewStatusEnum anEnum : PictureReviewStatusEnum.values()) {
            if (anEnum.value == value) {
                return anEnum;
            }
        }
        return null;
    }
}
