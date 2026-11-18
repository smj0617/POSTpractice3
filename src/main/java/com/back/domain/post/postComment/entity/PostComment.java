package com.back.domain.post.postComment.entity;

import com.back.domain.post.post.entity.Post;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.FetchType.LAZY;
import static lombok.AccessLevel.PROTECTED;

// [003 추가] 댓글 엔티티 → DB에 post_comment 테이블이 만들어진다
// 칼럼: id, create_date, modify_date (BaseEntity 에서 상속) + post_id, content
@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class PostComment extends BaseEntity {
    // 댓글 N개 : 글 1개 (Many 댓글 To One 글)
    // 이 필드가 DB에서 post_id 외래키(FK) 칼럼이 된다 → "이 댓글은 몇 번 글의 댓글인가"
    // LAZY: 댓글을 조회할 때 글은 바로 가져오지 않고, 실제로 post 를 사용할 때 가져온다
    @ManyToOne(fetch = LAZY)
    private Post post;

    @Column(columnDefinition = "TEXT")
    private String content;

    // 직접 new PostComment(...) 하지 말고 post.addComment("내용") 을 사용한다
    // (Post 와 패키지가 달라서 어쩔 수 없이 public)
    public PostComment(Post post, String content) {
        this.post = post;
        this.content = content;
    }

    // [013 추가] 댓글 수정: 내용만 바꾼다 → 트랜잭션이 끝날 때 더티체킹으로 UPDATE (012 글 수정과 같은 원리)
    public void modify(String content) {
        this.content = content;
    }
}
