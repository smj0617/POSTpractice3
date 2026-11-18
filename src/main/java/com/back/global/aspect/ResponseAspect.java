package com.back.global.aspect;

import com.back.global.rsData.RsData;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

// [011 추가] 컨트롤러 메서드가 RsData 를 돌려주면, resultCode 앞 숫자로 HTTP 상태코드를 정해 주는 Aspect (강의 28강, 37강)
//
// AOP(Aspect Oriented Programming, 관점 지향 프로그래밍)
//   여러 메서드에 공통으로 붙어야 하는 일(여기서는 "상태코드 맞추기")을 한곳에 모아 두고,
//   "어떤 메서드에 끼어들지"(포인트컷)만 정해 두면 스프링이 그 메서드들을 감싸서 실행해 준다.
//   → 컨트롤러마다 ResponseEntity.status(201)... 를 쓰지 않아도 된다.
//
// @Aspect    : 이 클래스는 "끼어들 일"을 담은 클래스
// @Component : 스프링이 객체로 만들어 관리 (그래야 Aspect 가 동작한다)
// AspectJ 라이브러리(aspectjweaver)는 spring-boot-starter-data-jpa 가 이미 가져와서 의존성을 따로 추가하지 않았다
@Aspect
@Component
@RequiredArgsConstructor
public class ResponseAspect {
    // 지금 처리 중인 요청의 응답 객체 (스프링이 요청마다 알맞은 것으로 연결해 준다)
    private final HttpServletResponse response;

    // @Around("포인트컷") : 조건에 맞는 메서드를 "감싸서" 실행한다
    // 포인트컷 읽는 법 (세 조건을 모두 만족하는 메서드):
    //   ① execution(public com.back.global.rsData.RsData *(..)) : RsData 를 돌려주는 public 메서드
    //   ② within(@...Controller *) || within(@...RestController *) : 컨트롤러 클래스 안에 있는
    //   ③ @annotation(...GetMapping) || ... : 요청을 받는 메서드(액션 메서드)
    // → 지금은 ApiV1PostController 의 write, deleteItem / ApiV1PostCommentController 의 deleteItem 이 해당된다
    //   (getItems, getItem 은 RsData 가 아니라 DTO 를 돌려주므로 ① 에서 빠진다)
    @Around("""
                execution(public com.back.global.rsData.RsData *(..)) &&
                (
                    within(@org.springframework.stereotype.Controller *) ||
                    within(@org.springframework.web.bind.annotation.RestController *)
                ) &&
                (
                    @annotation(org.springframework.web.bind.annotation.GetMapping) ||
                    @annotation(org.springframework.web.bind.annotation.PostMapping) ||
                    @annotation(org.springframework.web.bind.annotation.PutMapping) ||
                    @annotation(org.springframework.web.bind.annotation.DeleteMapping) ||
                    @annotation(org.springframework.web.bind.annotation.RequestMapping)
                )
            """)
    public Object handleResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        // ① 원래 컨트롤러 메서드 실행 (write() 등). 여기서 예외가 나면 아래 줄은 실행되지 않고 그대로 위로 던져진다
        Object proceed = joinPoint.proceed();

        // ② 결과가 RsData 이므로 resultCode 앞 숫자로 응답 상태코드를 바꾼다: "201-1" → 201
        RsData<?> rsData = (RsData<?>) proceed;
        response.setStatus(rsData.statusCode());

        // ③ 결과는 바꾸지 않고 그대로 돌려준다 → 스프링이 JSON 으로 바꿔 응답 본문에 넣는다
        return proceed;
    }
}
