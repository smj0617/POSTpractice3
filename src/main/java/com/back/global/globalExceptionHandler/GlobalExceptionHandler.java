package com.back.global.globalExceptionHandler;

import com.back.global.rsData.RsData;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;
import java.util.stream.Collectors;

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

    // [010 추가] @Valid 검사에서 규칙을 어긴 입력이 있을 때 (강의 50강)
    // 어긴 규칙마다 "칸이름-규칙이름-기본메시지" 한 줄을 만들고, 정렬해서 줄바꿈으로 잇는다
    // 예) title-NotBlank-must not be blank
    //     title-Size-size must be between 2 and 100
    // 정렬하는 이유: 검사 순서는 매번 같다는 보장이 없어서, 정렬해야 메시지가 항상 같아진다(테스트 가능)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RsData<Void>> handle(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult()
                .getAllErrors()                                   // 어긴 규칙 목록
                .stream()
                .filter(error -> error instanceof FieldError)     // 칸(필드) 단위 오류만
                .map(error -> (FieldError) error)
                .map(error -> error.getField() + "-" + error.getCode() + "-" + error.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining("\n"));

        RsData<Void> rsData = new RsData<>("400-1", msg);

        return ResponseEntity
                .status(rsData.statusCode()) // 400
                .body(rsData);
    }

    // [010 추가] 요청 본문을 읽을 수 없을 때: JSON 문법 오류, 본문 없음 등 (강의 52강)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RsData<Void>> handle(HttpMessageNotReadableException ex) {
        RsData<Void> rsData = new RsData<>("400-1", "요청 본문이 올바르지 않습니다.");

        return ResponseEntity
                .status(rsData.statusCode()) // 400
                .body(rsData);
    }
}
