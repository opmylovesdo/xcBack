package com.xuecheng.api.search;

import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.QueryResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.io.IOException;
import java.util.Map;

/**
 * @Auther: LUOLUO
 * @Date: 2019/5/20
 * @Description: com.xuecheng.api.search
 * @version: 1.0
 */
@Api(value = "课程搜索",description = "课程搜索",tags = {"课程搜索"})
public interface ESCourseControllerApi {

    @ApiOperation("课程搜索")
    QueryResponseResult<CoursePub> list(int page, int size, CourseSearchParam courseSearchParam)  throws IOException;

    @ApiOperation("根据id查询课程信息")
    Map<String, CoursePub> getAll(String courseId);

    @ApiOperation("根据课程计划id查询媒资信息")
    TeachplanMediaPub getMedia(String teachplanId);
}
