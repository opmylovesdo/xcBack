package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.cms.CmsTemplate;
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
@Api(value = "cms模板管理接口", description = "cms模板管理接口，提供模板的增、删、改、查")
public interface CmsTemplateControllerApi {
    @ApiOperation("查询模板列表")
    @ApiImplicitParams({

    })
    List<CmsTemplate> findList();
}
