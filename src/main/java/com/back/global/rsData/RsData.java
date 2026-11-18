package com.back.global.rsData;

import com.fasterxml.jackson.annotation.JsonIgnore;

// [005 추가] 결과 응답 상자 (RsData = Result Data)
// 조회 성공을 뺀 모든 응답(조회 실패, 작성·수정·삭제)은 이 모양으로 보낸다
// {
//   "resultCode": "404-1",                    ← "HTTP상태코드-세부번호"
//   "msg": "1000번 글은 존재하지 않습니다.",   ← 사람이 읽을 메시지
//   "data": null                              ← 함께 보낼 데이터 (없으면 null)
// }
// <T> : data 에 들어갈 타입을 쓰는 쪽에서 정한다 (RsData<PostDto>, RsData<Void> ...)
public record RsData<T>(
        String resultCode,
        String msg,
        T data
) {
    // data 가 없는 응답용 생성자 → new RsData<>("404-1", "메시지")
    public RsData(String resultCode, String msg) {
        this(resultCode, msg, null);
    }

    // "404-1" → 404 : resultCode 의 앞부분을 HTTP 상태코드로 쓴다
    // @JsonIgnore : record 의 메서드라도 JSON 에는 넣지 않는다 (응답에 statusCode 칸이 생기지 않게)
    @JsonIgnore
    public int statusCode() {
        return Integer.parseInt(resultCode.split("-")[0]);
    }
}
