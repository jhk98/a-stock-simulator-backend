package com.myproject.stocksimulator.stock.controller;

import com.myproject.stocksimulator.exception.ErrorResponse;
import com.myproject.stocksimulator.kiwoom.KiwoomApiException;
import com.myproject.stocksimulator.stock.service.DailyChartService;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.NoSuchElementException;

/**
 * 종목 일봉 차트 조회
 * 오류 응답과 서버 진단 로그를 구분해서 처리한다.
 */
@RestController
@RequestMapping("/simulation/charts")
public class StockChartController {

    private static final Logger log =
            LoggerFactory.getLogger(StockChartController.class);

    private final DailyChartService charts;

    public StockChartController(DailyChartService charts) {
        this.charts = charts;
    }

    @GetMapping("/{code}")
    public ResponseEntity<DailyChartService.ChartResponse> daily(
            @AuthenticationPrincipal Long memberId,
            @PathVariable String code
    ) {
        if (memberId == null) {
            throw new BadCredentialsException("로그인이 필요합니다.");
        }

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(charts.daily(code));
    }

    // 서비스가 지정한 404 등의 상태를 그대로 전달한다.
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> business(
            ResponseStatusException exception
    ) {
        int status = exception.getStatusCode().value();

        return ResponseEntity.status(status).body(new ErrorResponse(
                status,
                status == 404 ? "STOCK_NOT_FOUND" : "CHART_REQUEST_FAILED",
                exception.getReason() == null
                        ? "차트 요청을 처리하지 못했습니다."
                        : exception.getReason()
        ));
    }

    @ExceptionHandler(KiwoomApiException.class)
    public ResponseEntity<ErrorResponse> upstream(
            KiwoomApiException exception,
            HttpServletRequest request
    ) {
        // 인증 헤더, 토큰, 원문 응답은 기록하지 않는다.
        log.warn(
                "차트 조회 실패: category=UPSTREAM path={} status={} type={}",
                safePath(request),
                exception.upstreamStatus(),
                exception.getClass().getSimpleName()
        );

        return ResponseEntity.status(502).body(new ErrorResponse(
                502,
                "CHART_UNAVAILABLE",
                "키움 일봉 데이터를 조회하지 못했습니다. 잠시 후 다시 시도해주세요."
        ));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> database(
            DataAccessException exception,
            HttpServletRequest request
    ) {
        log.error(
                "차트 조회 실패: category=DATABASE path={} type={}",
                safePath(request),
                exception.getClass().getSimpleName()
        );

        return ResponseEntity.status(500).body(new ErrorResponse(
                500,
                "CHART_STORAGE_FAILED",
                "차트 조회에 필요한 종목 정보를 확인하지 못했습니다."
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> invalid() {
        return ResponseEntity.badRequest().body(new ErrorResponse(
                400,
                "INVALID_STOCK_CODE",
                "종목코드가 올바르지 않습니다."
        ));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> notFound() {
        return ResponseEntity.status(404).body(new ErrorResponse(
                404,
                "STOCK_NOT_FOUND",
                "등록된 국내 종목이 아닙니다."
        ));
    }

    // 로그 줄바꿈 삽입을 막고 요청 경로만 제한된 길이로 기록한다.
    private String safePath(HttpServletRequest request) {
        String path = request.getRequestURI()
                .replaceAll("[^A-Za-z0-9/_-]", "?");

        return path.substring(0, Math.min(path.length(), 120));
    }
}