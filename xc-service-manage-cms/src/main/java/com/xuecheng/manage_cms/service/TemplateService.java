package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.manage_cms.dao.CmsSiteRepository;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Auther: LUOLUO
 * @Date: 2019/5/2
 * @Description: com.xuecheng.manage_cms.service
 * @version: 1.0
 */
@Service
@Transactional
public class TemplateService {

    @Autowired
    private CmsTemplateRepository cmsTemplateRepository;

    public List<CmsTemplate> findAll(){
        return cmsTemplateRepository.findAll();
    }
}
