package com.xuecheng.manage_cms_client.dao;

import com.xuecheng.framework.domain.cms.CmsSite;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @Auther: LUOLUO
 * @Date: 2019/5/1
 * @Description: com.xuecheng.manage_cms.dao
 * @version: 1.0
 */
public interface CmsSiteRepository extends MongoRepository<CmsSite, String> {
}
