package com.myproject.stocksimulator.stock.service;

import com.myproject.stocksimulator.kiwoom.client.KiwoomStockClient;
import com.myproject.stocksimulator.kiwoom.KiwoomStockInfo;
import com.myproject.stocksimulator.stock.entity.stock.StockType;
import com.myproject.stocksimulator.stock.dto.StockSyncResult;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StockSyncService {

    private final KiwoomStockClient kiwoomStockClient;
    private final StockSyncWriter stockSyncWriter;

    public StockSyncService(
            KiwoomStockClient kiwoomStockClient,
            StockSyncWriter stockSyncWriter
    ) {
        this.kiwoomStockClient = kiwoomStockClient;
        this.stockSyncWriter = stockSyncWriter;
    }

    /**
     * 코스피·코스닥 종목 목록을 모두 수집한 뒤 저장한다.
     *
     * <p>
     * 외부 API 호출 중에는 DB 트랜잭션을 열지 않는다.
     * synchronized는 현재 서버 인스턴스 내부의 중복 실행을 방지한다.
     * </p>
     */
    public synchronized StockSyncResult syncStocks() {
        List<StockSyncWriter.StockItem> items = new ArrayList<>();

        for (StockType market : StockType.values()) {
            List<KiwoomStockInfo> stocks =
                    kiwoomStockClient.getStockList(
                            market.kiwoomMarketCode()
                    );

            if (stocks == null || stocks.isEmpty()) {
                throw new IllegalStateException(
                        market.name() + " 종목 응답이 비어 있습니다."
                );
            }

            for (KiwoomStockInfo info : stocks) {
                if (info == null) {
                    throw new IllegalStateException(
                            "종목 정보가 누락되었습니다."
                    );
                }

                items.add(new StockSyncWriter.StockItem(
                        info.code(),
                        info.name(),
                        market
                ));
            }
        }

        return stockSyncWriter.saveStocks(
                List.copyOf(items)
        );
    }
}