package com.back.global.jpa.entity;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

// 모든 엔티티(테이블)가 공통으로 갖는 칸: id, 생성일, 수정일
// 글(Post)과 댓글(PostComment, 003에서 추가)에 똑같이 쓰지 않도록 부모 클래스로 뽑아 둔다
@MappedSuperclass // 이 클래스 자체는 테이블이 아니다. 상속받는 엔티티의 칼럼으로 들어간다
@EntityListeners(AuditingEntityListener.class) // 저장/수정될 때 날짜를 자동으로 넣어 주는 리스너
@Getter
public abstract class BaseEntity {
    @Id // 기본키(PK)
    @GeneratedValue(strategy = IDENTITY) // DB가 1, 2, 3 ... 순서로 번호를 매긴다 (auto_increment)
    private Long id;

    @CreatedDate // 처음 저장될 때 시각
    private LocalDateTime createDate;

    @LastModifiedDate // 마지막으로 수정된 시각 (007 글 수정에서 바뀌는 것을 확인한다)
    private LocalDateTime modifyDate;
}
