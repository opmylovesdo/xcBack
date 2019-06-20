package com.xuecheng.ucenter.dao;

import com.xuecheng.framework.domain.ucenter.XcCompanyUser;
import com.xuecheng.framework.domain.ucenter.XcUser;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @Auther: LUOLUO
 * @Date: 2019/6/11
 * @Description: com.xuecheng.ucenter.dao
 * @version: 1.0
 */
public interface XcCompanyUserRepository extends JpaRepository<XcCompanyUser, String> {
    //根据用户id查询所属企业id
    XcCompanyUser findByUserId(String userId);
}
