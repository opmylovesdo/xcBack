package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.manage_cms.dao.CmsSiteRepository;
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
public class SiteService {

    @Autowired
    private CmsSiteRepository cmsSiteRepository;

    public List<CmsSite> findAll(){
        return cmsSiteRepository.findAll();
    }
}
