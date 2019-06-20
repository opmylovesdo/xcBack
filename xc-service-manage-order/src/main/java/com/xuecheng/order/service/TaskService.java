package com.xuecheng.order.service;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.domain.task.XcTaskHis;
import com.xuecheng.order.dao.XcTaskHisRepository;
import com.xuecheng.order.dao.XcTaskRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @Auther: LUOLUO
 * @Date: 2019/6/20
 * @Description: com.xuecheng.order.service
 * @version: 1.0
 */
@Service
public class TaskService {

    @Autowired
    private XcTaskRepository xcTaskRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private XcTaskHisRepository xcTaskHisRepository;

    /**
     * 取出前n条任务,取出指定时间之前的任务
     *
     * @param updateTime
     * @param n
     * @return
     */
    public List<XcTask> findTaskList(Date updateTime, int n) {
        //设置分页
        Pageable pageable = PageRequest.of(0, n);
        Page<XcTask> xcTasks = xcTaskRepository.findByUpdateTimeBefore(pageable, updateTime);
        return xcTasks.getContent();
    }


    /**
     * 发送消息
     *
     * @param xcTask
     * @param ex
     * @param routingKey
     */
    public void publish(XcTask xcTask, String ex, String routingKey) {
        //查询任务
        Optional<XcTask> taskOptional = xcTaskRepository.findById(xcTask.getId());
        taskOptional.ifPresent(task -> {
            rabbitTemplate.convertAndSend(ex, routingKey, xcTask);
            //更新任务时间为当前时间
            xcTask.setUpdateTime(new Date());
            xcTaskRepository.save(xcTask);
        });
    }

    /**
     * 使用乐观锁的方法 校验任务
     *
     * @param taskId
     * @param version
     * @return
     */
    @Transactional
    public int getTask(String taskId, int version) {
        int i = xcTaskRepository.updateTaskVersion(taskId, version);
        // 测试
        // XcTask xcTask = xcTaskRepository.findById(taskId).get();
        return i;
    }


    /**
     * 完成任务 将xc_task 移到 xc_task_his
     *
     * @param taskId
     */
    @Transactional
    public void finishTask(String taskId) {
        Optional<XcTask> taskOptional = xcTaskRepository.findById(taskId);
        if (taskOptional.isPresent()) {
            XcTask xcTask = taskOptional.get();
            xcTask.setDeleteTime(new Date());
            XcTaskHis xcTaskHis = new XcTaskHis();
            BeanUtils.copyProperties(xcTask, xcTaskHis);
            xcTaskHisRepository.save(xcTaskHis);
            xcTaskRepository.delete(xcTask);
        }
    }

}
