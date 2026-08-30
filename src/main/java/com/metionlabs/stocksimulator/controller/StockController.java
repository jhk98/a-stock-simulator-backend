package com.metionlabs.stocksimulator.controller;

import com.metionlabs.stocksimulator.controller.dto.stock.StockGetResult;
import com.metionlabs.stocksimulator.controller.dto.stock.StockPatchCommand;
import com.metionlabs.stocksimulator.controller.dto.stock.StockPostCommand;
import com.metionlabs.stocksimulator.controller.dto.stock.StockPostResult;
import com.metionlabs.stocksimulator.domain.Stock;
import com.metionlabs.stocksimulator.repository.StockRepository;
import com.metionlabs.stocksimulator.service.StockService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stocks")
public class StockController {

    private final StockService stockService;
    private final StockRepository stockRepository;

    public StockController(StockService stockService, StockRepository stockRepository) {
        this.stockService = stockService;
        this.stockRepository = stockRepository;
    }

    // ========================================================================
    // C — POST /stocks
    // ========================================================================

    // 주식 종목 만들기
    @PostMapping
    public ResponseEntity<StockPostResult> create(@RequestBody @Valid StockPostCommand command) {
        Stock stock = stockService.create(command);
        return ResponseEntity.ok(new StockPostResult(stock.getId()));
    }

    // ========================================================================
    // R — GET /accounts/{id}, GET /accounts?memberId=
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

    @PatchMapping("/{id}")
    public ResponseEntity<StockGetResult> update(@PathVariable Long id,
                                                 @RequestBody @Valid StockPatchCommand command) {
        Stock stock = stockService.update(id, command);
        return ResponseEntity.ok(StockGetResult.from(stock));
    }

    // ========================================================================
    // D — DELETE /stocks/{id}
    // ========================================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        stockService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
