package com.xuecheng.test.freemarker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * @Auther: LUOLUO
 * @Date: 2019/5/5
 * @Description: com.xuecheng.test.freemarker
 * @version: 1.0
 */
@SpringBootApplication
public class FreeMarkerTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(FreeMarkerTestApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate(new OkHttp3ClientHttpRequestFactory());
    }
}
