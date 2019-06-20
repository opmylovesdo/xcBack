package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Auther: LUOLUO
 * @Date: 2019/5/9
 * @Description: com.xuecheng.manage_course.dao
 * @version: 1.0
 */
@Mapper
public interface TeachplanMapper {

    TeachplanNode selectList(@Param("courseId") String courseId);
}
