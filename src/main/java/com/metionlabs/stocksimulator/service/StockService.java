package com.metionlabs.stocksimulator.service;

import com.metionlabs.stocksimulator.controller.dto.stock.StockPatchCommand;
import com.metionlabs.stocksimulator.controller.dto.stock.StockPostCommand;
import com.metionlabs.stocksimulator.domain.Stock;
import com.metionlabs.stocksimulator.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@Transactional
public class StockService {

    private final StockRepository stockRepository;

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    // ========================================================================
    // C — 생성
    // ========================================================================

    public Stock create(StockPostCommand command) {
        Stock stock = Stock.builder()
                .stockCode(command.getStockCode())
                .stockName(command.getStockName())
                .stockType(command.getStockType())
                .build();

        return stockRepository.save(stock);
    }

    // ========================================================================
    // R — 조회
    // ========================================================================

    @Transactional(readOnly = true)
    public Stock getById(Long id) {
        return stockRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("주식 종목을 찾을 수 없습니다. id=" + id));
    }

    // ========================================================================
    // U — 수정
    // ========================================================================

    public Stock update(Long id, StockPatchCommand command) {
        Stock stock = getById(id);

        String newStockCode = command.getStockCode();
        if(newStockCode != null && !newStockCode.equals(stock.getStockCode())
                && stockRepository.existsByStockCode(newStockCode)) {
            throw new IllegalStateException("이미 사용 중인 주식코드입니다. " + newStockCode);
        }

        String newStockName = command.getStockName();
        if(newStockName != null && !newStockName.equals(stock.getStockName())
                && stockRepository.existsByStockName(newStockName)) {
            throw new IllegalStateException("이미 사용 중인 주식이름입니다. " + newStockName);
        }

        stock.update(newStockCode, newStockName);
        return stock;
    }

    // ========================================================================
    // D — 삭제
    // ========================================================================
    public void delete(Long id) {
        Stock stock = getById(id);
        stockRepository.delete(stock);
    }
}