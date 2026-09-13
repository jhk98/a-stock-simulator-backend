package com.metionlabs.stocksimulator.controller;

import com.metionlabs.stocksimulator.controller.dto.orders.OrdersGetResult;
import com.metionlabs.stocksimulator.controller.dto.orders.OrdersPatchCommand;
import com.metionlabs.stocksimulator.controller.dto.orders.OrdersPostCommand;
import com.metionlabs.stocksimulator.controller.dto.orders.OrdersPostResult;
import com.metionlabs.stocksimulator.domain.orders.Orders;
import com.metionlabs.stocksimulator.service.OrdersService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrdersController {

    private final OrdersService ordersService;

    public OrdersController(OrdersService ordersService) {
        this.ordersService = ordersService;
    }

    // ========================================================================
    // C — POST /orders
    // ========================================================================

    // 주문 생성
    @PostMapping
    public ResponseEntity<OrdersPostResult> create(@RequestBody @Valid OrdersPostCommand command) {
        Orders orders = ordersService.create(command);
        return ResponseEntity.ok(new OrdersPostResult(orders.getId()));
    }

    // ========================================================================
    // R — GET /orders/{id}, GET /orders?stockId=, GET /orders?accountId=
    // ========================================================================

    // 주문 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<OrdersGetResult> get(@PathVariable Long id) {
        Orders orders = ordersService.getById(id);
        return ResponseEntity.ok(OrdersGetResult.from(orders));
    }

    // 주식ID로 주문 조회
    @GetMapping(params = "stockId")
    public ResponseEntity<List<OrdersGetResult>> getByStock(@RequestParam Long stockId) {
        List<Orders> orders = ordersService.getByStockId(stockId);
        List<OrdersGetResult> results = orders.stream()
                .map(OrdersGetResult::from)
                .toList();
        return ResponseEntity.ok(results);
    }

    // 계좌ID로 주문 조회
    @GetMapping(params = "accountId")
    public ResponseEntity<List<OrdersGetResult>> getByAccount(@RequestParam Long accountId) {
        List<Orders> orders = ordersService.getByAccountId(accountId);
        List<OrdersGetResult> results = orders.stream()
                .map(OrdersGetResult::from)
                .toList();
        return ResponseEntity.ok(results);
    }

    // ========================================================================
    // U — PATCH /orders/{id}
    // ========================================================================

    // 주문 수정
    @PatchMapping("/{id}")
    public ResponseEntity<OrdersGetResult> update(@PathVariable Long id,
                                                  @RequestBody @Valid OrdersPatchCommand command) {
        Orders orders = ordersService.update(id, command);
        return ResponseEntity.ok(OrdersGetResult.from(orders));
    }

    // ========================================================================
    // D — DELETE /orders/{id}
    // ========================================================================

    // 주문 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ordersService.delete(id);
        return ResponseEntity.noContent().build();
    }
}