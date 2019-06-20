package com.xuecheng.manage_cms.web.controller;

import com.xuecheng.api.config.SysDictionaryControllerApi;
import com.xuecheng.framework.domain.system.SysDictionary;
import com.xuecheng.manage_cms.service.SysDictionaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Auther: LUOLUO
 * @Date: 2019/5/11
 * @Description: com.xuecheng.manage_cms.web.controller
 * @version: 1.0
 */
@RestController
@RequestMapping("/sys")
public class SysDictionaryController implements SysDictionaryControllerApi {

    @Autowired
    private SysDictionaryService sysDictionaryService;

    @Override
    @GetMapping("/dictionary/get/{dtype}")
    public SysDictionary findByType(@PathVariable("dtype") String type) {
        return sysDictionaryService.findByType(type);
    }
}
