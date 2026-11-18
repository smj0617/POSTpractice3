package com.back.global.exception;

import com.back.global.rsData.RsData;

// [005 추가] "요청은 이해했지만 처리할 수 없다"를 알리는 예외
// 예) throw new ServiceException("404-1", "1000번 글은 존재하지 않습니다.");
// 이 예외를 던지면 컨트롤러는 거기서 멈추고, GlobalExceptionHandler 가 받아서 RsData 응답으로 바꾼다
// RuntimeException 을 상속 → 메서드에 throws 를 적지 않아도 던질 수 있다
public class ServiceException extends RuntimeException {
    private final String resultCode;
    private final String msg;

    public ServiceException(String resultCode, String msg) {
        super(resultCode + " : " + msg); // 로그·테스트 리포트에 보이는 예외 메시지
        this.resultCode = resultCode;
        this.msg = msg;
    }

    // 예외 → 응답 상자
    public RsData<Void> getRsData() {
        return new RsData<>(resultCode, msg);
    }
}
