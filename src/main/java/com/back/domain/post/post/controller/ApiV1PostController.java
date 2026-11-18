package com.back.domain.post.post.controller;

import com.back.domain.post.post.dto.PostDto;
import com.back.domain.post.post.entity.Post;               // [005 추가]
import com.back.domain.post.post.service.PostService;
import com.back.global.rsData.RsData;                         // [009 추가]
import jakarta.validation.Valid;                              // [010 추가]
import jakarta.validation.constraints.NotBlank;               // [010 추가]
import jakarta.validation.constraints.Size;                   // [010 추가]
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping; // [009 추가]
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable; // [005 추가]
import org.springframework.web.bind.annotation.PostMapping;   // [010 추가]
import org.springframework.web.bind.annotation.RequestBody;   // [010 추가]
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// [004 추가] 글 API 컨트롤러: HTTP 요청을 받아서 서비스를 부르고, 결과를 JSON 으로 응답한다
// @RestController = @Controller + @ResponseBody
//   → 메서드가 돌려준 객체를 HTML 화면이 아니라 JSON 으로 바꿔서 응답 본문에 넣는다
// @RequestMapping("/api/v1/posts") : 이 클래스의 모든 주소 앞에 공통으로 붙는 접두어
//   - api : 화면(HTML)이 아니라 데이터(JSON)를 주는 주소라는 표시
//   - v1  : API 버전. 나중에 응답 모양을 크게 바꿔야 하면 v2 를 따로 만들어 기존 사용자를 보호한다
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class ApiV1PostController {
    private final PostService postService;

    // 글 다건조회: GET /api/v1/posts
    // 조회 성공은 RsData 로 감싸지 않고 데이터(글 배열)를 그대로 응답한다 (README 응답 규칙)
    @GetMapping
    @Transactional(readOnly = true) // 읽기만 하는 작업 → JPA가 변경 감지를 생략해서 조금 더 가볍다
    public List<PostDto> getItems() {
        return postService.findAllNewestFirst() // List<Post> (엔티티)
                .stream()
                .map(PostDto::new)              // Post 하나하나를 PostDto 로 바꾼다 (post -> new PostDto(post))
                .toList();                      // List<PostDto>
    }

    // [005 추가] 글 단건조회: GET /api/v1/posts/{id}
    // {id} : 주소의 이 자리에 오는 값을 @PathVariable 로 받는다 → /api/v1/posts/1 이면 id = 1
    // 성공: PostDto 를 그대로 응답 (200)
    // [006 수정] 실패 처리는 컨트롤러에서 하지 않는다
    //   .get() : Optional 이 비어 있으면 NoSuchElementException 이 저절로 던져진다
    //   → GlobalExceptionHandler 가 받아서 404 + RsData 로 응답 (강의 48강)
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public PostDto getItem(@PathVariable long id) {
        Post post = postService.findById(id).get();

        return new PostDto(post);
    }

    // [009 추가] 글 삭제: DELETE /api/v1/posts/{id} (강의 16강)
    // 성공: RsData("200-1", "1번 글이 삭제되었습니다.") / 없는 글: .get() → 404 (006)
    @DeleteMapping("/{id}")
    @Transactional
    public RsData<Void> deleteItem(@PathVariable long id) {
        Post post = postService.findById(id).get();

        postService.delete(post);

        return new RsData<>("200-1", "%d번 글이 삭제되었습니다.".formatted(id));
    }

    // [010 추가] 글 작성 요청 본문 (강의 24강: Form 보다 ReqBody 가 더 정확한 이름)
    // JSON {"title": "...", "content": "..."} 이 이 record 로 바뀐다 (Jackson 이 같은 이름의 칸에 넣어 준다)
    // 각 칸에 붙은 어노테이션이 입력값 규칙이다 (README 입력값 규칙)
    //   @NotBlank : null, "", "   " (공백만) 모두 거부
    //   @Size     : 글자 수 범위
    public record PostWriteReqBody(
            @NotBlank
            @Size(min = 2, max = 100)
            String title,

            @NotBlank
            @Size(min = 2, max = 5000)
            String content
    ) {
    }

    // [010 추가] 글 작성: POST /api/v1/posts (강의 22강)
    // @RequestBody : 요청 본문(JSON)을 PostWriteReqBody 로 바꿔서 받는다
    //                JSON 문법이 틀리면 바꾸지 못해서 HttpMessageNotReadableException → 400 (GlobalExceptionHandler)
    // @Valid       : 바꾼 뒤 위의 규칙을 검사한다
    //                어기면 이 메서드는 실행되지 않고 MethodArgumentNotValidException → 400 (GlobalExceptionHandler)
    // 그래서 메서드 안에는 "규칙을 통과한 정상 입력"만 들어온다
    // 응답: RsData 의 data 에 작성된 글을 담아서, 프론트가 새 글 번호 등을 바로 쓸 수 있게 한다
    @PostMapping
    @Transactional
    public RsData<PostDto> write(@RequestBody @Valid PostWriteReqBody reqBody) {
        Post post = postService.write(reqBody.title(), reqBody.content());

        return new RsData<>(
                "201-1",
                "%d번 글이 작성되었습니다.".formatted(post.getId()),
                new PostDto(post)
        );
    }
}
