package com.back.domain.post.post.entity;

import com.back.domain.post.postComment.entity.PostComment;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.PERSIST;
import static lombok.AccessLevel.PROTECTED;

// @Entity 가 붙으면 이 클래스를 보고 DB에 post 테이블이 만들어진다
// 칼럼: id, create_date, modify_date (BaseEntity 에서 상속) + title, content
@Entity
@Getter // getTitle(), getContent() 등을 자동 생성
@NoArgsConstructor(access = PROTECTED) // JPA가 DB에서 꺼낼 때 쓰는 기본 생성자. 밖에서 new Post() 는 못 하게 막음
public class Post extends BaseEntity {
    private String title;

    @Column(columnDefinition = "TEXT") // 긴 글도 들어가도록 TEXT 타입
    private String content;

    // [003 추가] 글 1개 : 댓글 N개 (One 글 To Many 댓글)
    // mappedBy = "post" : 외래키(post_id)는 내가 아니라 PostComment 의 post 필드가 관리한다는 뜻
    //                     → post 테이블에는 칼럼이 생기지 않는다
    // cascade = PERSIST : 글이 저장될 때(트랜잭션이 끝날 때), comments 에 새로 들어온 댓글도 같이 저장한다
    //                     → 댓글마다 save() 를 부르지 않아도 INSERT 된다
    @OneToMany(mappedBy = "post", cascade = PERSIST)
    private List<PostComment> comments = new ArrayList<>();

    // 글을 만들 때는 이 생성자를 쓴다 (id, 날짜는 저장할 때 자동으로 채워짐)
    public Post(String title, String content) {
        this.title = title;
        this.content = content;
    }

    // [003 추가] 댓글은 반드시 글을 통해서 만든다
    // 양쪽(글의 comments 목록, 댓글의 post 필드)을 한 번에 연결해서 빠뜨릴 일이 없게 한다
    public PostComment addComment(String content) {
        PostComment postComment = new PostComment(this, content); // 댓글 → 글 연결 (DB의 post_id 가 이걸로 정해진다)
        comments.add(postComment);                                 // 글 → 댓글 연결 (자바 객체끼리의 연결)
        return postComment;
    }
}
