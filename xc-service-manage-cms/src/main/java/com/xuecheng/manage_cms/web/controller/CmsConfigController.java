package com.xuecheng.manage_cms.web.controller;

import com.xuecheng.api.cms.CmsConfigControllerApi;
import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.manage_cms.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Auther: LUOLUO
 * @Date: 2019/5/5
 * @Description: com.xuecheng.manage_cms.web.controller
 * @version: 1.0
 */
@RequestMapping("/cms/config")
@RestController
public class CmsConfigController implements CmsConfigControllerApi {

    @Autowired
    private ConfigService configService;

    @Override
    @GetMapping("/getmodel/{id}")
    public CmsConfig getModel(@PathVariable("id") String id) {
        CmsConfig config = configService.getModel(id);
        return config;
    }
}
