package com.back.domain.post.postComment.controller;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import com.back.domain.post.postComment.dto.PostCommentDto;
import com.back.domain.post.postComment.entity.PostComment;
import com.back.global.rsData.RsData;                         // [008 추가]
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping; // [008 추가]
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// [007 추가] 댓글 API 컨트롤러 (강의 10강)
// 댓글은 항상 글에 속하므로 주소도 "글 아래의 댓글"로 만든다: /api/v1/posts/{postId}/comments
// 클래스 주소에도 {postId} 를 쓸 수 있고, 메서드에서 @PathVariable 로 꺼낸다
// 댓글 전용 서비스는 아직 만들지 않는다: 댓글은 글을 통해서(post.getComments()) 꺼내면 충분하다
@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
@RequiredArgsConstructor
public class ApiV1PostCommentController {
    private final PostService postService;

    // 댓글 다건조회: GET /api/v1/posts/{postId}/comments
    // 글이 없으면 .get() 에서 NoSuchElementException → GlobalExceptionHandler 가 404 (006)
    // 글은 있는데 댓글이 없으면 빈 배열 [] 로 응답 (실패가 아니다)
    @GetMapping
    @Transactional(readOnly = true) // post.getComments() 는 LAZY → 트랜잭션 안에서 꺼내야 DB에서 불러올 수 있다
    public List<PostCommentDto> getItems(@PathVariable long postId) {
        Post post = postService.findById(postId).get();

        return post.getComments() // SELECT ... FROM post_comment WHERE post_id = ? ORDER BY id ASC
                .stream()
                .map(PostCommentDto::new)
                .toList();
    }

    // 댓글 단건조회: GET /api/v1/posts/{postId}/comments/{id}
    // 글이 없거나, 그 글에 해당 번호의 댓글이 없으면 .get() 에서 예외 → 404
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public PostCommentDto getItem(@PathVariable long postId, @PathVariable long id) {
        Post post = postService.findById(postId).get();

        PostComment postComment = post.findCommentById(id).get(); // 이 글의 댓글 중에서만 찾는다

        return new PostCommentDto(postComment);
    }

    // [008 추가] 댓글 삭제: DELETE /api/v1/posts/{postId}/comments/{id} (강의 11~12강, 21강에서 DELETE 방식으로)
    // 삭제는 조회가 아니므로 성공도 RsData 로 감싼다. 지울 데이터는 이제 없으므로 data 는 비운다 → RsData<Void>
    // @Transactional : 메서드가 끝날 때 JPA가 바뀐 것(댓글 목록에서 빠진 댓글)을 찾아 DB에 반영한다 (더티체킹)
    @DeleteMapping("/{id}")
    @Transactional
    public RsData<Void> deleteItem(@PathVariable long postId, @PathVariable long id) {
        Post post = postService.findById(postId).get();                // 글이 없으면 404
        PostComment postComment = post.findCommentById(id).get();      // 그 글의 댓글이 아니면 404

        post.deleteComment(postComment); // 목록에서 빼기만 한다 → DELETE SQL 은 JPA 가 만든다

        return new RsData<>("200-1", "%d번 댓글이 삭제되었습니다.".formatted(id));
    }
}
