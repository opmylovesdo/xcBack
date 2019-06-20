package com.xuecheng.auth;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

/**
 * @Auther: LUOLUO
 * @Date: 2019/6/10
 * @Description: com.xuecheng.auth
 * @version: 1.0
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestRedis {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void testRedis(){
        //定义key
        String key = "user_token:0f5b9cf5-5f7f-463b-b2da-e0c5ef5621dd";
//        Map<String, String> value = new HashMap<>();
//        value.put("jwt", "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjoiaXRjYXN0In0.lQOqL1s4DpDHROUAibkz6EMf6hcM7HmTPgmg-SlkacVoQAV7y3XQ7LXxiua6SJlN_uNX_EFjzIshEg_kyy972DtymtRMc2NIO5HzIF5I4oQCxNPsJdhu6qQni6sTas3q0JbAarMZSajDX7HhzVSYWPQJCussA4e1r9oFxDcoAo6TEAXOW8gRHzNIygQz1yCj6mdf4UOHI070kRy7f3BdhmrUJdOuDIMoRBYS4WsEOibAU1UCNPaJAXpZC0ihrtdY7SCg1N43fimeFOHrfpLb6OmRF7v7uvGMgrhg9JIYDbJ6nbode5OJkNceRx8QUICre2yKAe0ctlvXO0REf6OpRA");
//        value.put("refresh_token", "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21wYW55SWQiOm51bGwsInVzZXJwaWMiOm51bGwsInVzZXJfbmFtZSI6Iml0Y2FzdCIsInNjb3BlIjpbImFwcCJdLCJhdGkiOiIwZjViOWNmNS01ZjdmLTQ2M2ItYjJkYS1lMGM1ZWY1NjIxZGQiLCJuYW1lIjpudWxsLCJ1dHlwZSI6bnVsbCwiaWQiOm51bGwsImV4cCI6MTU2MDE4NjE5MywianRpIjoiY2FlYjljMTUtZGE1Ni00NzhjLTgwMDYtZmMzOWRlMTQ3NWQ1IiwiY2xpZW50X2lkIjoiWGNXZWJBcHAifQ.ezldHKFSDlTBUQxY_dNoDIXyAn7TdjOhdmtzHOMJFVaQqaVpT8b-rTL_MbAvGMVTPQ3pyBX9-Ee5re6yRU0fd6ojrVKL_ofyWmyNP1DYxZ2fIMHa3J6elyIVagHuAft6MIL6FtgsIbOH-mw4SBJ3-4ED7phknUMHRGKI-WJHLd21-wXtkPBGJbjztuSEiMWNQ84yvcqRjrXnRqz4pVJKBy_QinjHfsRspK93E0Bpr1PL9VXMvmpyxBtIDzaw15svZ39Ky9E5KfiTaPuTvWl4G5X2Krt6Qu-UAxTIwcEpPrqvUKgZwbZ1VFhiASoJoOzaEDjZXd6dIKLZHtnr0QeABQ");
//        stringRedisTemplate.boundValueOps(key).set(JSON.toJSONString(value), 60 , TimeUnit.SECONDS);
        //读取过期时间
        Long expire = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        System.out.println("expire:"+expire);
        //根据key获取值
        String s = stringRedisTemplate.opsForValue().get(key);
        System.out.println(s);

    }

}
