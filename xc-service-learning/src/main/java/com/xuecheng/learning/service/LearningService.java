package com.xuecheng.learning.service;

import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.learning.XcLearningCourse;
import com.xuecheng.framework.domain.learning.response.GetMediaResult;
import com.xuecheng.framework.domain.learning.response.LearningCode;
import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.domain.task.XcTaskHis;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.learning.client.CourseSearchClient;
import com.xuecheng.learning.dao.XcLearningCourseRepository;
import com.xuecheng.learning.dao.XcTaskHisRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

/**
 * @Auther: LUOLUO
 * @Date: 2019/6/8
 * @Description: com.xuecheng.learning.service
 * @version: 1.0
 */
@Service
public class LearningService {

    @Autowired
    private CourseSearchClient courseSearchClient;

    @Autowired
    private XcTaskHisRepository xcTaskHisRepository;

    @Autowired
    private XcLearningCourseRepository xcLearningCourseRepository;

    public GetMediaResult getMedia(String courseId, String teachplanId) {
        //根据课程id检查课程情况(是否收费) 学生的学习权限(是否购买该课程)
        //掉用搜索服务查询
        TeachplanMediaPub media = courseSearchClient.getMedia(teachplanId);

        if (media == null || StringUtils.isEmpty(media.getMediaUrl())) {
            //获取视频播放地址出错
            ExceptionCast.cast(LearningCode.LEARNING_GETMEDIA_ERROR);
        }

        return new GetMediaResult(CommonCode.SUCCESS, media.getMediaUrl());
    }


    /**
     * 选课
     *
     * @param userId
     * @param courseId
     * @param valid
     * @param startTime
     * @param endTime
     * @param xcTask
     * @return
     */
    @Transactional
    public ResponseResult addCourse(String userId, String courseId, String valid,
                                    Date startTime, Date endTime, XcTask xcTask) {

        if (StringUtils.isEmpty(courseId)) {
            ExceptionCast.cast(LearningCode.LEARNING_GETMEDIA_ERROR);
        }
        if (StringUtils.isEmpty(userId)) {
            ExceptionCast.cast(LearningCode.CHOOSECOURSE_USERISNULL);
        }
        if (xcTask == null || StringUtils.isEmpty(xcTask.getId())) {
            ExceptionCast.cast(LearningCode.CHOOSECOURSE_TASKISNULL);
        }

        //查询历史任务
        Optional<XcTaskHis> taskHisOptional = xcTaskHisRepository.findById(xcTask.getId());
//        taskHisOptional.ifPresent(taskHls ->  new ResponseResult(CommonCode.SUCCESS));
        //查询到表示该课程已经添加
        if (taskHisOptional.isPresent()) {
            return new ResponseResult(CommonCode.SUCCESS);
        }

        XcLearningCourse xcLearningCourse = xcLearningCourseRepository.findXcLearningCourseByUserIdAndCourseId(userId, courseId);
        //没有选课记录则添加
        if (xcLearningCourse == null) {
            xcLearningCourse = new XcLearningCourse();
            xcLearningCourse.setUserId(userId);
            xcLearningCourse.setCourseId(courseId);

        }
        //不管有没有选课记录 都要更新如下属性
        xcLearningCourse.setValid(valid);
        xcLearningCourse.setStartTime(startTime);
        xcLearningCourse.setEndTime(endTime);
        xcLearningCourse.setStatus("501001");
        //保存到数据库
        xcLearningCourseRepository.save(xcLearningCourse);

        //向历史任务表中插入记录
        XcTaskHis xcTaskHis = new XcTaskHis();
        BeanUtils.copyProperties(xcTask, xcTaskHis);
        xcTaskHisRepository.save(xcTaskHis);

        return new ResponseResult(CommonCode.SUCCESS);

    }


}
