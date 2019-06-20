package com.xuecheng.api.ucenter;

import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @Auther: LUOLUO
 * @Date: 2019/6/11
 * @Description: com.xuecheng.api.ucenter
 * @version: 1.0
 */
@Api(value = "用户中心", description = "用户中心管理")
public interface UcenterControllerApi {

    @ApiOperation("根据用户账号查询用户信息")
    XcUserExt getUserExt(String username);
}
