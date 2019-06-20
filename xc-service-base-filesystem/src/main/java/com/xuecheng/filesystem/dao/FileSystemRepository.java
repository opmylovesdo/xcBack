package com.xuecheng.filesystem.dao;

import com.xuecheng.framework.domain.filesystem.FileSystem;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @Auther: LUOLUO
 * @Date: 2019/5/14
 * @Description: com.xuecheng.filesystem.dao
 * @version: 1.0
 */
public interface FileSystemRepository extends MongoRepository<FileSystem, String> {
}
