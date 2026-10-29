package com.myproject.stocksimulator.stock.controller;

import com.myproject.stocksimulator.stock.service.QuoteService;
import com.myproject.stocksimulator.stock.service.SimulationService;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

/**
 * 시세, 투자현황, 모의 주문 API.
 *
 * 주문 요청 ID를 기록하여 브라우저 로그와 서버 로그를 연결한다.
 * 토큰, 쿠키, 회원정보, 계좌 전체 응답은 로그에 출력하지 않는다.
 */
@RestController
@RequestMapping("/simulation")
public class SimulationController {

    private static final Logger log =
            LoggerFactory.getLogger(SimulationController.class);

    private final QuoteService quotes;
    private final SimulationService simulation;

    public SimulationController(
            QuoteService quotes,
            SimulationService simulation
    ) {
        this.quotes = quotes;
        this.simulation = simulation;
    }

    @GetMapping("/quotes/{code}")
    public ResponseEntity<QuoteService.Quote> quote(
            @AuthenticationPrincipal Long memberId,
            @PathVariable String code
    ) {
        requireMember(memberId);
        return ok(quotes.quote(code, false));
    }

    @GetMapping("/overview")
    public ResponseEntity<SimulationService.Overview> overview(
            @AuthenticationPrincipal Long memberId
    ) {
        return ok(simulation.overview(requireMember(memberId)));
    }

    /**
     * 유효성 검사를 통과해 컨트롤러에 도달한 주문을 기록한다.
     *
     * 보안 필터 또는 @Valid 검사에서 거절된 요청은
     * 이 메서드 실행 전에 차단되므로 ORDER_RECEIVED가 남지 않는다.
     */
    @PostMapping("/orders")
    public ResponseEntity<SimulationService.OrderView> trade(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody SimulationService.TradeRequest request
    ) {
        long startedAt = System.nanoTime();

        String requestId = safeLogValue(request.requestId());
        String stockCode = safeLogValue(request.stockCode());
        String side = safeLogValue(request.side());

        log.info(
                "[TRADE][ORDER_RECEIVED] requestId={} stock={} side={} quantity={}",
                requestId,
                stockCode,
                side,
                request.quantity()
        );

        try {
            SimulationService.OrderView order = simulation.trade(
                    requireMember(memberId),
                    request
            );

            /*
             * 서비스가 정상 반환한 주문 결과다.
             * 기존에 완료된 동일 요청의 재조회도 포함하므로
             * '이번에 새로 체결됨'이라고 단정하는 로그는 사용하지 않는다.
             */
            log.info(
                    "[TRADE][ORDER_RESULT] requestId={} orderId={} status={} elapsedMs={}",
                    requestId,
                    order.id(),
                    order.status(),
                    elapsedMillis(startedAt)
            );

            return ok(order);

        } catch (RuntimeException exception) {
            /*
             * 예외를 삼키거나 새로운 오류 응답으로 바꾸지 않는다.
             * 기존 ExceptionHandler가 HTTP 상태와 응답을 결정한다.
             *
             * 오류 메시지나 외부 응답 전체를 기록하지 않고
             * 요청 ID, 예외 종류, 처리 시간만 기록한다.
             */
            log.warn(
                    "[TRADE][ORDER_EXCEPTION] requestId={} type={} elapsedMs={}",
                    requestId,
                    exception.getClass().getSimpleName(),
                    elapsedMillis(startedAt)
            );

            throw exception;
        }
    }

    @GetMapping("/orders")
    public ResponseEntity<SimulationService.OrderPage> orders(
            @AuthenticationPrincipal Long memberId,
            @RequestParam(required = false) Long beforeId
    ) {
        return ok(simulation.orders(requireMember(memberId), beforeId));
    }

    private long requireMember(Long memberId) {
        if (memberId == null) {
            throw new BadCredentialsException("로그인이 필요합니다.");
        }

        return memberId;
    }

    private long elapsedMillis(long startedAt) {
        return TimeUnit.NANOSECONDS.toMillis(
                System.nanoTime() - startedAt
        );
    }

    // 입력값의 개행 등으로 로그 구조가 깨지지 않도록 제한한다.
    private String safeLogValue(String value) {
        if (value == null) {
            return "-";
        }

        String cleaned = value.replaceAll("[^A-Za-z0-9_-]", "?");
        return cleaned.substring(0, Math.min(cleaned.length(), 64));
    }

    private <T> ResponseEntity<T> ok(T body) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(body);
    }
}