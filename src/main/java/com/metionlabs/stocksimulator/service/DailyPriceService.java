package com.metionlabs.stocksimulator.service;

import com.metionlabs.stocksimulator.controller.dto.dailyPrice.DailyPricePatchCommand;
import com.metionlabs.stocksimulator.controller.dto.dailyPrice.DailyPricePostCommand;
import com.metionlabs.stocksimulator.domain.DailyPrice;
import com.metionlabs.stocksimulator.domain.Stock;
import com.metionlabs.stocksimulator.repository.DailyPriceRepository;
import com.metionlabs.stocksimulator.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
@Transactional
public class DailyPriceService {

    private final DailyPriceRepository dailyPriceRepository;
    private final StockRepository stockRepository;

    // 생성자 주입
    public DailyPriceService(DailyPriceRepository dailyPriceRepository, StockRepository stockRepository) {
        this.dailyPriceRepository = dailyPriceRepository;
        this.stockRepository = stockRepository;
    }

    // ========================================================================
    // C — 생성
    // ========================================================================

    // 주식 종목 가격 생성
    public DailyPrice create(DailyPricePostCommand command) {
        Stock stock = stockRepository.findById(command.getStockId())
                .orElseThrow(() -> new NoSuchElementException(
                        "주식을 찾을 수 없습니다. stockId=" + command.getStockId()));

        if(dailyPriceRepository.existsByStockId(stock.getId())) {
            throw new IllegalStateException("이미 주식이 있습니다. stockId=" + stock.getId());
        }

        LocalDateTime now = LocalDateTime.now();
        DailyPrice dailyPrice = DailyPrice.builder()
                .stock(stock)
                .openPrice(command.getOpenPrice())
                .closePrice(command.getClosePrice())
                .highPrice(command.getHighPrice())
                .lowPrice(command.getLowPrice())
                .volume(command.getVolume())
                .reportTime(now)
                .build();

        return dailyPriceRepository.save(dailyPrice);
    }

    // ========================================================================
    // R — 조회
    // ========================================================================

    // id로 조회 없으면 예외
    @Transactional(readOnly = true)
    public DailyPrice getById(Long id) {
        return dailyPriceRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("주식 종목 가격을 찾을 수 없습니다. id=" + id));
    }

    // 주식ID로 조회 없으면 예외
    @Transactional(readOnly = true)
    public DailyPrice getByStockId(Long stockId) {
        return dailyPriceRepository.findByStockId(stockId)
                .orElseThrow(() -> new NoSuchElementException(
                "해당 주식의 주식 종목 가격이 없습니다. stockId=" + stockId));
    }

    // ========================================================================
    // U — 수정
    // ========================================================================

    // 주식 종목 가격 정보 부분 수정
    public DailyPrice update(Long id, DailyPricePatchCommand command) {
        DailyPrice dailyPrice = getById(id);
        dailyPrice.update(command.getOpenPrice(),
                        command.getClosePrice(),
                        command.getHighPrice(),
                        command.getLowPrice(),
                        command.getVolume());
        return dailyPrice;
    }

    // ========================================================================
    // D — 삭제
    // ========================================================================

    public void delete(Long id) {
        DailyPrice dailyPrice = getById(id);
        dailyPriceRepository.delete(dailyPrice);
    }
}