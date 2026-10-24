package com.myproject.stocksimulator.stock.scheduler;

import com.myproject.stocksimulator.stock.dto.StockSyncResult;
import com.myproject.stocksimulator.stock.service.StockSyncService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "stock.sync.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class StockSyncSchedular {

    private final StockSyncService stockSyncService;

    /**
     * 서버 시작 15초 후 첫 실행
     * 이후 작업 종료 시점부터 기본 6시간 간격으로 실행한다.
     *
     * <p> 현재가가 아닌 종목 목록을 동기화한다. </p>
     */
    @Scheduled(
            initialDelayString = "${stock.sync.initial-delay-ms:15000}",
            fixedDelayString = "${stock.sync.delay-ms:21600000}"
    )
    public void synchronize() {
        try {
            StockSyncResult result = stockSyncService.syncStocks();

            log.info(
                    "종목 동기화 완료: total={}, inserted={}, updated={}, unchanged={}",
                    result.totalCount(),
                    result.insertedCount(),
                    result.updatedCount(),
                    result.unchangedCount()
            );
        } catch (Exception exception) {
            log.error(
                    "종목 동기화 실패. 기존 데이터를 유지합니다. type={}",
                    exception.getClass().getSimpleName()
            );
        }
    }
}