package com.back.global.initData;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import com.back.domain.post.postComment.entity.PostComment; // [003 추가]
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

    // ---------- [003 추가] ----------

    @Test
    @DisplayName("앱이 시작되면 샘플 댓글 5개가 DB에 저장되어 있다")
    void t4() {
        // findAll() 로 DB에서 글을 읽어 오고, 각 글의 댓글 수를 더한다
        int totalComments = postService.findAll().stream()
                .mapToInt(post -> post.getComments().size())
                .sum();

        assertThat(totalComments).isEqualTo(5);
    }

    @Test
    @DisplayName("1번 글에 댓글 3개, 2번 글에 댓글 2개, 3번 글에는 댓글이 없다")
    void t5() {
        List<Post> posts = postService.findAll(); // [1번 글, 2번 글, 3번 글]

        assertThat(posts.get(0).getComments())
                .extracting(PostComment::getContent)
                .containsExactly("댓글 1-1", "댓글 1-2", "댓글 1-3");
        assertThat(posts.get(1).getComments())
                .extracting(PostComment::getContent)
                .containsExactly("댓글 2-1", "댓글 2-2");
        assertThat(posts.get(2).getComments()).isEmpty();
    }

    @Test
    @DisplayName("샘플 댓글의 번호는 1 ~ 5 이고, 각 댓글은 자기가 속한 글을 알고 있다")
    void t6() {
        Post post1 = postService.findAll().getFirst();
        PostComment comment = post1.getComments().getFirst(); // 1번 글의 첫 댓글

        assertThat(comment.getId()).isEqualTo(1L);
        assertThat(comment.getPost().getId()).isEqualTo(1L); // 댓글 → 글 방향으로도 연결되어 있다

        List<Long> allCommentIds = postService.findAll().stream()
                .flatMap(post -> post.getComments().stream())
                .map(PostComment::getId)
                .toList();
        assertThat(allCommentIds).containsExactly(1L, 2L, 3L, 4L, 5L);
    }
}
