package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.model.response.QueryResponseResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * @Auther: LUOLUO
 * @Date: 2019/5/2
 * @Description: com.xuecheng.manage_cms.service
 * @version: 1.0
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class PageServiceTest {

    @Autowired
    private PageService pageService;

    @Test
    public void findList() {
        QueryPageRequest request = new QueryPageRequest();
        request.setSiteId("5a751fab6abb5044e0d19ea1");
        request.setPageAliase("课程详情页面");
        request.setTemplateId("5a925be7b00ffc4b3c1578b5");

        QueryResponseResult result = pageService.findList(1, 10, request);
        System.out.println(result);
    }

    @Test
    public void getPageHtml() {
        String html = pageService.getPageHtml("5a795ac7dd573c04508f3a56");
        System.out.println(html);
    }
}