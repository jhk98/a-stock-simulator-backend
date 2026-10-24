package com.myproject.stocksimulator.stock.exception;

import com.myproject.stocksimulator.exception.ErrorResponse;
import com.myproject.stocksimulator.kiwoom.KiwoomApiException;
import com.myproject.stocksimulator.stock.controller.SimulationController;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * 시세·투자 현황·모의 주문 API의 오류 처리
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = SimulationController.class)
public class SimulationExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(SimulationExceptionHandler.class);

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> business(
            ResponseStatusException exception
    ) {
        int status = exception.getStatusCode().value();

        return ResponseEntity.status(status).body(new ErrorResponse(
                status,
                "SIMULATION_REQUEST_FAILED",
                exception.getReason() == null
                        ? "모의거래 요청을 처리하지 못했습니다."
                        : exception.getReason()
        ));
    }

    @ExceptionHandler(KiwoomApiException.class)
    public ResponseEntity<ErrorResponse> upstream(
            KiwoomApiException exception,
            HttpServletRequest request
    ) {
        // 시세 조회와 주문 중 시세 조회를 요청 경로로 구분한다.
        log.warn(
                "모의투자 API 실패: category=UPSTREAM path={} status={} type={}",
                safePath(request),
                exception.upstreamStatus(),
                exception.getClass().getSimpleName()
        );

        return ResponseEntity.status(502).body(new ErrorResponse(
                502,
                "QUOTE_UNAVAILABLE",
                "키움 시세를 확인하지 못했습니다. 잠시 후 다시 시도해주세요."
        ));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> database(
            DataAccessException exception,
            HttpServletRequest request
    ) {
        log.error(
                "모의투자 API 실패: category=DATABASE path={} type={}",
                safePath(request),
                exception.getClass().getSimpleName()
        );

        /*
         * DB 오류를 주문 실패 확정으로 표시하지 않는다.
         * 프론트는 미확정 주문을 유지하고 같은 requestId로 확인한다.
         */
        return ResponseEntity.status(500).body(new ErrorResponse(
                500,
                "SIMULATION_STORAGE_FAILED",
                "데이터 처리 중 오류가 발생했습니다. 주문은 결과 확인 후 재시도해주세요."
        ));
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            ArithmeticException.class
    })
    public ResponseEntity<ErrorResponse> invalid() {
        return ResponseEntity.badRequest().body(new ErrorResponse(
                400,
                "INVALID_SIMULATION_REQUEST",
                "요청 값 또는 계산 결과가 허용 범위를 벗어났습니다."
        ));
    }

    private String safePath(HttpServletRequest request) {
        String path = request.getRequestURI()
                .replaceAll("[^A-Za-z0-9/_-]", "?");

        return path.substring(0, Math.min(path.length(), 120));
    }
}