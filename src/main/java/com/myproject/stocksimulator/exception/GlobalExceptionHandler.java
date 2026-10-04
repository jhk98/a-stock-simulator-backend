package com.myproject.stocksimulator.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.NoSuchElementException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 조회한 데이터가 존재하지 않을 때
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Void> handleNoSuchElementException(
            NoSuchElementException exception
    ) {
        return ResponseEntity
                .notFound()
                .build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllException(Exception ex) {

        // 1. 서버 콘솔/로그 시스템에 전체 스택 트레이스 기록
        log.error("Unhandled Exception occurred: ", ex);

        String stackTraceStr = null;

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        stackTraceStr = sw.toString();

        // 3. 에러 응답 객체 생성
        ErrorResponse response = new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                ex.getMessage() != null ? ex.getMessage() : "알 수 없는 서버에 오류가 발생했습니다.",
                stackTraceStr
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}