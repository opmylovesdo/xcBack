package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.system.SysDictionary;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @Auther: LUOLUO
 * @Date: 2019/5/11
 * @Description: com.xuecheng.manage_cms.dao
 * @version: 1.0
 */
public interface SysDictionaryRepository extends MongoRepository<SysDictionary, String> {

    SysDictionary findByDType(String type);
}
