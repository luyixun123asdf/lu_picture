package com.lu.lupicturebackend.api.imagesearch.sub;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.lu.lupicturebackend.exception.BusinessException;
import com.lu.lupicturebackend.exception.ErrorCode;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 获取图片页面的url (step1)
 */
public class GetImagePageUrlApi {

    /**
     * 获取以图搜图方法
     *
     * @param imageUrl
     * @return
     */
    public static String getImagePageUrl(String imageUrl) {
          final  String acsToken ="1749270361097_1749306076416_ztnYKJjw6WapoBDuhnpSVug9fRh/0VBRoAQxPHXEq+ivP2M7Xkqrdtq/hNWqrPuDDM1DwTA5fQ0dmvg+vohWgILUGBcyn7uQCATmk9Ma6DqFSNO//MBh26h" +
                  "cgMY/nIQeNVnEW1SYpN/Cgq+dGgN8wlXFnFwxKH/quEpvkWfyUSCFpBxiQI3PufWWDFf9hf+YdjZ2Y6HOHXEcIDrYhifdjjqQl+GTJ7Z6WaQNSzmVP2Q74pkkMSbCqr2DrrQUKAiICKoxqRX36M" +
                  "cO5shN7h3NoVyop7f5eDfXlBStiVh4IDM7UylSU8IZNucpdbisAmaby/Y9hcU+4gw75dYRrSwLGZe1XwNGJZmAdzm1zyLxH0eVWmpGhe4Va4" +
                  "CTUbHYABaChpMnlaSlKznuhuDRSHVsJ+PGV+vIlTtzB4IQo5T9bL8TTpTu8NUQvyn9oQeE/J8fK2BY/tcZBJI576t7uCju9QjH+T/D1QB+U14QFG/qWK8=";
        //准备参请求数
        HashMap<String, Object> formData = new HashMap<>();
        formData.put("image", imageUrl);
        formData.put("tn", "pc");
        formData.put("from", "pc");
        formData.put("image_source", "PC_UPLOAD_URL");
        // 获取当前时间戳
        long timestamp = System.currentTimeMillis();
        // 请求地址
        String url = "https://graph.baidu.com/upload?uptime=" + timestamp;
        // 2。发送请求
        try {
            HttpResponse httpResponse = HttpRequest.post(url)
                    .header("Acs-Token",acsToken)
                    .form(formData)
                    .timeout(5000)
                    .execute();
            if (!httpResponse.isOk()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败");
            }
            // 解析响应
            String body = httpResponse.body();
            Map<String, Object> result = JSONUtil.toBean(body, Map.class);

            if (result == null || !Integer.valueOf(0).equals(result.get("status"))) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败");
            }
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            String rawUrl = (String) data.get("url");
            // 解码url
            URLUtil.decode(rawUrl, StandardCharsets.UTF_8);
            //
            if (StrUtil.isBlank(rawUrl)) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "未返回有效的结果地址");
            }
            return rawUrl;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "搜索失败");
        }
    }
    public static void main(String[] args) {
        String imageUrl = "https://www.codefather.cn/logo.png";
        String imagePageUrl = getImagePageUrl(imageUrl);
        System.out.println("成功调用结果为"+imagePageUrl);
    }
}


