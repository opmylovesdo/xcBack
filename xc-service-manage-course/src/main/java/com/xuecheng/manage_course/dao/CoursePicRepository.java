package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.CoursePic;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @Auther: LUOLUO
 * @Date: 2019/5/14
 * @Description: com.xuecheng.manage_course.dao
 * @version: 1.0
 */
public interface CoursePicRepository extends JpaRepository<CoursePic, String> {
    long deleteByCourseid(String courseId);
}
