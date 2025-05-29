package com.lu.lupicturebackend.common;

import com.lu.lupicturebackend.exception.ErrorCode;

/**
 * 封装返回类
 */
public class ResultUtils {
    /**
     * 封装成功返回
     *
     * @param data
     * @param <T>
     * @return
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data, "ok");
    }

    /**
     * 封装失败返回
     *
     * @param errorCode
     * @return
     */
    public static BaseResponse<Void> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    /**
     * 封装失败返回自定义错误信息
     *
     * @param errorCode
     * @param message
     * @return
     */
    public static BaseResponse<Void> error(ErrorCode errorCode, String message) {
        return new BaseResponse<>(errorCode.getCode(), null, message);
    }

    /**
     * @param code
     * @param message
     * @return
     */
    public static BaseResponse<Void> error(int code, String message) {
        return new BaseResponse<>(code, null, message);
    }
}
