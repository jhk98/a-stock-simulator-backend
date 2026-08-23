package com.metionlabs.stocksimulator.controller;

import com.metionlabs.stocksimulator.domain.Account;
import com.metionlabs.stocksimulator.domain.Orders;
import com.metionlabs.stocksimulator.domain.Stock;
import com.metionlabs.stocksimulator.repository.AccountRepository;
import com.metionlabs.stocksimulator.repository.OrdersRepository;
import com.metionlabs.stocksimulator.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/orders")
public class OrdersController {

    @Autowired
    OrdersRepository ordersRepository;

    @Autowired
    StockRepository stockRepository;

    @Autowired
    AccountRepository accountRepository;

    @GetMapping("/{id}")
    public ResponseEntity<Orders> get(@PathVariable Long id) {
        Orders orders = ordersRepository.findById(id).orElseThrow();
        return ResponseEntity.ok(orders);
    }

    @PostMapping("")
    public ResponseEntity<?> post(
            @RequestBody Orders orders,
            @RequestParam Long stockId,
            @RequestParam Long accountId) {
        Stock stock = stockRepository.findById(stockId).orElseThrow();
        Account account = accountRepository.findById(accountId).orElseThrow();

        LocalDateTime localDateTime = LocalDateTime.now();

        orders.setStock(stock);
        orders.setAccount(account);
        orders.setCreatedAt(localDateTime);

        ordersRepository.save(orders);

//        return new ResponseEntity<>(HttpStatus.OK);
        return ResponseEntity.ok("created");
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> patch(@PathVariable Long id) {
//        return new ResponseEntity<>(HttpStatus.OK);
        return ResponseEntity.ok(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
//        return new ResponseEntity<>(HttpStatus.OK);
        return ResponseEntity.ok(id);
    }
}