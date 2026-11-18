# POSTpractice3

글·댓글 게시판 REST API의 **CRUD(다건조회·단건조회·작성·수정·삭제)** 를 TDD로 만드는 실습입니다.
**기능 하나 = 커밋 하나**로 나누고, 커밋마다 `001`, `002` … 태그를 달아 나중에 그 시점의 코드를 바로 꺼내 볼 수 있게 했습니다.

- 참고: [slog.gg/p/14172](https://www.slog.gg/p/14172/raw) 2부(33강 ~ 56강) REST API by TDD
- 기술: Java 25, Spring Boot 4.1.2, Spring Data JPA, H2, Lombok, JUnit 5 + MockMvc
- 용어: [GLOSSARY.md](GLOSSARY.md)
- 테스트 실행: `./gradlew test` (리포트: `build/reports/tests/test/index.html`)
- 캡처 도구: ego lite 브라우저

## 코드 확인하는 법

| 보고 싶은 것 | 방법 |
|---|---|
| 지금(최신) 코드 | 이 폴더를 IntelliJ로 열기 |
| 특정 커밋에서 바뀐 내용 | `git show 003` |
| 특정 커밋 시점의 코드 전체 | `git checkout 003` → 다 보면 `git checkout main` |
| 커밋 목록 | `git log --oneline --decorate` |

## 진행 상황

| 태그 | 기능 | 상태 |
|---|---|---|
| `001` | 빈 Spring Boot 프로젝트 + 용어집 + README 뼈대 | ✅ 완료 |
| `002` | JPA·H2 설정 + `Post` 엔티티 + 샘플 글 3개 | ✅ 완료 |
| `003` | `PostComment` 엔티티 + 샘플 댓글 5개 | ✅ 완료 |
| `004` | 글 다건조회 | ⏳ |
| `005` | 글 단건조회 | ⏳ |
| `006` | 글 작성 (입력값 검증 포함) | ⏳ |
| `007` | 글 수정 (입력값 검증 포함) | ⏳ |
| `008` | 글 삭제 (댓글도 함께 삭제) | ⏳ |
| `009` | 댓글 다건조회 | ⏳ |
| `010` | 댓글 단건조회 | ⏳ |
| `011` | 댓글 작성 (입력값 검증 포함) | ⏳ |
| `012` | 댓글 수정 (입력값 검증 포함) | ⏳ |
| `013` | 댓글 삭제 + 최종 결과 정리 | ⏳ |

## 만들 API

| 메서드 | URL | 기능 | 성공 응답 | 태그 |
|---|---|---|---|---|
| GET | `/api/v1/posts` | 글 다건조회 (최신순) | 200, 글 배열 | `004` |
| GET | `/api/v1/posts/{id}` | 글 단건조회 | 200, 글 | `005` |
| POST | `/api/v1/posts` | 글 작성 | 201, `RsData` | `006` |
| PUT | `/api/v1/posts/{id}` | 글 수정 | 200, `RsData` | `007` |
| DELETE | `/api/v1/posts/{id}` | 글 삭제 | 200, `RsData` | `008` |
| GET | `/api/v1/posts/{postId}/comments` | 댓글 다건조회 (작성순) | 200, 댓글 배열 | `009` |
| GET | `/api/v1/posts/{postId}/comments/{id}` | 댓글 단건조회 | 200, 댓글 | `010` |
| POST | `/api/v1/posts/{postId}/comments` | 댓글 작성 | 201, `RsData` | `011` |
| PUT | `/api/v1/posts/{postId}/comments/{id}` | 댓글 수정 | 200, `RsData` | `012` |
| DELETE | `/api/v1/posts/{postId}/comments/{id}` | 댓글 삭제 | 200, `RsData` | `013` |

### 응답 규칙
- **조회 성공**: 데이터(DTO)를 그대로 응답한다.
- **조회 실패, 작성·수정·삭제**: `RsData` 로 감싼다. → `{"resultCode": "200-1", "msg": "1번 글이 수정되었습니다.", "data": null}`
- `resultCode` 는 `HTTP상태코드-세부번호` 형식이다.

| 상황 | HTTP | resultCode | msg 예시 |
|---|---|---|---|
| 작성 성공 | 201 | `201-1` | `4번 글이 작성되었습니다.` |
| 수정 성공 | 200 | `200-1` | `1번 글이 수정되었습니다.` |
| 삭제 성공 | 200 | `200-1` | `1번 글이 삭제되었습니다.` |
| 입력값 오류 | 400 | `400-1` | `title-Size-size must be between 2 and 100` |
| 없는 대상 | 404 | `404-1` | `1000번 글은 존재하지 않습니다.` |

### 입력값 규칙
| 항목 | 규칙 |
|---|---|
| 글 제목 | 공백 불가, 2 ~ 100자 |
| 글 내용 | 공백 불가, 2 ~ 5000자 |
| 댓글 내용 | 공백 불가, 2 ~ 1000자 |

---

## 001 - 빈 Spring Boot 프로젝트 + 용어집 + README 뼈대

### 작업 내용

| 파일 | 역할 |
|---|---|
| `build.gradle.kts` | 사용할 라이브러리 목록. 지금은 웹(`webmvc`), Lombok, DevTools, 테스트 라이브러리만 넣었다. DB(JPA·H2)는 002에서 추가한다. |
| `settings.gradle.kts` | 프로젝트 이름 `POSTpractice3` |
| `gradlew`, `gradlew.bat`, `gradle/wrapper/` | Gradle을 따로 설치하지 않아도 빌드할 수 있게 해 주는 래퍼 |
| `.gitignore`, `.gitattributes` | git에 올리지 않을 파일(빌드 결과, DB 파일 등)과 줄바꿈 규칙 |
| `src/main/resources/application.yaml` | 앱 설정. 지금은 앱 이름만 |
| `src/main/java/com/back/BackApplication.java` | 앱의 시작점(`main` 메서드) |
| `src/test/java/com/back/BackApplicationTests.java` | 앱이 에러 없이 뜨는지 확인하는 테스트 |
| `GLOSSARY.md` | 이 프로젝트에서 쓰는 말(글, 댓글, 다건조회, 단건조회, 샘플 데이터)의 뜻 |
| `README.md` | 전체 계획(API 목록, 응답·입력값 규칙)과 커밋별 기록 |

### 핵심 포인트
- `@SpringBootApplication` 이 붙은 클래스의 패키지(`com.back`) **아래에** 있는 클래스만 스프링이 찾아낸다. 그래서 앞으로 만들 모든 클래스는 `com.back.…` 안에 둔다.
- `@SpringBootTest` 는 테스트할 때 스프링을 실제 실행처럼 통째로 띄운다. 테스트 메서드가 비어 있어도, 띄우는 도중에 에러가 나면 실패한다.
- 이 단계는 아직 "기능"이 없어서 RED(실패) 단계가 없다. 앞으로 만들 모든 테스트의 출발점인 "앱이 뜬다"를 먼저 확인해 둔다.

### 테스트

| 테스트 | 검증 내용 |
|---|---|
| `BackApplicationTests.contextLoads` | 앱(스프링)이 에러 없이 실행된다 |

### 결과 스크린샷

전체 결과 — 1건 실행, 실패 0, 성공률 100%

![001 테스트 요약](docs/images/001-test-summary.png)

`BackApplicationTests` 상세

![001 테스트 상세](docs/images/001-test-detail.png)

---

## 002 - JPA·H2 설정 + `Post` 엔티티 + 샘플 글 3개

### 작업 내용

| 파일 | 변경 | 역할 |
|---|---|---|
| `build.gradle.kts` | 수정 | JPA(`data-jpa`), H2 DB(`h2`), H2 웹 콘솔(`h2console`) 3줄 추가 |
| `src/main/resources/application.yaml` | 수정 | DB 접속 정보(`db_dev` 파일), H2 콘솔 켜기, 테이블 자동 생성(`ddl-auto: update`), SQL 출력 |
| `src/test/resources/application-test.yaml` | 추가 | 테스트 전용 설정. 파일 DB 대신 **메모리 DB** 사용, 테스트마다 테이블을 새로 만듦 |
| `BackApplication.java` | 수정 | `@EnableJpaAuditing` 추가 → 작성일·수정일 자동 기록 켜기 |
| `global/jpa/entity/BaseEntity.java` | 추가 | 모든 엔티티의 공통 칸: `id`, `createDate`, `modifyDate` |
| `domain/post/post/entity/Post.java` | 추가 | 글 엔티티 → DB의 `post` 테이블 (`title`, `content`) |
| `domain/post/post/repository/PostRepository.java` | 추가 | DB 접근 담당. 인터페이스만 선언하면 `save`, `count`, `findAll` 등이 생긴다 |
| `domain/post/post/service/PostService.java` | 추가 | 글 기능 담당: `count()`, `write()`, `findAll()` |
| `global/initData/BaseInitData.java` | 추가 | 앱이 시작되면 샘플 글 3개를 만든다 (이미 있으면 안 만든다) |
| `BackApplicationTests.java` | 수정 | `@ActiveProfiles("test")` 추가 → 메모리 DB 사용 |
| `global/initData/BaseInitDataTest.java` | 추가 | 샘플 글 3개 검증 테스트 3건 |

### 봐야 할 부분 (읽는 순서)

1. **[`BaseInitDataTest.java`](src/test/java/com/back/global/initData/BaseInitDataTest.java)** — 테스트를 **먼저** 읽는다. "앱이 시작되면 글이 3개 있어야 한다"는 기대가 코드로 적혀 있다. 테스트 안에서 글을 직접 만들지 않는다는 점이 핵심이다.
2. **[`BaseEntity.java`](src/main/java/com/back/global/jpa/entity/BaseEntity.java)** → **[`Post.java`](src/main/java/com/back/domain/post/post/entity/Post.java)** — 자바 클래스가 어떻게 DB 테이블이 되는지. `Post` 에는 `id` 가 안 보이는데 `BaseEntity` 를 상속해서 갖게 된다.
3. **[`PostRepository.java`](src/main/java/com/back/domain/post/post/repository/PostRepository.java)** → **[`PostService.java`](src/main/java/com/back/domain/post/post/service/PostService.java)** — 몸통 없는 인터페이스 하나로 `save()`, `count()` 를 쓸 수 있는 이유.
4. **[`BaseInitData.java`](src/main/java/com/back/global/initData/BaseInitData.java)** — RED를 GREEN으로 바꾼 **유일한 코드**. 이 파일만 없으면 테스트가 실패한다.
5. **[`application-test.yaml`](src/test/resources/application-test.yaml)** — 테스트가 개발용 DB 파일을 건드리지 않는 이유.

### 핵심 포인트

- **엔티티 = 테이블**: `@Entity` 를 붙이면 JPA가 클래스를 보고 `CREATE TABLE post (id, create_date, modify_date, title, content)` 를 대신 실행한다(`ddl-auto`). SQL을 직접 쓰지 않는다.
- **`@MappedSuperclass`**: `BaseEntity` 는 테이블이 되지 않고, 자기 칸(`id`, 날짜 2개)을 자식 엔티티 테이블에 물려준다. 003의 댓글도 이걸 상속한다.
- **`@NoArgsConstructor(access = PROTECTED)`**: JPA는 DB에서 꺼낸 값을 담으려고 빈 생성자가 필요하다. 하지만 우리가 `new Post()` 로 제목 없는 글을 만들면 안 되므로 `protected` 로 막고, 밖에서는 `new Post(title, content)` 만 쓰게 한다.
- **`JpaRepository<Post, Long>`**: "Post를 다루고 id는 Long" 이라고만 알려 주면 스프링이 구현 클래스를 자동으로 만든다.
- **계층**: `BaseInitData` → `PostService` → `PostRepository` → DB. 004부터는 맨 앞에 컨트롤러가 붙는다.
- **`ApplicationRunner`**: 앱이 다 뜬 직후 1번 실행된다. 테스트에서 `@SpringBootTest` 로 스프링을 띄울 때도 실행되기 때문에, 테스트가 글을 직접 만들지 않아도 3개가 있다.
- **`if (count() > 0) return;`**: 개발용 DB(`db_dev.mv.db`)는 파일이라 앱을 껐다 켜도 남는다. 이 줄이 없으면 재시작할 때마다 글이 3개씩 늘어난다.
- **`@ActiveProfiles("test")` + `application-test.yaml`**: 테스트는 메모리 DB(`jdbc:h2:mem:db_test`)를 쓴다. 그래서 테스트가 아무리 글을 지우고 바꿔도 개발용 DB는 그대로다.
- **`@Transactional` (테스트 클래스)**: 테스트 메서드가 끝나면 그 안에서 바꾼 DB 내용을 되돌린다(롤백). 테스트끼리 서로 영향을 주지 않는다.

### TDD 과정

**RED** — `BaseInitDataTest` 를 먼저 쓰고 실행. 샘플 데이터를 만드는 코드가 없어서 3건 모두 실패.
`t1` 실패 메시지: `expected: 3L but was: 0L` → "글이 3개여야 하는데 0개다"

![002 RED](docs/images/002-red.png)

![002 RED 상세](docs/images/002-red-detail.png)

**GREEN** — `BaseInitData` 추가 후 다시 실행. 3건 모두 통과.

![002 GREEN](docs/images/002-green.png)

### 테스트

| 테스트 | 검증 내용 |
|---|---|
| `BaseInitDataTest.t1` | 앱이 시작되면 샘플 글 3개가 자동으로 만들어져 있다 |
| `BaseInitDataTest.t2` | 샘플 글의 번호는 1, 2, 3 이고 제목·내용은 '제목 n', '내용 n' 이다 |
| `BaseInitDataTest.t3` | 샘플 글에는 작성일과 수정일이 자동으로 채워져 있다 |

### 결과 스크린샷

전체 결과 — 4건 실행, 실패 0, 성공률 100%

![002 테스트 요약](docs/images/002-green-summary.png)

---

## 003 - `PostComment` 엔티티 + 샘플 댓글 5개

### 작업 내용

| 파일 | 변경 | 역할 |
|---|---|---|
| `domain/post/postComment/entity/PostComment.java` | 추가 | 댓글 엔티티 → DB의 `post_comment` 테이블 (`post_id`, `content`) |
| `domain/post/post/entity/Post.java` | 수정 | `comments` 목록(`@OneToMany`) + 댓글을 만드는 `addComment()` 추가 |
| `global/initData/BaseInitData.java` | 수정 | 샘플 댓글 5개 생성, `@Transactional` + `self` 호출로 변경 |
| `global/initData/BaseInitDataTest.java` | 수정 | 샘플 댓글 검증 테스트 3건(`t4` ~ `t6`) 추가 |

### 봐야 할 부분 (읽는 순서)

1. **[`BaseInitDataTest.java`](src/test/java/com/back/global/initData/BaseInitDataTest.java) `t4` ~ `t6`** — 댓글이 "몇 개", "어느 글에", "몇 번으로" 저장되어야 하는지.
2. **[`PostComment.java`](src/main/java/com/back/domain/post/postComment/entity/PostComment.java)** 의 `@ManyToOne private Post post;` — DB의 `post_id` 외래키가 되는 필드.
3. **[`Post.java`](src/main/java/com/back/domain/post/post/entity/Post.java)** 의 `@OneToMany(mappedBy = "post", cascade = PERSIST)` 와 `addComment()` — 글과 댓글을 양쪽에서 이어 주는 방법.
4. **[`BaseInitData.java`](src/main/java/com/back/global/initData/BaseInitData.java)** 의 `@Transactional` 과 `self.work1()` — 아래 "함정"에서 왜 필요한지 확인.

### 핵심 포인트

- **관계의 주인은 댓글**: DB에서 "이 댓글은 몇 번 글 것인가"는 `post_comment.post_id` 칸에 적힌다. 그 칸을 만드는 건 `PostComment.post` 필드(`@ManyToOne`)다. `Post.comments` 는 `mappedBy = "post"` 라서 칸을 만들지 않고, 자바에서 "이 글의 댓글 목록"을 편하게 보려고 둔 거울이다.
- **`addComment()` 로만 댓글을 만든다**: 한쪽만 연결하면(예: `post_id` 만 넣고 `comments` 목록엔 안 넣으면) 같은 트랜잭션 안에서 `post.getComments()` 가 비어 보인다. 양쪽을 한 메서드에서 같이 연결해 실수를 막는다.
- **`cascade = PERSIST`**: 글이 저장될 때 `comments` 에 새로 들어온 댓글도 같이 저장한다. 그래서 댓글은 `save()` 를 따로 부르지 않는다.
- **`fetch = LAZY`**: 댓글을 꺼낼 때 글은 바로 가져오지 않고, `comment.getPost()` 로 실제로 쓸 때 가져온다(필요할 때만 SQL 실행).

### 함정: `@Transactional` 이 없으면 댓글이 저장되지 않는다

처음에는 `work1()` 에 `addComment()` 다섯 줄만 추가했다. 그런데 테스트는 그대로 실패했다(`expected: 5 but was: 0`).

![003 함정](docs/images/003-trap.png)

| | 트랜잭션 없음 (실패) | 트랜잭션 있음 (성공) |
|---|---|---|
| `postService.write()` | `save()` 한 번 = 작은 트랜잭션 하나가 열리고 바로 닫힌다 | `work1()` 전체가 트랜잭션 하나 |
| `post1` 상태 | 트랜잭션이 끝나 JPA가 **더 이상 지켜보지 않는** 객체 | 트랜잭션 안이라 JPA가 **계속 지켜보는** 객체 |
| `addComment()` | 자바 리스트에 넣기만 하고 끝 → DB 변화 없음 | 트랜잭션이 끝날 때 JPA가 바뀐 것을 발견 → cascade로 `INSERT` 5번 |

- **왜 `self.work1()` 인가**: `@Transactional` 은 스프링이 만든 대리 객체(프록시)를 거쳐 호출해야 동작한다. 같은 클래스 안에서 `work1()`(= `this.work1()`)로 부르면 프록시를 안 거쳐서 어노테이션이 무시된다. 그래서 자기 자신을 프록시로 주입받아(`self`) 그걸로 부른다.
- **`@Lazy`**: 자기 자신을 주입받으면 "나를 만들려면 내가 필요한" 순환이 생긴다. `@Lazy` 로 실제로 쓸 때 주입받아 이 문제를 피한다.

### TDD 과정

**RED** — 댓글 테스트 3건을 먼저 쓰고 실행. 샘플 댓글이 없어서 3건 실패 (`expected: 5 but was: 0`).

![003 RED](docs/images/003-red.png)

![003 RED 상세](docs/images/003-red-detail.png)

**GREEN** — `@Transactional` + `self.work1()` 로 고친 뒤 실행. 모두 통과.

![003 GREEN](docs/images/003-green.png)

### 테스트

| 테스트 | 검증 내용 |
|---|---|
| `BaseInitDataTest.t4` | 앱이 시작되면 샘플 댓글 5개가 DB에 저장되어 있다 |
| `BaseInitDataTest.t5` | 1번 글에 댓글 3개, 2번 글에 댓글 2개, 3번 글에는 댓글이 없다 |
| `BaseInitDataTest.t6` | 샘플 댓글의 번호는 1 ~ 5 이고, 각 댓글은 자기가 속한 글을 알고 있다 |

### 결과 스크린샷

전체 결과 — 7건 실행, 실패 0, 성공률 100%

![003 테스트 요약](docs/images/003-green-summary.png)
