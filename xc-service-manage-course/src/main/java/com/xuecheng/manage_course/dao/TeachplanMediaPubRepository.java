package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @Auther: LUOLUO
 * @Date: 2019/6/7
 * @Description: com.xuecheng.manage_course.dao
 * @version: 1.0
 */
public interface TeachplanMediaPubRepository extends JpaRepository<TeachplanMediaPub, String> {

    /**
     * 根据课程id删除课程计划媒资信息
     * @param courseId
     * @return
     */
    long deleteByCourseId(String courseId);
}
