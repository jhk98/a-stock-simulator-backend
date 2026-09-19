package com.myproject.stocksimulator.controller;

import com.myproject.stocksimulator.controller.dto.currentPrice.CurrentPriceGetResult;
import com.myproject.stocksimulator.controller.dto.currentPrice.CurrentPricePatchCommand;
import com.myproject.stocksimulator.controller.dto.currentPrice.CurrentPricePostCommand;
import com.myproject.stocksimulator.controller.dto.currentPrice.CurrentPricePostResult;
import com.myproject.stocksimulator.domain.CurrentPrice;
import com.myproject.stocksimulator.service.CurrentPriceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/currentPrices")
public class CurrentPriceController {

    private final CurrentPriceService currentPriceService;

    public CurrentPriceController(CurrentPriceService currentPriceService) {
        this.currentPriceService = currentPriceService;
    }

    // ========================================================================
    // C — POST /currentPrices
    // ========================================================================

    @PostMapping
    public ResponseEntity<CurrentPricePostResult> post(@RequestBody @Valid CurrentPricePostCommand command) {
        CurrentPrice currentPrice = currentPriceService.create(command);
        return ResponseEntity.ok(new CurrentPricePostResult(currentPrice.getId()));
    }

    // ========================================================================
    // R — GET /currentPrices/{id}, GET /currentPrices?stockId=
    // ========================================================================

    // 주식 현재가 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<CurrentPriceGetResult> get(@PathVariable Long id) {
        CurrentPrice currentPrice = currentPriceService.getById(id);
        return ResponseEntity.ok(CurrentPriceGetResult.from(currentPrice));
    }

    // 주식ID로 주식 현재가 조회
    @GetMapping(params = "stockId")
    public ResponseEntity<CurrentPriceGetResult> getByStock(@RequestParam Long stockId) {
        CurrentPrice currentPrice = currentPriceService.getByStockId(stockId);
        return ResponseEntity.ok(CurrentPriceGetResult.from(currentPrice));
    }

    // ========================================================================
    // U — PATCH /currentPrices/{id}
    // ========================================================================

    // 주식 현재가 부분 수정
    @PatchMapping("/{id}")
    public ResponseEntity<CurrentPriceGetResult> update(@PathVariable Long id,
                                                        @RequestBody @Valid CurrentPricePatchCommand command) {
        CurrentPrice currentPrice = currentPriceService.update(id, command);
        return ResponseEntity.ok(CurrentPriceGetResult.from(currentPrice));
    }

    // ========================================================================
    // D — DELETE /currentPrices/{id}
    // ========================================================================

    // 주식 현재가 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        currentPriceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}