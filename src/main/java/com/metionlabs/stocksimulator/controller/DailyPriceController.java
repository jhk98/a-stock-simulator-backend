package com.metionlabs.stocksimulator.controller;

import com.metionlabs.stocksimulator.controller.dto.dailyPrice.DailyPriceGetResult;
import com.metionlabs.stocksimulator.controller.dto.dailyPrice.DailyPricePatchCommand;
import com.metionlabs.stocksimulator.controller.dto.dailyPrice.DailyPricePostCommand;
import com.metionlabs.stocksimulator.controller.dto.dailyPrice.DailyPricePostResult;
import com.metionlabs.stocksimulator.domain.DailyPrice;
import com.metionlabs.stocksimulator.service.DailyPriceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dailyPrices")
public class DailyPriceController {

    private final DailyPriceService dailyPriceService;

    public DailyPriceController(DailyPriceService dailyPriceService) {
        this.dailyPriceService = dailyPriceService;
    }

    // ========================================================================
    // C — POST /dailyPrices
    // ========================================================================

    // 주식 종목 가격 생성
    @PostMapping
    public ResponseEntity<DailyPricePostResult> post(@RequestBody @Valid DailyPricePostCommand command) {
        DailyPrice dailyPrice = dailyPriceService.create(command);
        return ResponseEntity.ok(new DailyPricePostResult(dailyPrice.getId()));
    }

    // ========================================================================
    // R — GET /dailyPrices/{id}, GET /dailyPrices?stockId=
    // ========================================================================

    // 주식 종목 가격 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<DailyPriceGetResult> get(@PathVariable Long id) {
        DailyPrice dailyPrice = dailyPriceService.getById(id);
        return ResponseEntity.ok(DailyPriceGetResult.from(dailyPrice));
    }

    // 주식ID로 주식 종목 가격 조회
    @GetMapping(params = "stockId")
    public ResponseEntity<List<DailyPriceGetResult>> getByStock(@RequestParam Long stockId) {
        List<DailyPrice> dailyPrice = dailyPriceService.getByStockId(stockId);
        List<DailyPriceGetResult> results = dailyPrice.stream()
                .map(DailyPriceGetResult::from)
                .toList();
        return ResponseEntity.ok(results);
    }

    // ========================================================================
    // U — PATCH /dailyPrices/{id}
    // ========================================================================

    // 주식 종목 가격 수정
    @PatchMapping("/{id}")
    public ResponseEntity<DailyPriceGetResult> update(@PathVariable Long id,
                                                      @RequestBody @Valid DailyPricePatchCommand command) {
        DailyPrice dailyPrice = dailyPriceService.update(id, command);
        return ResponseEntity.ok(DailyPriceGetResult.from(dailyPrice));
    }

    // ========================================================================
    // D — DELETE /dailyPrices/{id}
    // ========================================================================

    // 주식 종목 가격 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        dailyPriceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}