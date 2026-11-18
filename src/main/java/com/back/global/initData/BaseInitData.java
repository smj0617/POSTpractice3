package com.back.global.initData;

import com.back.domain.post.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 앱이 시작될 때 샘플 데이터를 자동으로 만든다
@Configuration // 스프링 설정 클래스. 안에 있는 @Bean 메서드를 스프링이 실행해서 객체를 등록한다
@RequiredArgsConstructor
public class BaseInitData {
    private final PostService postService;

    // ApplicationRunner: 앱 시작이 끝나면 run() 을 자동으로 1번 실행해 주는 인터페이스
    // 여기서는 람다(args -> ...)로 run() 의 내용을 적었다
    @Bean
    ApplicationRunner baseInitDataApplicationRunner() {
        return args -> work1();
    }

    private void work1() {
        if (postService.count() > 0) return; // 이미 데이터가 있으면 또 만들지 않는다 (앱을 재시작해도 3개 유지)

        postService.write("제목 1", "내용 1"); // → 1번 글
        postService.write("제목 2", "내용 2"); // → 2번 글
        postService.write("제목 3", "내용 3"); // → 3번 글
    }
}
