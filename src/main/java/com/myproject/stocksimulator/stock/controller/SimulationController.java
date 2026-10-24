package com.myproject.stocksimulator.stock.controller;

import com.myproject.stocksimulator.stock.service.QuoteService;
import com.myproject.stocksimulator.stock.service.SimulationService;

import jakarta.validation.Valid;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/simulation")
public class SimulationController {

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

    @PostMapping("/orders")
    public ResponseEntity<SimulationService.OrderView> trade(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody SimulationService.TradeRequest request
    ) {
        return ok(simulation.trade(requireMember(memberId), request));
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

    private <T> ResponseEntity<T> ok(T body) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(body);
    }
}