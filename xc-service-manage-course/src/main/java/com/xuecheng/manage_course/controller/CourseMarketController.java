package com.xuecheng.manage_course.controller;

import com.xuecheng.api.course.CourseMarketControllerApi;
import com.xuecheng.framework.domain.course.CourseMarket;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.service.CourseMarketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Auther: LUOLUO
 * @Date: 2019/5/9
 * @Description: com.xuecheng.manage_course.controller
 * @version: 1.0
 */
@RestController
@RequestMapping("/course")
public class CourseMarketController implements CourseMarketControllerApi {

    @Autowired
    private CourseMarketService courseMarketService;


    @Override
    @GetMapping("/market/get/{courseId}")
    public CourseMarket findCourseMarketById(@PathVariable("courseId") String courseId) {

        return courseMarketService.findCourseMarketById(courseId);
    }

    @Override
    @PutMapping("/market/update/{courseId}")
    public ResponseResult updateCourseMarket(@PathVariable("courseId") String courseId, @RequestBody CourseMarket courseMarket) {
        CourseMarket courseMarket1 = courseMarketService.updateCourseMarket(courseId, courseMarket);
        if(null == courseMarket1) return new ResponseResult(CommonCode.FAIL);
        return new ResponseResult(CommonCode.SUCCESS);
    }
}
