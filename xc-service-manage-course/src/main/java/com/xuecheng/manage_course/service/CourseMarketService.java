package com.xuecheng.manage_course.service;
import java.util.Date;

import com.xuecheng.framework.domain.course.CourseMarket;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.dao.CourseMarketRepository;
import org.junit.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * @Auther: LUOLUO
 * @Date: 2019/5/11
 * @Description: com.xuecheng.manage_course.service
 * @version: 1.0
 */
@Service
@Transactional
public class CourseMarketService {
    @Autowired
    private CourseMarketRepository courseMarketRepository;


    public CourseMarket findCourseMarketById(String courseId) {
        Optional<CourseMarket> optional = courseMarketRepository.findById(courseId);
        if (!optional.isPresent()){
           return null;
        }else{
            return optional.get();
        }
    }


    public CourseMarket updateCourseMarket(String courseId, CourseMarket courseMarket) {
        CourseMarket course = this.findCourseMarketById(courseId);
        if(null != course){
            course.setCharge(courseMarket.getCharge());
            course.setStartTime(courseMarket.getStartTime());//课程有效期，开始时间
            course.setEndTime(courseMarket.getEndTime());//课程有效期，结束时间
            course.setPrice(courseMarket.getPrice());
            course.setQq(courseMarket.getQq());
            course.setValid(courseMarket.getValid());
            courseMarketRepository.save(course);
        }else{
            course = new CourseMarket();
            BeanUtils.copyProperties(courseMarket, course);
            course.setId(courseId);
            courseMarketRepository.save(course);
        }

        return course;
    }


}
