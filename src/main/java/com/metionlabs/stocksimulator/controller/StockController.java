package com.metionlabs.stocksimulator.controller;

import com.metionlabs.stocksimulator.domain.Stock;
import com.metionlabs.stocksimulator.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stocks")
public class StockController {

    @Autowired
    StockRepository stockRepository;

    @GetMapping("/{id}")
    public ResponseEntity<Stock> get(@PathVariable Long id) {
        Stock stock = stockRepository.findById(id).orElseThrow();
        return ResponseEntity.ok(stock);
    }

    @PostMapping("")
    public ResponseEntity<?> post(@RequestBody Stock stock) {
        stockRepository.save(stock);
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
