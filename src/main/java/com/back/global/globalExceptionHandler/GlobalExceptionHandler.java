package com.back.global.globalExceptionHandler;

import com.back.global.rsData.RsData;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

// [005 추가] 모든 컨트롤러에서 던져진 예외를 한곳에서 받아 응답으로 바꾸는 클래스
// @RestControllerAdvice : 모든 @RestController 에 공통으로 적용되는 "조언자"
//                         여기서 돌려준 값도 JSON 으로 응답된다
// 덕분에 컨트롤러는 예외 처리 코드를 쓰지 않아도 된다
@RestControllerAdvice
public class GlobalExceptionHandler {
    // [006 수정] ServiceException → NoSuchElementException
    // 컨트롤러에서 Optional.get() 이 빈 값을 만나면 NoSuchElementException 이 던져지고, 이 메서드가 실행된다
    // 어떤 컨트롤러의 어떤 데이터가 없었는지는 모르므로 공통 메시지를 쓴다 (강의 48강)
    // ResponseEntity : 응답 본문(body)뿐 아니라 HTTP 상태코드까지 정해서 보낼 수 있는 상자 (강의 49강 정답코드 B)
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<RsData<Void>> handle(NoSuchElementException ex) {
        RsData<Void> rsData = new RsData<>("404-1", "해당 데이터가 존재하지 않습니다.");

        return ResponseEntity
                .status(rsData.statusCode()) // "404-1" → 404
                .body(rsData);               // {"resultCode":"404-1","msg":"해당 데이터가 존재하지 않습니다.","data":null}
    }
}
