package com.back.domain.post.post.controller;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.nullValue; // [005 추가]
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// [004 추가] 글 API 테스트
// 컨트롤러 테스트는 항상 2단계: ① 요청을 보낸다 → ② 응답을 검증한다
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc // MockMvc 를 준비해 준다
@Transactional        // 테스트 중에 바꾼 DB 내용은 끝나면 롤백 → 다른 테스트에 영향 없음
public class ApiV1PostControllerTest {
    // MockMvc: 실제 서버(8080)를 켜지 않고, 컨트롤러에 HTTP 요청을 보낸 것처럼 흉내 내는 도구
    @Autowired
    private MockMvc mvc;

    @Autowired
    private PostService postService;

    @Test
    @DisplayName("글 다건조회 - GET /api/v1/posts 요청하면 200 과 글 3개가 최신순(3, 2, 1)으로 응답된다")
    void t1() throws Exception {
        // ① 요청 보내기
        ResultActions resultActions = mvc
                .perform(get("/api/v1/posts"))
                .andDo(print()); // 요청/응답 내용을 콘솔에 출력 (테스트 리포트의 standard output 에서 확인 가능)

        // ② 응답 검증
        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class)) // 이 컨트롤러의
                .andExpect(handler().methodName("getItems"))                  // getItems 메서드가 처리했다
                .andExpect(status().isOk())                                   // 200
                .andExpect(jsonPath("$.length()").value(3))                   // 배열에 3개
                .andExpect(jsonPath("$[*].id").value(contains(3, 2, 1)));     // 번호가 3, 2, 1 순서 (최신순)
    }

    @Test
    @DisplayName("글 다건조회 - 응답의 각 글에는 id, createDate, modifyDate, title, content 가 들어 있고 comments 는 없다")
    void t2() throws Exception {
        // ① 요청 보내기
        ResultActions resultActions = mvc
                .perform(get("/api/v1/posts"))
                .andDo(print());

        // 비교 기준: DB에서 꺼낸 글 목록 (최신순으로 응답되므로 응답의 0번째 = 3번 글)
        List<Post> posts = postService.findAll().reversed();

        // ② 응답의 모든 글, 모든 필드를 DB 값과 비교
        for (int i = 0; i < posts.size(); i++) {
            Post post = posts.get(i);

            resultActions
                    .andExpect(jsonPath("$[%d].id".formatted(i)).value(post.getId()))
                    // 날짜는 소수점 아래 자릿수 표현이 달라질 수 있어서 앞 19글자(초 단위까지)만 비교한다
                    .andExpect(jsonPath("$[%d].createDate".formatted(i)).value(startsWith(post.getCreateDate().toString().substring(0, 19))))
                    .andExpect(jsonPath("$[%d].modifyDate".formatted(i)).value(startsWith(post.getModifyDate().toString().substring(0, 19))))
                    .andExpect(jsonPath("$[%d].title".formatted(i)).value(post.getTitle()))
                    .andExpect(jsonPath("$[%d].content".formatted(i)).value(post.getContent()))
                    .andExpect(jsonPath("$[%d].comments".formatted(i)).doesNotExist()); // 댓글은 댓글 API(009)로 따로 조회
        }
    }

    // ---------- [005 추가] ----------

    @Test
    @DisplayName("글 단건조회 - GET /api/v1/posts/1 요청하면 200 과 1번 글이 응답된다")
    void t3() throws Exception {
        // ① 요청 보내기
        ResultActions resultActions = mvc
                .perform(get("/api/v1/posts/1"))
                .andDo(print());

        // 비교 기준: DB의 1번 글
        Post post = postService.findAll().getFirst();

        // ② 응답 검증: 배열이 아니라 객체 하나({...})라서 $[0].id 가 아니라 $.id 로 꺼낸다
        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("getItem"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.createDate").value(startsWith(post.getCreateDate().toString().substring(0, 19))))
                .andExpect(jsonPath("$.modifyDate").value(startsWith(post.getModifyDate().toString().substring(0, 19))))
                .andExpect(jsonPath("$.title").value("제목 1"))
                .andExpect(jsonPath("$.content").value("내용 1"))
                .andExpect(jsonPath("$.comments").doesNotExist());
    }

    @Test
    @DisplayName("글 단건조회 - 없는 글(1000번)을 요청하면 404 와 RsData 형식의 실패 응답이 온다")
    void t4() throws Exception {
        // ① 요청 보내기
        ResultActions resultActions = mvc
                .perform(get("/api/v1/posts/1000"))
                .andDo(print());

        // ② 응답 검증: 조회 실패는 RsData 로 감싼다 (README 응답 규칙)
        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("getItem"))
                .andExpect(status().isNotFound())                              // 404
                .andExpect(jsonPath("$.resultCode").value("404-1"))
                .andExpect(jsonPath("$.msg").value("해당 데이터가 존재하지 않습니다.")) // [006 수정] 강의 48강 공통 메시지
                .andExpect(jsonPath("$.data").value(nullValue()));             // 키는 있고 값은 null
    }
}
