package com.xuecheng.auth.controller;

import com.xuecheng.api.auth.AuthControllerApi;
import com.xuecheng.auth.service.AuthService;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.request.LoginRequest;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.domain.ucenter.response.JwtResult;
import com.xuecheng.framework.domain.ucenter.response.LoginResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.utils.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @Auther: LUOLUO
 * @Date: 2019/6/10
 * @Description: com.xuecheng.auth.controller
 * @version: 1.0
 */
@RestController
public class AuthController implements AuthControllerApi {

    @Autowired
    private AuthService authService;

    @Value("${auth.clientId}")
    private String clientId;
    @Value("${auth.clientSecret}")
    private String clientSecret;
    @Value("${auth.cookieDomain}")
    private String cookieDomain;
    @Value("${auth.cookieMaxAge}")
    private int cookieMaxAge;
    @Value("${auth.tokenValiditySeconds}")
    private int tokenValiditySeconds;

    @Override
    @PostMapping("/userlogin")
    public LoginResult login(LoginRequest loginRequest) {
        //校验账号是否输入
        if(loginRequest == null || StringUtils.isEmpty(loginRequest.getUsername())){
            ExceptionCast.cast(AuthCode.AUTH_PASSWORD_NONE);
        }
        //校验密码是否输入
        if(StringUtils.isEmpty(loginRequest.getPassword())){
            ExceptionCast.cast(AuthCode.AUTH_PASSWORD_NONE);
        }

        //申请令牌
        AuthToken authToken = authService.login(loginRequest.getUsername(),
                loginRequest.getPassword(), clientId, clientSecret);

        //将令牌存储到cookie
        String accessToken = authToken.getAccess_token();
        saveCookie(accessToken);

        return new LoginResult(CommonCode.SUCCESS, accessToken);
    }

    //将令牌存入cookie中
    private void saveCookie(String token){
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        CookieUtil.addCookie(response, cookieDomain, "/", "uid", token, cookieMaxAge, false);
    }

    //从cookie中删除accessToken
    private void delCookie(String token){
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        CookieUtil.addCookie(response, cookieDomain, "/", "uid", token, 0, false);
    }


    @Override
    @PostMapping("/userlogout")
    public ResponseResult logout() {
        //从cookie中取出用户身份令牌
        String uid = getTokenFromCookie();

        //删除redis中的token
        boolean result = authService.delToken(uid);
        //删除cookie中accessToken
        this.delCookie(uid);

        return new ResponseResult(CommonCode.SUCCESS);
    }


    @Override
    @GetMapping("/userjwt")
    public JwtResult userJwt() {
        //获取cookie中的令牌
        String accessToken = this.getTokenFromCookie();
        if(accessToken == null){
            return new JwtResult(CommonCode.FAIL, null);
        }
        //根据令牌从redis查询jwt
        AuthToken userToken = authService.getUserToken(accessToken);
        if(userToken == null){
            return new JwtResult(CommonCode.FAIL, null);
        }

        return new JwtResult(CommonCode.SUCCESS, userToken.getJwt_token());
    }

    //从cookie中读取令牌
    private String getTokenFromCookie(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        Map<String, String> cookieMap = CookieUtil.readCookie(request, "uid");
        String accessToken = null;
        if(cookieMap != null) {
            accessToken = cookieMap.get("uid");
        }
        return accessToken;
    }
}
