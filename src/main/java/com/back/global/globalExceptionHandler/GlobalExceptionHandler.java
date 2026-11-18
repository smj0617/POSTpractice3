package com.back.global.globalExceptionHandler;

import com.back.global.exception.ServiceException;
import com.back.global.rsData.RsData;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// [005 추가] 모든 컨트롤러에서 던져진 예외를 한곳에서 받아 응답으로 바꾸는 클래스
// @RestControllerAdvice : 모든 @RestController 에 공통으로 적용되는 "조언자"
//                         여기서 돌려준 값도 JSON 으로 응답된다
// 덕분에 컨트롤러마다 try-catch 를 쓰지 않고 throw 만 하면 된다
@RestControllerAdvice
public class GlobalExceptionHandler {
    // ServiceException 이 던져지면 이 메서드가 실행된다
    // ResponseEntity : 응답 본문(body)뿐 아니라 HTTP 상태코드까지 정해서 보낼 수 있는 상자
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<RsData<Void>> handle(ServiceException ex) {
        RsData<Void> rsData = ex.getRsData();

        return ResponseEntity
                .status(rsData.statusCode()) // "404-1" → 404
                .body(rsData);               // {"resultCode":"404-1","msg":"...","data":null}
    }
}
