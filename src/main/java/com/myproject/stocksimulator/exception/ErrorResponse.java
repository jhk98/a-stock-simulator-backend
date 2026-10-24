package com.myproject.stocksimulator.exception;

/**
 * API 공통 오류 응답 DTO
 *
 * <p>Controller 또는 Service에서 예외가 발생했을 때
 * 클라이언트(React, Postman 등)에 전달할 오류 정보를 정의한다.</p>
 *
 * <p>모든 API 오류 응답을 동일한 JSON 구조로 통일하기 위해 사용한다.</p>
 *
 * <p>보안상 서버 내부의 Stack Trace는 포함하지 않는다.
 * 상세한 예외 정보는 서버 로그에만 기록한다.</p>
 *
 * @param status HTTP 상태 코드 (400, 401, 403, 404, 500 등)
 * @param code   애플리케이션에서 사용하는 오류 식별 코드
 * @param message 클라이언트에 전달할 오류 설명
 */
public record ErrorResponse(
        int status,
        String code,
        String message
) {
}