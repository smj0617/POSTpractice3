package com.back.global.initData;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

// 앱이 시작될 때 샘플 데이터를 자동으로 만든다
@Configuration // 스프링 설정 클래스. 안에 있는 @Bean 메서드를 스프링이 실행해서 객체를 등록한다
@RequiredArgsConstructor
public class BaseInitData {
    // [003 추가] 자기 자신을 "스프링이 만든 대리 객체(프록시)"로 주입받는다
    // work1() 의 @Transactional 은 프록시를 거쳐 호출될 때만 동작한다.
    // this.work1() 로 부르면 프록시를 안 거치므로 트랜잭션이 걸리지 않는다 → self.work1() 로 부른다
    // @Lazy: 자기 자신을 주입받을 때 생기는 순환 문제를 피하려고, 실제로 쓸 때 주입받는다
    @Autowired
    @Lazy
    private BaseInitData self;
    private final PostService postService;

    // ApplicationRunner: 앱 시작이 끝나면 run() 을 자동으로 1번 실행해 주는 인터페이스
    // 여기서는 람다(args -> ...)로 run() 의 내용을 적었다
    @Bean
    ApplicationRunner baseInitDataApplicationRunner() {
        return args -> self.work1(); // [003 수정] work1() → self.work1()
    }

    // [003 수정] private → public, @Transactional 추가
    // 메서드 전체가 하나의 트랜잭션 → 끝날 때 JPA가 바뀐 객체를 모아서 DB에 반영한다
    // 트랜잭션이 없으면 addComment() 는 "자바 객체의 리스트에 넣기"로 끝나고 DB에는 아무 일도 안 일어난다
    @Transactional
    public void work1() {
        if (postService.count() > 0) return; // 이미 데이터가 있으면 또 만들지 않는다 (앱을 재시작해도 유지)

        Post post1 = postService.write("제목 1", "내용 1"); // → 1번 글
        Post post2 = postService.write("제목 2", "내용 2"); // → 2번 글
        postService.write("제목 3", "내용 3");              // → 3번 글

        // [003 추가] 샘플 댓글 5개 (1번 글 3개, 2번 글 2개, 3번 글 0개)
        // 댓글은 save() 를 따로 부르지 않는다
        // → 트랜잭션이 끝날 때 Post 의 cascade = PERSIST 설정으로 함께 INSERT 된다
        post1.addComment("댓글 1-1"); // → 1번 댓글
        post1.addComment("댓글 1-2"); // → 2번 댓글
        post1.addComment("댓글 1-3"); // → 3번 댓글
        post2.addComment("댓글 2-1"); // → 4번 댓글
        post2.addComment("댓글 2-2"); // → 5번 댓글
    }
}
