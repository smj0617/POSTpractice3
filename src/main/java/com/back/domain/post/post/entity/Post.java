package com.back.domain.post.post.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    // 글을 만들 때는 이 생성자를 쓴다 (id, 날짜는 저장할 때 자동으로 채워짐)
    public Post(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
