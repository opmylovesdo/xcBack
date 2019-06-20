package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.TeachplanMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @Auther: LUOLUO
 * @Date: 2019/6/6
 * @Description: com.xuecheng.manage_media.dao
 * @version: 1.0
 */
public interface TeachplanMediaRepository extends JpaRepository<TeachplanMedia, String> {

    /**
     * 根据课程id查询课程媒资信息
     * @param courseId
     * @return
     */
    List<TeachplanMedia> findByCourseId(String courseId);
}
