package com.back.domain.post.postComment.dto;

import com.back.domain.post.postComment.entity.PostComment;

import java.time.LocalDateTime;

// [007 추가] 응답으로 내보낼 댓글 데이터
// 엔티티(PostComment)를 그대로 내보내면 comment.post → post.comments → comment.post → ... 순환참조가 생긴다
// 그래서 글 객체 전체(post) 대신 글 번호(postId)만 담는다
public record PostCommentDto(
        Long id,
        LocalDateTime createDate,
        LocalDateTime modifyDate,
        Long postId,
        String content
) {
    // PostComment 엔티티 → PostCommentDto
    public PostCommentDto(PostComment postComment) {
        this(
                postComment.getId(),
                postComment.getCreateDate(),
                postComment.getModifyDate(),
                postComment.getPost().getId(), // 댓글 → 글 → 글 번호
                postComment.getContent()
        );
    }
}
