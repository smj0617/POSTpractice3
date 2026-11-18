package com.back;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest // 실제 앱을 실행할 때처럼 스프링을 통째로 띄운다
class BackApplicationTests {
    @Test
    @DisplayName("앱(스프링)이 에러 없이 실행된다")
    void contextLoads() {
        // 내용이 비어 있어도 된다.
        // 스프링이 뜨는 도중에 에러가 나면 이 테스트는 실패한다.
    }
}
