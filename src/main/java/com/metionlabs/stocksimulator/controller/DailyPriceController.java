package com.metionlabs.stocksimulator.controller;

import com.metionlabs.stocksimulator.domain.DailyPrice;
import com.metionlabs.stocksimulator.domain.Stock;
import com.metionlabs.stocksimulator.repository.DailyPriceRepository;
import com.metionlabs.stocksimulator.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/dailyPrices")
public class DailyPriceController {

    @Autowired
    DailyPriceRepository dailyPriceRepository;

    @Autowired
    StockRepository stockRepository;

    @GetMapping("/{id}")
    public ResponseEntity<DailyPrice> get(@PathVariable Long id) {
        DailyPrice dailyPrice = dailyPriceRepository.findById(id).orElseThrow();
        return ResponseEntity.ok(dailyPrice);
    }

    @PostMapping("")
    public ResponseEntity<?> post(
            @RequestBody DailyPrice dailyPrice,
            @RequestParam Long stockId) {
        Stock stock = stockRepository.findById(stockId).orElseThrow();

        LocalDateTime localDateTime = LocalDateTime.now();

        dailyPrice.setStock(stock);
        dailyPrice.setReportTime(localDateTime);

        dailyPriceRepository.save(dailyPrice);

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