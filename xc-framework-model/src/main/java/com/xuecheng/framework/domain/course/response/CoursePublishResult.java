package com.xuecheng.framework.domain.course.response;

/**
 * @Auther: LUOLUO
 * @Date: 2019/5/18
 * @Description: com.xuecheng.framework.domain.course.response
 * @version: 1.0
 */

import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
public class CoursePublishResult extends ResponseResult {
    String previewUrl;

    public CoursePublishResult(ResultCode resultCode, String previewUrl) {
        super(resultCode);
        this.previewUrl = previewUrl;
    }
}
