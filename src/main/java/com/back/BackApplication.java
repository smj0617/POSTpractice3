package com.back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// 이 클래스가 있는 패키지(com.back)와 그 하위 패키지를 스캔해서
// @Service, @Controller 등이 붙은 클래스를 스프링이 자동으로 찾아 객체로 만든다
@SpringBootApplication
public class BackApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackApplication.class, args); // 내장 톰캣 서버를 켜고 8080 포트에서 대기
    }
}
