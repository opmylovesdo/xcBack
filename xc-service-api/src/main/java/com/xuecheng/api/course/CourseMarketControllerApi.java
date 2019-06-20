package com.xuecheng.api.course;

import com.xuecheng.framework.domain.course.CourseMarket;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @Auther: LUOLUO
 * @Date: 2019/5/9
 * @Description: com.xuecheng.api.config
 * @version: 1.0
 */
@Api(value = "课程营销接口", description = "课程营销接口")
public interface CourseMarketControllerApi {


    @ApiOperation("根据id查询课程营销")
    CourseMarket findCourseMarketById(String courseId);

    @ApiOperation("更新课程营销")
    ResponseResult updateCourseMarket(String courseId, CourseMarket courseMarket);
}
