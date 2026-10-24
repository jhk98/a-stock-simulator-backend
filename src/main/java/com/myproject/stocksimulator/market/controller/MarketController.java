package com.myproject.stocksimulator.market.controller;

import com.myproject.stocksimulator.exception.ErrorResponse;
import com.myproject.stocksimulator.market.service.MarketService;

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

    public MarketController(MarketService market) {
        this.market = market;
    }

    /**
     * 종목 검색과 정렬
     * 정렬을 지정하지 않으면 현재가 높은순으로 조회한다.
     */
    @GetMapping("/stocks")
    public ResponseEntity<MarketService.StockPage> stocks(
            @RequestParam(defaultValue = "ALL") String market,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "PRICE_DESC") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(this.market.search(market, keyword, sort, page, size));
    }

    // 기존 메인 화면의 거래대금 순위 조회를 유지한다.
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