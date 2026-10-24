package com.myproject.stocksimulator.kiwoom;

public class KiwoomApiException extends RuntimeException {

    // 키움이 반환한 HTTP 상태
    // 통신 실패나 응답 형식 오류는 0으로 표현한다.
    private final int upstreamStatus;

    public KiwoomApiException(int upstreamStatus, String message) {
        super(message);
        this.upstreamStatus = upstreamStatus;
    }

    public int upstreamStatus() {
        return upstreamStatus;
    }

    public static KiwoomApiException invalidResponse() {
        return new KiwoomApiException(
                0,
                "키움 응답을 정상적으로 처리할 수 없습니다."
        );
    }
}