package com.xuecheng.order.mq;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.order.config.RabbitMQConfig;
import com.xuecheng.order.service.TaskService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @Auther: LUOLUO
 * @Date: 2019/6/19
 * @Description: com.xuecheng.order.mq
 * @version: 1.0
 */
@Component
public class ChooseCourseTask {

    public static final Logger LOGGER = LoggerFactory.getLogger(ChooseCourseTask.class);

    @Autowired
    private TaskService taskService;

    @RabbitListener(queues = RabbitMQConfig.XC_LEARNING_FINISHADDCHOOSECOURSE)
    public void receiveFinishChooseCourseTask(XcTask xcTask) {
        LOGGER.info("receiveChooseCourseTask...{}", xcTask.getId());
       if(xcTask != null && StringUtils.isNotEmpty(xcTask.getId())){
           taskService.finishTask(xcTask.getId());
       }
    }


    @Scheduled(cron = "0/3 * * * * *")
    public void task1() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, -1);
        Date date = calendar.getTime();
        System.out.println(date);
        List<XcTask> taskList = taskService.findTaskList(date, 100);
        System.out.println(taskList);

        //遍历任务列表
        taskList.forEach(task -> {
            //调用乐观锁方法校验任务是否可以执行
            if (taskService.getTask(task.getId(), task.getVersion()) > 0) {
                //发送选课消息
                taskService.publish(task, task.getMqExchange(), task.getMqRoutingkey());
                LOGGER.info("send choose course task id{}", task.getId());
            }
        });
    }

}
