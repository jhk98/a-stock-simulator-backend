package com.metionlabs.stocksimulator.service;

import com.metionlabs.stocksimulator.controller.dto.currentPrice.CurrentPricePatchCommand;
import com.metionlabs.stocksimulator.controller.dto.currentPrice.CurrentPricePostCommand;
import com.metionlabs.stocksimulator.domain.CurrentPrice;
import com.metionlabs.stocksimulator.domain.stock.Stock;
import com.metionlabs.stocksimulator.repository.CurrentPriceRepository;
import com.metionlabs.stocksimulator.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
@Transactional
public class CurrentPriceService {

    private final CurrentPriceRepository currentPriceRepository;
    private final StockRepository stockRepository;

    // 생성자 주입
    public CurrentPriceService(CurrentPriceRepository currentPriceRepository, StockRepository stockRepository) {
        this.currentPriceRepository = currentPriceRepository;
        this.stockRepository = stockRepository;
    }

    // ========================================================================
    // C — 생성
    // ========================================================================

    // 주식 현재가 생성
    public CurrentPrice create(CurrentPricePostCommand command) {
        Stock stock = stockRepository.findById(command.getStockId())
                .orElseThrow(() -> new NoSuchElementException(
                        "주식을 찾을 수 없습니다. stockId = " + command.getStockId()));

        if(currentPriceRepository.existsByStockId(stock.getId())) {
            throw new IllegalStateException("이미 주식이 있습니다. stockId = " + stock.getId());
        }

        LocalDateTime now = LocalDateTime.now();

        CurrentPrice currentPrice = CurrentPrice.builder()
                .stock(stock)
                .currentPrice(command.getCurrentPrice())
                .changePrice(command.getChangePrice())
                .changeRate(command.getChangeRate())
                .currentVolume(command.getCurrentVolume())
                .updatedAt(now)
                .build();

        return currentPriceRepository.save(currentPrice);
    }

    // ========================================================================
    // R — 조회
    // ========================================================================

    // id로 조회 없으면 예외
    @Transactional(readOnly = true)
    public CurrentPrice getById(Long id) {
        return currentPriceRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("주식 현재가를 찾을 수 없습니다. id=" + id));
    }

    // 주식ID로 조회 없으면 예외
    @Transactional(readOnly = true)
    public CurrentPrice getByStockId(Long stockId) {
        return currentPriceRepository.findByStockId(stockId)
                .orElseThrow(() -> new NoSuchElementException(
                        "해당 주식의 주식 현재가가 없습니다. stockId=" + stockId));
    }

    // ========================================================================
    // U — 수정
    // ========================================================================

    // 주식 현재가 정보 수정
    public CurrentPrice update(Long id, CurrentPricePatchCommand command) {
        CurrentPrice currentPrice = getById(id);
        currentPrice.update(command.getCurrentPrice(),
                command.getChangePrice(),
                command.getChangeRate(),
                command.getCurrentVolume());
        return currentPrice;
    }

    // ========================================================================
    // D — 삭제
    // ========================================================================

    // 주식 현재가 삭제
    public void delete(Long id) {
        CurrentPrice currentPrice = getById(id);
        currentPriceRepository.delete(currentPrice);
    }
}