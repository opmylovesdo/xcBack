package com.xuecheng.ucenter.dao;

import com.xuecheng.framework.domain.ucenter.XcUser;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @Auther: LUOLUO
 * @Date: 2019/6/11
 * @Description: com.xuecheng.ucenter.dao
 * @version: 1.0
 */
public interface XcUserRepository extends JpaRepository<XcUser, String> {
    XcUser findXcUserByUsername(String username);
    XcUser findByUsername(String username);
}
