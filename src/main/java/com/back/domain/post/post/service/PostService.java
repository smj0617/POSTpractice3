package com.back.domain.post.post.service;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional; // [005 추가]

// 비즈니스 로직 담당 ("글을 쓴다", "글이 몇 개인지 센다" 같은 기능 단위)
// 컨트롤러(004부터 추가)는 서비스를 부르고, 서비스는 리포지터리를 부른다
@Service // 스프링이 이 클래스의 객체를 1개 만들어 관리한다(빈 등록)
@RequiredArgsConstructor // final 필드를 받는 생성자를 자동 생성 → 스프링이 PostRepository 를 넣어 준다(의존성 주입)
public class PostService {
    private final PostRepository postRepository;

    // 전체 글 개수
    public long count() {
        return postRepository.count(); // SELECT count(*) FROM post
    }

    // 글 작성: 객체를 만들고 → DB에 저장
    public Post write(String title, String content) {
        Post post = new Post(title, content); // 이 시점에는 id 가 null
        return postRepository.save(post);     // INSERT 실행 → id 가 채워진 Post 가 돌아온다
    }

    // 전체 글 목록 (id 오름차순)
    public List<Post> findAll() {
        return postRepository.findAll(); // SELECT * FROM post
    }

    // [004 추가] 전체 글 목록, 최신 글이 먼저 (글 다건조회 API 용)
    public List<Post> findAllNewestFirst() {
        return postRepository.findAllByOrderByIdDesc(); // SELECT * FROM post ORDER BY id DESC
    }

    // [005 추가] id 로 글 1개 찾기. 없을 수도 있으므로 Optional(값이 있을 수도, 없을 수도 있는 상자)로 돌려준다
    public Optional<Post> findById(long id) {
        return postRepository.findById(id); // SELECT * FROM post WHERE id = ?
    }
}
