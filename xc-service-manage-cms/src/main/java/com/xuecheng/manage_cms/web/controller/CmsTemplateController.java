package com.xuecheng.manage_cms.web.controller;

import com.xuecheng.api.cms.CmsTemplateControllerApi;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.manage_cms.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Auther: LUOLUO
 * @Date: 2019/5/1
 * @Description: com.xuecheng.manage_cms.web.controller
 * @version: 1.0
 */
@RequestMapping("/cms/template")
@RestController
public class CmsTemplateController implements CmsTemplateControllerApi {

    @Autowired
    private TemplateService templateService;

    @Override
    @GetMapping("/list")
    public List<CmsTemplate> findList() {
        return templateService.findAll();
    }
}
