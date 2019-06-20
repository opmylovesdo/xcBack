package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * @Auther: LUOLUO
 * @Date: 2019/5/1
 * @Description: com.xuecheng.manage_cms.dao
 * @version: 1.0
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@Transactional
public class CmsPageRepositoryTest {

    @Autowired
    private CmsPageRepository cmsPageRepository;


    @Test
    public void testFindAllByCriteria(){
        CmsPage request = new CmsPage();
        //request.setTemplateId("5a962c16b00ffc514038fafd");
//        request.setSiteId("5a751fab6abb5044e0d19ea1");
        request.setPageAliase("课程");
        ExampleMatcher matcher = ExampleMatcher.matching();
        matcher = matcher.withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains());
        Example<CmsPage> example = Example.of(request, matcher);
        List<CmsPage> list = cmsPageRepository.findAll(example);
        System.out.println(list);
    }


    @Test
    public void testFindAll(){
        cmsPageRepository.findAll().forEach(System.out::println);
    }

    @Test
    public void testFindPage(){

        int page = 1;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);
        Page<CmsPage> pageList = cmsPageRepository.findAll(pageable);
        pageList.getContent().forEach(System.out::println);

    }


    @Test
    public void testUpdate(){
        Optional<CmsPage> page = cmsPageRepository.findById("5abefd525b05aa293098fca6");
        if(page.isPresent()){
            CmsPage cmsPage = page.get();
            cmsPage.setPageAliase("ccc2");
            CmsPage save = cmsPageRepository.save(cmsPage);
            System.out.println(save);

        }
    }
}