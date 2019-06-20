package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.cms.CmsSite;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

import java.util.List;

/**
 * @Auther: LUOLUO
 * @Date: 2019/5/1
 * @Description: com.xuecheng.api.config
 * @version: 1.0
 */
@Api(value = "cms网站管理接口", description = "cms网站管理接口，提供页面的增、删、改、查")
public interface CmsSiteControllerApi {
    @ApiOperation("查询网站列表")
    @ApiImplicitParams({

    })
    List<CmsSite> findList();
}
