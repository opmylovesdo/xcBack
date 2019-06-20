package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.manage_course.ManageCourseApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

/**
 * @Auther: LUOLUO
 * @Date: 2019/5/16
 * @Description: com.xuecheng.manage_course.dao
 * @version: 1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ManageCourseApplication.class)
public class TestRibbon {
    @Autowired
    private RestTemplate restTemplate;

    @Test
    public void testRibbon(){
        String serviceId = "XC-SERVICE-MANAGE-CMS";
        for (int i = 0; i < 10; i++) {
            ResponseEntity<CmsPage> forEntity = restTemplate.getForEntity("http://" + serviceId
                    + "/cms/page/get/5a754adf6abb500ad05688d9", CmsPage.class);

            CmsPage cmsPage = forEntity.getBody();
            System.out.println(cmsPage);
        }
    }
}
