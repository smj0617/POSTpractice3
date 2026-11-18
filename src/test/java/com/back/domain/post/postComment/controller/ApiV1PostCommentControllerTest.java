package com.back.domain.post.postComment.controller;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import com.back.domain.post.postComment.entity.PostComment;
import jakarta.persistence.EntityManager; // [008 추가]
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType; // [013 추가]
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.assertj.core.api.Assertions.assertThat; // [008 추가]
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete; // [008 추가]
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post; // [014 추가]
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put; // [013 추가]
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// [007 추가] 댓글 API 테스트 (강의 10강 기능, 54강 테스트)
// 샘플 데이터: 1번 글 - 댓글 1, 2, 3 / 2번 글 - 댓글 4, 5 / 3번 글 - 댓글 없음
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional // 테스트 메서드 전체가 트랜잭션 → post.getComments() 처럼 나중에 불러오는(LAZY) 값도 꺼낼 수 있다
public class ApiV1PostCommentControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private PostService postService;

    // [008 추가] JPA가 관리하는 객체들의 "작업 공간"(영속성 컨텍스트)을 직접 다루는 도구
    // flush() : 모아 둔 변경을 지금 DB로 보낸다 / clear() : 작업 공간을 비운다 → 다음 조회는 DB에서 새로 읽는다
    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("댓글 다건조회 - GET /api/v1/posts/1/comments 요청하면 200 과 1번 글의 댓글 3개가 작성순(1, 2, 3)으로 응답된다")
    void t1() throws Exception {
        // ① 요청 보내기
        ResultActions resultActions = mvc
                .perform(get("/api/v1/posts/1/comments"))
                .andDo(print());

        // 비교 기준: DB의 1번 글에 달린 댓글들
        List<PostComment> comments = postService.findById(1).get().getComments();

        // ② 응답 검증
        resultActions
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("getItems"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[*].id").value(contains(1, 2, 3))); // 먼저 단 댓글이 먼저

        // 모든 댓글, 모든 필드를 DB 값과 비교
        for (int i = 0; i < comments.size(); i++) {
            PostComment comment = comments.get(i);

            resultActions
                    .andExpect(jsonPath("$[%d].id".formatted(i)).value(comment.getId()))
                    .andExpect(jsonPath("$[%d].createDate".formatted(i)).value(startsWith(comment.getCreateDate().toString().substring(0, 19))))
                    .andExpect(jsonPath("$[%d].modifyDate".formatted(i)).value(startsWith(comment.getModifyDate().toString().substring(0, 19))))
                    .andExpect(jsonPath("$[%d].postId".formatted(i)).value(1))  // 어느 글의 댓글인지
                    .andExpect(jsonPath("$[%d].content".formatted(i)).value(comment.getContent()))
                    .andExpect(jsonPath("$[%d].post".formatted(i)).doesNotExist()); // 글 객체 전체는 넣지 않는다
        }
    }

    @Test
    @DisplayName("댓글 다건조회 - 댓글이 없는 3번 글은 200 과 빈 배열이 응답된다")
    void t2() throws Exception {
        mvc
                .perform(get("/api/v1/posts/3/comments"))
                .andDo(print())
                .andExpect(handler().methodName("getItems"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0)); // [] : 실패가 아니라 "0개"라는 정상 결과
    }

    @Test
    @DisplayName("댓글 다건조회 - 없는 글(1000번)의 댓글을 요청하면 404")
    void t3() throws Exception {
        mvc
                .perform(get("/api/v1/posts/1000/comments"))
                .andDo(print())
                .andExpect(handler().methodName("getItems"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-1"))
                .andExpect(jsonPath("$.msg").value("해당 데이터가 존재하지 않습니다."))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    @DisplayName("댓글 단건조회 - GET /api/v1/posts/1/comments/2 요청하면 200 과 2번 댓글이 응답된다")
    void t4() throws Exception {
        // ① 요청 보내기
        ResultActions resultActions = mvc
                .perform(get("/api/v1/posts/1/comments/2"))
                .andDo(print());

        // 비교 기준: 1번 글의 두 번째 댓글 = 2번 댓글
        Post post = postService.findById(1).get();
        PostComment comment = post.getComments().get(1);

        // ② 응답 검증: 객체 하나라서 $.id
        resultActions
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("getItem"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.createDate").value(startsWith(comment.getCreateDate().toString().substring(0, 19))))
                .andExpect(jsonPath("$.modifyDate").value(startsWith(comment.getModifyDate().toString().substring(0, 19))))
                .andExpect(jsonPath("$.postId").value(1))
                .andExpect(jsonPath("$.content").value("댓글 1-2"));
    }

    @Test
    @DisplayName("댓글 단건조회 - 다른 글의 댓글(2번 글의 4번 댓글)을 1번 글 주소로 요청하면 404")
    void t5() throws Exception {
        // 4번 댓글은 존재하지만 1번 글의 댓글이 아니다 → "1번 글의 4번 댓글"은 없다 (GLOSSARY: 댓글은 그 글의 댓글로만 찾는다)
        mvc
                .perform(get("/api/v1/posts/1/comments/4"))
                .andDo(print())
                .andExpect(handler().methodName("getItem"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-1"))
                .andExpect(jsonPath("$.msg").value("해당 데이터가 존재하지 않습니다."));
    }

    // ---------- [008 추가] 댓글 삭제 (강의 11~12강 기능, 55강 테스트) ----------

    @Test
    @DisplayName("댓글 삭제 - DELETE /api/v1/posts/1/comments/1 요청하면 200, RsData 응답, DB에서 1번 댓글이 사라진다")
    void t6() throws Exception {
        // ① 요청 보내기
        ResultActions resultActions = mvc
                .perform(delete("/api/v1/posts/1/comments/1"))
                .andDo(print());

        // ② 응답 검증: 삭제는 "조회"가 아니므로 성공도 RsData 로 감싼다 (README 응답 규칙)
        resultActions
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("deleteItem"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("1번 댓글이 삭제되었습니다."))
                .andExpect(jsonPath("$.data").value(nullValue()));

        // ③ DB 검증: 작업 공간에 남은 자바 객체가 아니라 "DB에 실제로 남은 것"을 확인한다
        em.flush(); // 지금까지의 변경(DELETE 등)을 DB에 보낸다
        em.clear(); // 작업 공간을 비운다 → 아래 findById 는 DB에서 새로 읽어 온다

        Post post1 = postService.findById(1).get();
        assertThat(post1.getComments())
                .extracting(PostComment::getId)
                .containsExactly(2L, 3L); // 1번 댓글만 사라지고 2, 3번은 남는다
    }

    @Test
    @DisplayName("댓글 삭제 - 다른 글의 댓글(2번 글의 4번 댓글)을 1번 글 주소로 삭제 요청하면 404, 4번 댓글은 그대로 남는다")
    void t7() throws Exception {
        mvc
                .perform(delete("/api/v1/posts/1/comments/4"))
                .andDo(print())
                .andExpect(handler().methodName("deleteItem"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-1"));

        em.flush();
        em.clear();

        Post post2 = postService.findById(2).get();
        assertThat(post2.getComments())
                .extracting(PostComment::getId)
                .containsExactly(4L, 5L); // 아무것도 지워지지 않았다
    }

    // ---------- [013 추가] 댓글 수정 (강의 32강 기능, 55강 테스트) ----------

    @Test
    @DisplayName("댓글 수정 - PUT /api/v1/posts/1/comments/1 로 새 내용을 보내면 200, RsData 응답, DB의 1번 댓글 내용과 수정일이 바뀐다")
    void t8() throws Exception {
        // 비교 기준: 수정 전 1번 댓글의 작성일·수정일
        PostComment before = postService.findById(1).get().findCommentById(1).get();
        var createDateBefore = before.getCreateDate();
        var modifyDateBefore = before.getModifyDate();

        // ① 요청 보내기
        ResultActions resultActions = mvc
                .perform(
                        put("/api/v1/posts/1/comments/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "content": "댓글 수정"
                                        }
                                        """)
                )
                .andDo(print());

        // ② 응답 검증
        resultActions
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("modify"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("1번 댓글이 수정되었습니다."))
                .andExpect(jsonPath("$.data").value(nullValue()));

        // ③ DB 검증
        em.flush();
        em.clear();

        PostComment after = postService.findById(1).get().findCommentById(1).get();
        assertThat(after.getContent()).isEqualTo("댓글 수정");
        assertThat(after.getCreateDate()).isEqualTo(createDateBefore);
        assertThat(after.getModifyDate()).isAfter(modifyDateBefore);
    }

    @Test
    @DisplayName("댓글 수정 - 다른 글의 댓글(2번 글의 4번 댓글)을 1번 글 주소로 수정 요청하면 404, 4번 댓글은 그대로")
    void t9() throws Exception {
        mvc
                .perform(
                        put("/api/v1/posts/1/comments/4")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "content": "댓글 수정"
                                        }
                                        """)
                )
                .andDo(print())
                .andExpect(handler().methodName("modify"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-1"));

        em.flush();
        em.clear();

        assertThat(postService.findById(2).get().findCommentById(4).get().getContent()).isEqualTo("댓글 2-1");
    }

    @Test
    @DisplayName("댓글 수정 - 내용이 공백뿐이면 400")
    void t10() throws Exception {
        mvc
                .perform(
                        put("/api/v1/posts/1/comments/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "content": "   "
                                        }
                                        """)
                )
                .andDo(print())
                .andExpect(handler().methodName("modify"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-1"))
                .andExpect(jsonPath("$.msg").value("content-NotBlank-must not be blank")); // "   " 은 3글자라 @Size 는 통과, @NotBlank 만 걸린다
    }

    // ---------- [014 추가] 댓글 작성 (강의 56강: TDD 로 댓글 작성 구현) ----------

    @Test
    @DisplayName("댓글 작성 - POST /api/v1/posts/1/comments 로 내용을 보내면 201, RsData 에 작성된 댓글(번호 포함)이 담겨 오고, 1번 글의 댓글이 4개가 된다")
    void t11() throws Exception {
        // ① 요청 보내기
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/posts/1/comments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "content": "새 댓글"
                                        }
                                        """)
                )
                .andDo(print());

        // 비교 기준: DB에서 다시 읽은 1번 글의 마지막 댓글 = 방금 작성된 댓글
        em.flush();
        em.clear();
        Post post1 = postService.findById(1).get();
        PostComment comment = post1.getComments().getLast();

        // ② 응답 검증
        resultActions
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(status().isCreated()) // ResponseAspect 가 "201-1" 을 보고 201 로 (011)
                .andExpect(jsonPath("$.resultCode").value("201-1"))
                .andExpect(jsonPath("$.msg").value("%d번 댓글이 작성되었습니다.".formatted(comment.getId())))
                .andExpect(jsonPath("$.data.id").value(comment.getId()))
                .andExpect(jsonPath("$.data.createDate").value(startsWith(comment.getCreateDate().toString().substring(0, 19))))
                .andExpect(jsonPath("$.data.modifyDate").value(startsWith(comment.getModifyDate().toString().substring(0, 19))))
                .andExpect(jsonPath("$.data.postId").value(1))
                .andExpect(jsonPath("$.data.content").value("새 댓글"));

        // ③ DB 검증: 1번 글의 댓글이 3개 → 4개
        assertThat(post1.getComments()).hasSize(4);
        assertThat(comment.getContent()).isEqualTo("새 댓글");
    }

    @Test
    @DisplayName("댓글 작성 - 없는 글(1000번)에 댓글을 쓰면 404")
    void t12() throws Exception {
        mvc
                .perform(
                        post("/api/v1/posts/1000/comments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "content": "새 댓글"
                                        }
                                        """)
                )
                .andDo(print())
                .andExpect(handler().methodName("write"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-1"));
    }

    @Test
    @DisplayName("댓글 작성 - 내용이 1글자면 400, 댓글은 늘지 않는다")
    void t13() throws Exception {
        mvc
                .perform(
                        post("/api/v1/posts/1/comments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "content": "새"
                                        }
                                        """)
                )
                .andDo(print())
                .andExpect(handler().methodName("write"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-1"))
                .andExpect(jsonPath("$.msg").value("content-Size-size must be between 2 and 1000"));

        assertThat(postService.findById(1).get().getComments()).hasSize(3);
    }
}
