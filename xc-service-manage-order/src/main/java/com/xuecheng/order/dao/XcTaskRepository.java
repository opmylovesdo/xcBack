package com.xuecheng.order.dao;

import com.xuecheng.framework.domain.task.XcTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;

/**
 * @Auther: LUOLUO
 * @Date: 2019/6/20
 * @Description: com.xuecheng.order.dao
 * @version: 1.0
 */
public interface XcTaskRepository extends JpaRepository<XcTask, String> {

    /**
     * 取出指定时间之前的记录
     * @param pageable 分页参数
     * @param updateTime 指定时间
     * @return
     */
    Page<XcTask> findByUpdateTimeBefore(Pageable pageable, Date updateTime);

    /**
     * 更新任务处理时间
     * @param id
     * @param updateTime
     * @return
     */
    @Modifying
//    @Query("update XcTask t set t.updateTime = :updateTime where t.id = :id")
    @Query(nativeQuery = true, value = "update xc_task t set t.update_time = ?2 where t.id = ?1")
    int updateTaskTime(String id, Date updateTime);


    @Modifying
    @Query(nativeQuery = true, value = "update xc_task t set t.version=?2+1 where t.id=?1 and t.version=?2")
    int updateTaskVersion(String id, int version);
}
