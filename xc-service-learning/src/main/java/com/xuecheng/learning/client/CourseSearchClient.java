package com.xuecheng.learning.client;

import com.xuecheng.framework.client.XcServiceList;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @Auther: LUOLUO
 * @Date: 2019/6/8
 * @Description: com.xuecheng.learning.client
 * @version: 1.0
 */
@FeignClient(value= XcServiceList.XC_SERVICE_SEARCH)
public interface CourseSearchClient {

    @GetMapping("/search/course/getmedia/{teachplanId}")
    TeachplanMediaPub getMedia(@PathVariable("teachplanId") String teachplanId);
}
