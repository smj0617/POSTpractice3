package com.back.domain.post.post.repository;

import com.back.domain.post.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

// DB에 직접 접근하는 담당
// 인터페이스만 만들면 스프링이 구현체를 자동으로 만들어 준다
// JpaRepository<Post, Long> : Post 엔티티를 다루고, id 타입은 Long
// → save(), findById(), findAll(), count(), delete() 등을 코드 없이 바로 사용 가능
public interface PostRepository extends JpaRepository<Post, Long> {
}
