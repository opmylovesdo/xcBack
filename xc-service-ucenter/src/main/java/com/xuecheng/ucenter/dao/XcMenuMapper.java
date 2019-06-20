package com.xuecheng.ucenter.dao;

import com.xuecheng.framework.domain.ucenter.XcMenu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Auther: LUOLUO
 * @Date: 2019/6/18
 * @Description: com.xuecheng.ucenter.dao
 * @version: 1.0
 */
@Mapper
public interface XcMenuMapper {
    List<XcMenu> selectPermissionByUserId(String userId);
}
