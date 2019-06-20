package com.xuecheng.manage_cms.web.controller;

import com.xuecheng.framework.web.BaseController;
import com.xuecheng.manage_cms.service.PageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletOutputStream;
import java.io.IOException;

/**
 * @Auther: LUOLUO
 * @Date: 2019/5/6
 * @Description: com.xuecheng.manage_cms.web.controller
 * @version: 1.0
 */
@Controller
public class CmsPagePreviewController extends BaseController {

    @Autowired
    private PageService pageService;

    @GetMapping("/cms/preview/{pageId}")
    public void previce(@PathVariable("pageId") String pageId){
        String pageHtml = pageService.getPageHtml(pageId);

        if(StringUtils.isNotEmpty(pageHtml)){
            try {
                response.setHeader("Content-type","text/html;charset=utf-8");
                ServletOutputStream outputStream = response.getOutputStream();
                outputStream.write(pageHtml.getBytes("utf-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
