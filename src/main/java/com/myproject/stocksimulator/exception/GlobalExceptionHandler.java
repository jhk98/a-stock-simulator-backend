package com.myproject.stocksimulator.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

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
}