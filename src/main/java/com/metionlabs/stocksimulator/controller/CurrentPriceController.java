package com.metionlabs.stocksimulator.controller;

import com.metionlabs.stocksimulator.domain.CurrentPrice;
import com.metionlabs.stocksimulator.domain.Stock;
import com.metionlabs.stocksimulator.repository.CurrentPriceRepository;
import com.metionlabs.stocksimulator.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/currentPrices")
public class CurrentPriceController {

    @Autowired
    CurrentPriceRepository currentPriceRepository;

    @Autowired
    StockRepository stockRepository;

    @GetMapping("/{id}")
    public ResponseEntity<CurrentPrice> get(@PathVariable Long id) {
        CurrentPrice currentPrice = currentPriceRepository.findById(id).orElseThrow();
        return ResponseEntity.ok(currentPrice);
    }

    @PostMapping("")
    public ResponseEntity<?> post(
            @RequestBody CurrentPrice currentPrice,
            @RequestParam Long stockId) {
        Stock stock = stockRepository.findById(stockId).orElseThrow();

        LocalDateTime localDateTime = LocalDateTime.now();

        currentPrice.setStock(stock);
        currentPrice.setUpdatedAt(localDateTime);

        currentPriceRepository.save(currentPrice);

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