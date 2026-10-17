//package com.myproject.stocksimulator.scheduler;
//
//import com.myproject.stocksimulator.dto.stock.StockSyncResult;
//import com.myproject.stocksimulator.service.stock.StockSyncService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//@Slf4j
//@Component
//@ConditionalOnProperty(
//        name = "stock.sync.enabled",
//        havingValue = "true"
//)
//public class StockSyncScheduler {
//
//    private final StockSyncService stockSyncService;
//
//    public StockSyncScheduler(StockSyncService stockSyncService) {
//        this.stockSyncService = stockSyncService;
//    }
//
//    @Scheduled(
//            initialDelayString = "${stock.sync.initial-delay-ms:15000}",
//            fixedDelayString = "${stock.sync.delay-ms:21600000}"
//    )
//    public void synchronize() {
//        try {
//            StockSyncResult result = stockSyncService.syncStocks();
//
//            log.info(
//                    "종목 동기화 완료: total={}, inserted={}, updated={}, unchanged={}",
//                    result.totalCount(),
//                    result.insertedCount(),
//                    result.updatedCount(),
//                    result.unchangedCount()
//            );
//
//        } catch (Exception e) {
//            // 실패하면 기존 데이터와 마지막 성공 시각은 그대로 유지됩니다.
//            // 외부 원문 응답이나 인증 정보는 로그로 출력하지 않습니다.
//            log.error(
//                    "종목 동기화 실패. 기존 데이터를 유지합니다. errorType={}",
//                    e.getClass().getSimpleName()
//            );
//        }
//    }
//}