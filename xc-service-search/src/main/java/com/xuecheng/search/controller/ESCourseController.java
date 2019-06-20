package com.xuecheng.search.controller;

import com.xuecheng.api.search.ESCourseControllerApi;
import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.search.service.ESCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

/**
 * @Auther: LUOLUO
 * @Date: 2019/5/20
 * @Description: com.xuecheng.search.controller
 * @version: 1.0
 */
@RestController
@RequestMapping("/search/course")
public class ESCourseController implements ESCourseControllerApi {

    @Autowired
    private ESCourseService esCourseService;

    @Override
    @GetMapping("/list/{page}/{size}")
    public QueryResponseResult<CoursePub> list(@PathVariable("page") int page,
                                               @PathVariable("size") int size,
                                               CourseSearchParam courseSearchParam)  throws IOException {
        return esCourseService.list(page, size, courseSearchParam);
    }

    @Override
    @GetMapping("/getall/{id}")
    public Map<String, CoursePub> getAll(@PathVariable("id") String courseId) {
        return esCourseService.getAll(courseId);
    }

    @Override
    @GetMapping("/getmedia/{teachplanId}")
    public TeachplanMediaPub getMedia(@PathVariable("teachplanId") String teachplanId) {
        //将课程计划id放在数组中，为调用service作准备
        QueryResponseResult<TeachplanMediaPub> mediaPubQueryResponseResult = esCourseService.getMedia(new String[]{teachplanId});
        QueryResult<TeachplanMediaPub> queryResult = mediaPubQueryResponseResult.getQueryResult();
        if(queryResult != null && queryResult.getList() != null && queryResult.getList().size() > 0){
            //返回课程计划对应课程资源
            return queryResult.getList().get(0);
        }
        return new TeachplanMediaPub();
    }
}
