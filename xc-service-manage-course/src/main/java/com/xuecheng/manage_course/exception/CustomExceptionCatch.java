package com.xuecheng.manage_course.exception;

import com.xuecheng.framework.exception.ExceptionCatch;
import com.xuecheng.framework.model.response.CommonCode;
import org.springframework.web.bind.annotation.ControllerAdvice;

import org.springframework.security.access.AccessDeniedException;

/**
 * @Auther: LUOLUO
 * @Date: 2019/6/18
 * @Description: com.xuecheng.manage_course.exception
 * @version: 1.0
 */
@ControllerAdvice
public class CustomExceptionCatch extends ExceptionCatch {

    static {
        //除了CustomException以外的异常类型及对应的错误代码在这里定义,，如果不定义则统一返回固定的错误信息
        builder.put(AccessDeniedException.class, CommonCode.UNAUTHORISE);
    }
}
