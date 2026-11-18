package com.back.domain.post.post.dto;

import com.back.domain.post.post.entity.Post;

import java.time.LocalDateTime;

// [004 추가] 응답으로 내보낼 글 데이터 (DTO = Data Transfer Object, 데이터를 옮기는 상자)
// 엔티티(Post)를 그대로 응답하지 않고, 보여 줄 필드만 골라 담는다
//   - 엔티티를 그대로 내보내면 post.comments → comment.post → post.comments → ... 로
//     끝없이 이어지는 문제(순환참조)가 생긴다
//   - 엔티티에 필드를 추가하면 API 응답 모양까지 의도치 않게 바뀐다
// record: 필드 + 생성자 + 값 꺼내는 메서드(id(), title() ...) 를 자동으로 만들어 주는 간단한 클래스
//         Jackson(JSON 변환기)은 record 의 필드를 그대로 JSON 키로 쓴다
public record PostDto(
        Long id,
        LocalDateTime createDate,
        LocalDateTime modifyDate,
        String title,
        String content
) {
    // Post 엔티티 → PostDto 로 바꾸는 생성자
    public PostDto(Post post) {
        this(
                post.getId(),
                post.getCreateDate(),
                post.getModifyDate(),
                post.getTitle(),
                post.getContent()
        );
    }
}
