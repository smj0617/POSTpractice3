package com.back.global.initData;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// 샘플 데이터가 "앱이 시작될 때 자동으로" 만들어지는지 검증한다
// 테스트 안에서 글을 직접 만들지 않는다는 점이 중요하다 → 스프링이 뜨기만 하면 글이 있어야 한다
@SpringBootTest         // 스프링을 띄운다 → 샘플 데이터를 만드는 코드(BaseInitData)도 실행된다
@ActiveProfiles("test") // 메모리 DB 사용 (application-test.yaml)
@Transactional          // 테스트가 끝나면 테스트 중에 바꾼 DB 내용을 되돌린다(롤백)
public class BaseInitDataTest {
    @Autowired // 스프링이 만들어 둔 PostService 객체를 넣어 준다
    private PostService postService;

    @Test
    @DisplayName("앱이 시작되면 샘플 글 3개가 자동으로 만들어져 있다")
    void t1() {
        assertThat(postService.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("샘플 글의 번호는 1, 2, 3 이고 제목·내용은 '제목 n', '내용 n' 이다")
    void t2() {
        List<Post> posts = postService.findAll();

        assertThat(posts)
                .extracting(Post::getId)
                .containsExactly(1L, 2L, 3L);
        assertThat(posts)
                .extracting(Post::getTitle)
                .containsExactly("제목 1", "제목 2", "제목 3");
        assertThat(posts)
                .extracting(Post::getContent)
                .containsExactly("내용 1", "내용 2", "내용 3");
    }

    @Test
    @DisplayName("샘플 글에는 작성일과 수정일이 자동으로 채워져 있다")
    void t3() {
        Post post = postService.findAll().getFirst();

        assertThat(post.getCreateDate()).isNotNull();
        assertThat(post.getModifyDate()).isNotNull();
    }
}
