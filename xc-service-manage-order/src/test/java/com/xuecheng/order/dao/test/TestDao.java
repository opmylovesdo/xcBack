package com.xuecheng.order.dao.test;
import com.xuecheng.order.dao.XcTaskRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @Auther: LUOLUO
 * @Date: 2019/6/20
 * @Description: PACKAGE_NAME
 * @version: 1.0
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestDao {
    
    @Autowired
    private XcTaskRepository xcTaskRepository;
    
    @Test
    @Transactional
    public void testUpdateTime(){
        int i = xcTaskRepository.updateTaskTime("10", new Date());
        System.out.println(i);
    }
    
}
