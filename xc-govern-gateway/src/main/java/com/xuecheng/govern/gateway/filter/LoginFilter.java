package com.xuecheng.govern.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.netflix.discovery.converters.Auto;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.utils.CookieUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @Auther: LUOLUO
 * @Date: 2019/6/11
 * @Description: com.xuecheng.govern.gateway.filter
 * @version: 1.0
 */
@Component
@Slf4j
public class LoginFilter extends ZuulFilter {


    /**
     * filterType：返回字符串代表过滤器的类型，如下
     * pre：请求在被路由之前执行
     * routing：在路由请求时调用
     * post：在routing和error过滤器之后调用
     * error：处理请求时发生错误调用
     * @return
     */
    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 0;//int值来定义过滤器的执行顺序，数值越小优先级越高
    }

    @Override
    public boolean shouldFilter() {
        return true;// 该过滤器需要执行
    }

    @Override
    public Object run() throws ZuulException {
        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletRequest request = requestContext.getRequest();
        HttpServletResponse response = requestContext.getResponse();
        //取cookie中身份令牌
        String accessTokenFromCookie = this.getTokenFromCookie(request);
        if(StringUtils.isEmpty(accessTokenFromCookie)){
            //拒绝访问
            rejectAccess(requestContext, response);
            return null;
        }
        //从header中取jwt
        String tokenFromHeader = this.getJwtFromRequestHeader(request);
        if(StringUtils.isEmpty(tokenFromHeader)){
            //拒绝访问
            rejectAccess(requestContext, response);
            return null;
        }

        long expire = this.getExpire(accessTokenFromCookie);
        if(expire < 0){
            //拒绝访问
            rejectAccess(requestContext, response);
            return null;
        }
        return null;
    }


    //从请求头取出jwt令牌
    public String getJwtFromRequestHeader(HttpServletRequest request){
        //Basic WGNXZWJBcHA6WGNXZWJBcHA=
        //Bearer 用户令牌
        String authorization = request.getHeader("Authorization");
        if(StringUtils.isEmpty(authorization)){
            //拒绝访问
            return null;
        }

        if(!authorization.startsWith("Bearer ")){
            //拒绝访问
            return null;
        }

        return authorization;
    }

    //从cookie取出token
    public String getTokenFromCookie(HttpServletRequest request){
        Map<String, String> cookieMap = CookieUtil.readCookie(request, "uid");
        String accessToken = cookieMap.get("uid");
//        if(StringUtils.isEmpty(accessToken)){
//            return null;
//        }
        return accessToken;
    }


    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    //从redis查询jwt令牌的有效期
    public long getExpire(String accessToken){
        //user_token:26512c9b-fe0e-4e90-8f6b-ed921ed57569
        String key = "user_token:" + accessToken;
        Long expire = stringRedisTemplate.getExpire(key);
        //过期时返回-2
        return expire;
    }


    //拒绝访问
    public void rejectAccess(RequestContext requestContext,HttpServletResponse response){
        requestContext.setSendZuulResponse(false);//拒绝访问
        requestContext.setResponseStatusCode(200);
        ResponseResult responseResult = new ResponseResult(CommonCode.UNAUTHENTICATED);
        String jsonString = JSON.toJSONString(responseResult);
        requestContext.setResponseBody(jsonString);
        response.setContentType("application/json;charset=UTF-8");
    }
}
