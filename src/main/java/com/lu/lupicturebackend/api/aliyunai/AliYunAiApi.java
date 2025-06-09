package com.lu.lupicturebackend.api.aliyunai;

import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.lu.lupicturebackend.api.aliyunai.model.CreateOutPaintingTaskRequest;
import com.lu.lupicturebackend.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.lu.lupicturebackend.api.aliyunai.model.GetOutPaintingTaskResponse;
import com.lu.lupicturebackend.exception.BusinessException;
import com.lu.lupicturebackend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AliYunAiApi {

    @Value("${aliyunAi.apikey}")
    private String apiKey;
    // 创建任务地址
    public static final String CREATE_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/image2image/out-painting";

    // 查询任务状态
    public static final String GET_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/tasks/%s";

    /**
     * 创建任务
     * @param createOutPaintingTaskRequest
     * @return
     */
    // 创建任务
    public CreateOutPaintingTaskResponse createOutPaintingTask(CreateOutPaintingTaskRequest createOutPaintingTaskRequest) {
        if (createOutPaintingTaskRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数错误");
        }
        // 发送请求
        HttpRequest httpRequest = HttpRequest.post(CREATE_OUT_PAINTING_TASK_URL)
                .header(Header.AUTHORIZATION, "Bearer " + apiKey)
                // 必须开启异步处理，设置为enable。
                .header("X-DashScope-Async", "enable")
                .header(Header.CONTENT_TYPE, ContentType.JSON.getValue())
                .body(JSONUtil.toJsonStr(createOutPaintingTaskRequest));

        // 处理响应
        try (HttpResponse response = httpRequest.execute()) {
            if (!response.isOk()) {
                log.info("请求异常：{}", response.body());
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "Ai扩图失败");
            }
            CreateOutPaintingTaskResponse paintingTaskResponse = JSONUtil.toBean(response.body(), CreateOutPaintingTaskResponse.class);
            if (paintingTaskResponse.getCode() != null){
                String message = paintingTaskResponse.getMessage();
                log.info("错误：{}",  message);
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "AI扩图失败："+message);
            }
            return paintingTaskResponse;
        }
    }

    /**
     * 创建查询结果
     * @param taskId
     * @return
     */
    // 创建查询结果
    public GetOutPaintingTaskResponse getOutPaintingTask(String taskId) {
        if (taskId == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "任务id不能为空");
        }
        try (HttpResponse httpResponse = HttpRequest.get(String.format(GET_OUT_PAINTING_TASK_URL, taskId))
                .header(Header.AUTHORIZATION, "Bearer " + apiKey)
                .execute()) {
            if (!httpResponse.isOk()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取任务失败");
            }
            return JSONUtil.toBean(httpResponse.body(), GetOutPaintingTaskResponse.class);
        }
    }
}
