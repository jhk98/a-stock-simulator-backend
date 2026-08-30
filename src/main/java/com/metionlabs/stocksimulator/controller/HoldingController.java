package com.metionlabs.stocksimulator.controller;

import com.metionlabs.stocksimulator.controller.dto.holding.HoldingGetResult;
import com.metionlabs.stocksimulator.controller.dto.holding.HoldingPatchCommand;
import com.metionlabs.stocksimulator.controller.dto.holding.HoldingPostCommand;
import com.metionlabs.stocksimulator.controller.dto.holding.HoldingPostResult;
import com.metionlabs.stocksimulator.domain.Holding;
import com.metionlabs.stocksimulator.service.HoldingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/holdings")
public class HoldingController {

    private final HoldingService holdingService;

    public HoldingController(HoldingService holdingservice) {
        this.holdingService = holdingservice;
    }

    // ========================================================================
    // C — POST /holdings
    // ========================================================================

    // 회원 보유 주식 생성
    @PostMapping
    public ResponseEntity<HoldingPostResult> create(@RequestBody @Valid HoldingPostCommand command) {
        Holding holding = holdingService.create(command);
        return ResponseEntity.ok(new HoldingPostResult(holding.getId()));
    }

    // ========================================================================
    // R — GET /holdings/{id}, GET /holdings?stockId=, GET /holdings?accountId=
    // ========================================================================

    // 회원 보유 주식 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<HoldingGetResult> get(@PathVariable Long id) {
        Holding holding = holdingService.getById(id);
        return ResponseEntity.ok(HoldingGetResult.from(holding));
    }

    // 주식ID로 회원 보유 주식 조회
    @GetMapping(params = "stockId")
    public ResponseEntity<HoldingGetResult> getByStock(@RequestParam Long stockId) {
        Holding holding = holdingService.getByStockId(stockId);
        return ResponseEntity.ok(HoldingGetResult.from(holding));
    }

    // 계좌ID로 회원 보유 주식 조회
    @GetMapping(params = "accountId")
    public ResponseEntity<HoldingGetResult> getByAccount(@RequestParam Long accountId) {
        Holding holding = holdingService.getByAccountId(accountId);
        return ResponseEntity.ok(HoldingGetResult.from(holding));
    }

    // ========================================================================
    // U — PATCH /accounts/{id}
    // ========================================================================

    @PatchMapping("/{id}")
    public ResponseEntity<HoldingGetResult> update(@PathVariable Long id,
                                                   @RequestBody @Valid HoldingPatchCommand command) {
        Holding holding = holdingService.update(id, command);
        return ResponseEntity.ok(HoldingGetResult.from(holding));
    }

    // ========================================================================
    // D — DELETE /accounts/{id}
    // ========================================================================

    // 회원 보유 주식 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        holdingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}