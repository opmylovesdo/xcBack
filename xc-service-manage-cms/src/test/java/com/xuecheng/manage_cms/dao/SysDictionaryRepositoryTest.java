package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.system.SysDictionary;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @Auther: LUOLUO
 * @Date: 2019/5/11
 * @Description: com.xuecheng.manage_cms.dao
 * @version: 1.0
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class SysDictionaryRepositoryTest {
    @Autowired
    SysDictionaryRepository sysDictionaryRepository;

    @Test
    public void testFindByType(){
        SysDictionary sysDictionary = sysDictionaryRepository.findByDType("200");
        System.out.println(sysDictionary);
    }

}
