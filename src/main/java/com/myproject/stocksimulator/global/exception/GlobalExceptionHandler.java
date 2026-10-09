package com.myproject.stocksimulator.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

/**
 * 애플리케이션 전역 예외 처리 클래스
 *
 * <p>Controller에서 처리되지 않은 예외를 공통으로 처리하고,
 * ErrorResponse 형태의 JSON 응답을 반환한다.</p>
 *
 * <p>각 Controller에서 반복적으로 try-catch를 작성하지 않도록
 * 예외 처리 책임을 한곳에 모은다.</p>
 *
 * <p>Spring Security 인증 필터에서 발생하는 인증 및 인가 예외는
 * 별도의 AuthenticationEntryPoint와 AccessDeniedHandler에서
 * 처리하도록 구성할 예정이다.</p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 조회한 데이터가 존재하지 않을 때 발생하는 예외 처리
     *
     * <p>예: 존재하지 않는 회원 ID로 회원 정보를 조회한 경우</p>
     *
     * HTTP 404 Not Found
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNoSuchElementException(
            NoSuchElementException exception
    ) {
        ErrorResponse response = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "RESOURCE_NOT_FOUND",
                exception.getMessage() != null
                        ? exception.getMessage()
                        : "요청한 데이터를 찾을 수 없습니다."
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    /**
     * 요청 데이터의 유효성 검증에 실패했을 때 발생하는 예외 처리
     *
     * <p>Controller의 @Valid와 DTO의 @NotBlank, @NotNull,
     * @Email 등의 검증 어노테이션을 사용할 때 발생한다.</p>
     *
     * HTTP 400 Bad Request
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception
    ) {
        // 검증에 실패한 첫 번째 필드의 오류 메시지를 가져온다.
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("요청 데이터가 올바르지 않습니다.");

        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "INVALID_REQUEST",
                message
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * 별도로 처리하지 않은 모든 예외를 처리
     *
     * <p>예상하지 못한 서버 오류는 HTTP 500으로 반환한다.</p>
     *
     * <p>전체 Stack Trace는 서버 로그에만 기록하고,
     * 클라이언트에는 내부 구현 정보가 노출되지 않도록
     * 공통 오류 메시지만 반환한다.</p>
     *
     * HTTP 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllException(
            Exception exception
    ) {
        // 서버 로그에 예외와 전체 Stack Trace 기록
        log.error("처리되지 않은 서버 예외가 발생했습니다.", exception);

        ErrorResponse response = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                "서버 내부 오류가 발생했습니다."
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}