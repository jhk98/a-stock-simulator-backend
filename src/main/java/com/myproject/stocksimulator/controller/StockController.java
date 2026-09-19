package com.myproject.stocksimulator.controller;

import com.myproject.stocksimulator.controller.dto.stock.StockGetResult;
import com.myproject.stocksimulator.controller.dto.stock.StockPatchCommand;
import com.myproject.stocksimulator.controller.dto.stock.StockPostCommand;
import com.myproject.stocksimulator.controller.dto.stock.StockPostResult;
import com.myproject.stocksimulator.domain.stock.Stock;
import com.myproject.stocksimulator.service.StockService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stocks")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    // ========================================================================
    // C — POST /stocks
    // ========================================================================

    // 주식 종목 생성
    @PostMapping
    public ResponseEntity<StockPostResult> create(@RequestBody @Valid StockPostCommand command) {
        Stock stock = stockService.create(command);
        return ResponseEntity.ok(new StockPostResult(stock.getId()));
    }

    // ========================================================================
    // R — GET /stocks/{id}
    // ========================================================================

    // 주식 종목 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<StockGetResult> get(@PathVariable Long id) {
        Stock stock = stockService.getById(id);
        return ResponseEntity.ok(StockGetResult.from(stock));
    }

    // ========================================================================
    // U — PATCH /stocks/{id}
    // ========================================================================

    // 주식 종목 정보 수정
    @PatchMapping("/{id}")
    public ResponseEntity<StockGetResult> update(@PathVariable Long id,
                                                 @RequestBody @Valid StockPatchCommand command) {
        Stock stock = stockService.update(id, command);
        return ResponseEntity.ok(StockGetResult.from(stock));
    }

    // ========================================================================
    // D — DELETE /stocks/{id}
    // ========================================================================

    // 주식 종목 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        stockService.delete(id);
        return ResponseEntity.noContent().build();
    }
}