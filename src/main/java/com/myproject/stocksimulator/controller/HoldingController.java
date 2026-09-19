package com.myproject.stocksimulator.controller;

import com.myproject.stocksimulator.controller.dto.holding.HoldingGetResult;
import com.myproject.stocksimulator.controller.dto.holding.HoldingPatchCommand;
import com.myproject.stocksimulator.controller.dto.holding.HoldingPostCommand;
import com.myproject.stocksimulator.controller.dto.holding.HoldingPostResult;
import com.myproject.stocksimulator.domain.Holding;
import com.myproject.stocksimulator.service.HoldingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/holdings")
public class HoldingController {

    private final HoldingService holdingService;

    public HoldingController(HoldingService holdingService) {
        this.holdingService = holdingService;
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
    // R — GET /holdings/{id}, GET /holdings?accountId=
    // ========================================================================

    // 회원 보유 주식 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<HoldingGetResult> get(@PathVariable Long id) {
        Holding holding = holdingService.getById(id);
        return ResponseEntity.ok(HoldingGetResult.from(holding));
    }

    // 계좌ID로 회원 보유 주식 조회
    @GetMapping(params = "accountId")
    public ResponseEntity<List<HoldingGetResult>> getByAccount(@RequestParam Long accountId) {
        // 전달받은 계좌ID를 Service에 전달하여 해당 계좌가 보유하고 있는 모든 정보 조회
        List<Holding> holdings = holdingService.getByAccountId(accountId);

        // 조회한 Holding 엔티티 목록을 Controller에서 사용하는 응답 DTO 목록으로 반환
        List<HoldingGetResult> results = holdings.stream()
                .map(HoldingGetResult::from)
                .toList();
        return ResponseEntity.ok(results);
    }

    // ========================================================================
    // U — PATCH /holdings/{id}
    // ========================================================================

    // 회원 보유 주식 정보 수정
    @PatchMapping("/{id}")
    public ResponseEntity<HoldingGetResult> update(@PathVariable Long id,
                                                   @RequestBody @Valid HoldingPatchCommand command) {
        Holding holding = holdingService.update(id, command);
        return ResponseEntity.ok(HoldingGetResult.from(holding));
    }

    // ========================================================================
    // D — DELETE /holdings/{id}
    // ========================================================================

    // 회원 보유 주식 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        holdingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}