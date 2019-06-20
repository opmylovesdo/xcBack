package com.xuecheng.ucenter.dao.test;

import com.xuecheng.framework.domain.ucenter.XcMenu;
import com.xuecheng.ucenter.dao.XcMenuMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * @Auther: LUOLUO
 * @Date: 2019/6/18
 * @Description: com.xuecheng.ucenter.dao
 * @version: 1.0
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class DaoTest {

    @Autowired
    private XcMenuMapper mapper;

    @Test
    public void testSelectPermissionByUserId(){
        List<XcMenu> xcMenus = mapper.selectPermissionByUserId("49");
        xcMenus.forEach(System.out::println);
    }
}
