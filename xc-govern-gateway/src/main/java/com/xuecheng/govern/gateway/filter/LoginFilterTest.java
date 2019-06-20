package com.xuecheng.govern.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Auther: LUOLUO
 * @Date: 2019/6/11
 * @Description: com.xuecheng.govern.gateway.filter
 * @version: 1.0
 */
//@Component
//@Slf4j
public class LoginFilterTest extends ZuulFilter {


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
//        RequestContext requestContext = RequestContext.getCurrentContext();
//        HttpServletRequest request = requestContext.getRequest();
//        HttpServletResponse response = requestContext.getResponse();
//
//        //取出头部信息Authorization
//        String authorization = request.getHeader("Authorization");
//        if(StringUtils.isEmpty(authorization)){
//            requestContext.setSendZuulResponse(false);//拒绝访问
//            requestContext.setResponseStatusCode(200);
//            ResponseResult responseResult = new ResponseResult(CommonCode.UNAUTHENTICATED);
//            String jsonString = JSON.toJSONString(responseResult);
//            requestContext.setResponseBody(jsonString);
//            response.setContentType("application/json;charset=UTF-8");
//            return null;
//        }

        return null;
    }
}
