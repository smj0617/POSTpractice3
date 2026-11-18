package com.back.domain.post.post.entity;

import com.back.domain.post.postComment.entity.PostComment;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy; // [007 추가]
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;          // [007 추가]

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
    // [007 추가] @OrderBy("id ASC") : DB에서 댓글을 불러올 때 항상 번호 순서(먼저 단 댓글이 먼저)로 가져온다
    //                                → SELECT ... FROM post_comment WHERE post_id = ? ORDER BY id ASC
    //                                (없으면 DB가 주는 순서가 보장되지 않는다)
    // [008 추가] orphanRemoval = true : comments 목록에서 빠진 댓글(고아)은 DB에서도 삭제한다 (강의 12강)
    //                                  → comments.remove(댓글) 만 해도 트랜잭션이 끝날 때 DELETE 가 실행된다
    //                                  (없으면 목록에서만 빠지고 DB에는 그대로 남는다)
    @OneToMany(mappedBy = "post", cascade = PERSIST, orphanRemoval = true)
    @OrderBy("id ASC")
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

    // [007 추가] 이 글의 댓글 중에서 id 로 1개 찾기 (없으면 빈 Optional)
    // 이 글의 댓글 목록 안에서만 찾으므로, 다른 글의 댓글 번호를 넣으면 못 찾는다
    // → "댓글은 그 글의 댓글로만 찾는다" (GLOSSARY) 규칙이 여기서 지켜진다
    public Optional<PostComment> findCommentById(long id) {
        return comments.stream()
                .filter(comment -> comment.getId() == id)
                .findFirst();
    }

    // [012 추가] 글 수정: 필드 값만 바꾼다
    // save() 를 부르지 않는다 → 트랜잭션이 끝날 때 JPA가 "처음 읽었을 때와 달라진 칸"을 찾아 UPDATE 한다 (더티체킹)
    // modifyDate 는 BaseEntity 의 @LastModifiedDate 가 UPDATE 직전에 자동으로 갱신한다
    public void modify(String title, String content) {
        this.title = title;
        this.content = content;
    }

    // [008 추가] 댓글 삭제: 이 글의 댓글 목록에서 빼기만 한다
    // repository.delete() 를 부르지 않는다 → DB의 DELETE 는 JPA 가 알아서 하게 한다 (orphanRemoval)
    public void deleteComment(PostComment postComment) {
        comments.remove(postComment);
    }
}
