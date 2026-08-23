package com.metionlabs.stocksimulator.controller;

import com.metionlabs.stocksimulator.domain.Account;
import com.metionlabs.stocksimulator.domain.Holding;
import com.metionlabs.stocksimulator.domain.Stock;
import com.metionlabs.stocksimulator.repository.AccountRepository;
import com.metionlabs.stocksimulator.repository.HoldingRepository;
import com.metionlabs.stocksimulator.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/holdings")
public class HoldingController {

    @Autowired
    HoldingRepository holdingRepository;

    @Autowired
    StockRepository stockRepository;

    @Autowired
    AccountRepository accountRepository;

    @GetMapping("/{id}")
    public ResponseEntity<Holding> get(@PathVariable Long id) {
        Holding holding = holdingRepository.findById(id).orElseThrow();
        return ResponseEntity.ok(holding);
    }

    @PostMapping("")
    public ResponseEntity<?> post(
            @RequestBody Holding holding,
            @RequestParam Long stockId,
            @RequestParam Long accountId) {
        Stock stock = stockRepository.findById(stockId).orElseThrow();
        Account account = accountRepository.findById(accountId).orElseThrow();

        LocalDateTime localDateTime = LocalDateTime.now();

        holding.setStock(stock);
        holding.setAccount(account);
        holding.setCreatedAt(localDateTime);
        holding.setModifiedAt(localDateTime);

        holdingRepository.save(holding);

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