package com.xuecheng.learning.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.learning.config.RabbitMQConfig;
import com.xuecheng.learning.service.LearningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @Auther: LUOLUO
 * @Date: 2019/6/20
 * @Description: com.xuecheng.learning.mq
 * @version: 1.0
 */
@Component
public class ChooseCourseTask {

    public static final Logger LOGGER = LoggerFactory.getLogger(ChooseCourseTask.class);

    @Autowired
    private LearningService learningService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConfig.XC_LEARNING_ADDCHOOSECOURSE)
    public void receiveChooseCourseTask(XcTask xcTask) {
        LOGGER.info("receive choose course task,taskId:{}", xcTask.getId());
        //添加选课
        try {
            String requestBody = xcTask.getRequestBody();
            Map<String, String> map = JSON.parseObject(requestBody, Map.class);
            String userId =  map.get("userId");
            String courseId =  map.get("courseId");
            String valid =  map.get("valid");
            Date startTime = null;
            Date endTime = null;
            SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY‐MM‐dd HH:mm:ss");
            if (map.get("startTime") != null) {
                startTime = dateFormat.parse( map.get("startTime"));
            }
            if (map.get("endTime") != null) {
                endTime = dateFormat.parse( map.get("endTime"));
            }
            //添加选课
            ResponseResult responseResult = learningService.addCourse(userId, courseId, valid, startTime, endTime, xcTask);
            //选课成功发送响应消息
            if(responseResult.isSuccess()){
                rabbitTemplate.convertAndSend(RabbitMQConfig.EX_LEARNING_ADDCHOOSECOURSE,
                        RabbitMQConfig.XC_LEARNING_FINISHADDCHOOSECOURSE_KEY, xcTask);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            LOGGER.error("send finish choose course taskId:{}", xcTask.getId());
        }
    }

}
