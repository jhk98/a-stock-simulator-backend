package com.myproject.stocksimulator.market.controller;

import com.myproject.stocksimulator.exception.ErrorResponse;
import com.myproject.stocksimulator.market.service.MarketService;
import com.myproject.stocksimulator.stock.scheduler.QuoteScheduler;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 종목 목록과 거래대금 순위 API
 */
@RestController
@RequestMapping("/market")
public class MarketController {

    private final MarketService market;
    private final QuoteScheduler quoteScheduler;

    public MarketController(
            MarketService market,
            QuoteScheduler quoteScheduler
    ) {
        this.market = market;
        this.quoteScheduler = quoteScheduler;
    }

    /**
     * DB에 저장된 종목·시세를 즉시 반환한다.
     *
     * 현재 페이지에서 미수집되었거나 오래된 시세는
     * 백그라운드 수집 대기열에 등록한다.
     */
    @GetMapping("/stocks")
    public ResponseEntity<MarketService.StockPage> stocks(
            @RequestParam(defaultValue = "ALL") String market,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "PRICE_DESC") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        MarketService.StockPage result = this.market.search(
                market,
                keyword,
                sort,
                page,
                size
        );

        // 등록만 수행하며 외부 시세 API 응답을 기다리지 않는다.
        quoteScheduler.prioritize(result.items());

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(result);
    }

    @GetMapping("/popular")
    public ResponseEntity<MarketService.Ranking> popular() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(market.ranking());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> invalid(
            IllegalArgumentException exception
    ) {
        return ResponseEntity.badRequest().body(new ErrorResponse(
                400,
                "INVALID_MARKET_QUERY",
                exception.getMessage()
        ));
    }
}