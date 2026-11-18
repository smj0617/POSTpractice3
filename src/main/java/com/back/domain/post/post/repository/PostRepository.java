package com.back.domain.post.post.repository;

import com.back.domain.post.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// DB에 직접 접근하는 담당
// 인터페이스만 만들면 스프링이 구현체를 자동으로 만들어 준다
// JpaRepository<Post, Long> : Post 엔티티를 다루고, id 타입은 Long
// → save(), findById(), findAll(), count(), delete() 등을 코드 없이 바로 사용 가능
public interface PostRepository extends JpaRepository<Post, Long> {
    // [004 추가] 메서드 이름만으로 쿼리가 만들어진다 (쿼리 메서드)
    // findAll + By + OrderBy + Id + Desc
    // → SELECT * FROM post ORDER BY id DESC (번호 큰 글 = 최신 글이 먼저)
    List<Post> findAllByOrderByIdDesc();
}
