package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.course.Teachplan;
import com.xuecheng.framework.domain.course.ext.CategoryNode;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @Auther: LUOLUO
 * @Date: 2019/5/9
 * @Description: com.xuecheng.api.config
 * @version: 1.0
 */
@Api(value = "课程分类管理接口", description = "课程分类管理接口，提供课程的增、删、改、查")
public interface CategoryControllerApi {

    @ApiOperation("课程分类查询")
    CategoryNode findCategoryList();
}
