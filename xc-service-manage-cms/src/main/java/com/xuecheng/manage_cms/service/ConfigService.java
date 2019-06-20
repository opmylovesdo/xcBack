package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.manage_cms.dao.CmsConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * @Auther: LUOLUO
 * @Date: 2019/5/5
 * @Description: com.xuecheng.manage_cms.service
 * @version: 1.0
 */
@Service
@Transactional
public class ConfigService {
    @Autowired
    private CmsConfigRepository cmsConfigRepository;

    public CmsConfig getModel(String id) {
        Optional<CmsConfig> optional = cmsConfigRepository.findById(id);
        if(optional.isPresent()){
            return optional.get();
        }
        return null;
    }

}
