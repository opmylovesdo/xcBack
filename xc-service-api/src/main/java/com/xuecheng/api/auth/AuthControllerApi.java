package com.xuecheng.api.auth;

import com.xuecheng.framework.domain.ucenter.request.LoginRequest;
import com.xuecheng.framework.domain.ucenter.response.JwtResult;
import com.xuecheng.framework.domain.ucenter.response.LoginResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @Auther: LUOLUO
 * @Date: 2019/6/10
 * @Description: com.xuecheng.api.auth
 * @version: 1.0
 */
@Api(value="用户认证", description = "用户认证接口")
public interface AuthControllerApi {

    @ApiOperation("登陆")
    LoginResult login(LoginRequest loginRequest);

    @ApiOperation("退出")
    ResponseResult logout();

    @ApiOperation("查询userjwt令牌")
    JwtResult userJwt();
}
