
package com.lu.lupicturebackend.controller;


import com.lu.lupicturebackend.common.BaseResponse;
import com.lu.lupicturebackend.common.ResultUtils;
import com.lu.lupicturebackend.exception.BusinessException;
import com.lu.lupicturebackend.exception.ErrorCode;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class BasicController {

    // http://127.0.0.1:8080/hello?name=lisi
    @RequestMapping("/hello")
    @ResponseBody
    public BaseResponse<String> hello(@RequestParam(name = "name", defaultValue = "unknown user") String name) {
        try {
            return ResultUtils.success("hello " + name);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SUCCESS);
        }
    }

}
