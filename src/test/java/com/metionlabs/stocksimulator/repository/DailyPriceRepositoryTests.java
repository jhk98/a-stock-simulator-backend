package com.metionlabs.stocksimulator.repository;

import com.metionlabs.stocksimulator.domain.DailyPrice;
import com.metionlabs.stocksimulator.domain.stock.Stock;
import com.metionlabs.stocksimulator.domain.stock.StockType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static com.metionlabs.stocksimulator.util.TestRandomGenerator.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
public class DailyPriceRepositoryTests {

    @Autowired
    DailyPriceRepository dailyPriceRepository;

    @Autowired
    StockRepository stockRepository;

    // PK에 해당되는 id값들의 리스트 생성
    List<Long> pks = new ArrayList<>();

    // 랜덤 인덱스 생성
    private int randomIndex() {
        return (int) (Math.random() * pks.size());
    }

    // 테스트 준비
    @BeforeAll
    public void prepare() {
        // 테스트용 주식 3개 생성
        Stock stock1 = Stock.builder()
                .stockCode("abcd-123")
                .stockName("삼성전자")
                .stockType(StockType.KOSDAQ)
                .build();

        Stock stock2 = Stock.builder()
                .stockCode("efgh-234")
                .stockName("현대자동차")
                .stockType(StockType.KOSDAQ)
                .build();

        Stock stock3 = Stock.builder()
                .stockCode("ijkl-345")
                .stockName("SK하이닉스")
                .stockType(StockType.KOSDAQ)
                .build();

        // 주식 데이터 3개 삽입
        Stock saveStock1 = stockRepository.save(stock1);
        Stock saveStock2 = stockRepository.save(stock2);
        Stock saveStock3 = stockRepository.save(stock3);

        // 첫 번째 데이터
        DailyPrice dailyPrice1 = DailyPrice.builder()
                .stock(saveStock1)
                .openPrice(randomOpenPrice())
                .closePrice(randomClosePrice())
                .highPrice(randomHighPrice())
                .lowPrice(randomLowPrice())
                .volume(randomVolume())
                .priceDate(LocalDate.now())
                .build();

        // 두 번째 데이터
        DailyPrice dailyPrice2 = DailyPrice.builder()
                .stock(saveStock2)
                .openPrice(randomOpenPrice())
                .closePrice(randomClosePrice())
                .highPrice(randomHighPrice())
                .lowPrice(randomLowPrice())
                .volume(randomVolume())
                .priceDate(LocalDate.now())
                .build();

        // 세 번째 데이터
        DailyPrice dailyPrice3 = DailyPrice.builder()
                .stock(saveStock3)
                .openPrice(randomOpenPrice())
                .closePrice(randomClosePrice())
                .highPrice(randomHighPrice())
                .lowPrice(randomLowPrice())
                .volume(randomVolume())
                .priceDate(LocalDate.now())
                .build();

        // 세 개의 데이터 삽입
        DailyPrice save1 = dailyPriceRepository.save(dailyPrice1);
        DailyPrice save2 = dailyPriceRepository.save(dailyPrice2);
        DailyPrice save3 = dailyPriceRepository.save(dailyPrice3);

        // 세 개의 데이터에서 PK값들을 리스트에 저장
        pks.add(save1.getId());
        pks.add(save2.getId());
        pks.add(save3.getId());
    }

    // 테스트 끝
    @AfterAll
    public void done() {
        // 모든 주식 종목 가격 데이터 삭제
        dailyPriceRepository.deleteAll();
        // 전체 데이터 조회
        List<DailyPrice> dailyPrices = dailyPriceRepository.findAll();
        // 주식 종목 가격 데이터가 없는지 확인
        assertTrue(dailyPrices.isEmpty());

        // 모든 주식 데이터 삭제
        stockRepository.deleteAll();
        // 전체 데이터 조회
        List<Stock> stocks = stockRepository.findAll();
        // 주식 데이터가 없는지 확인
        assertTrue(stocks.isEmpty());
    }

    // crud
    @Order(1)
    @Test
    public void findAll() {
        // 테스트 준비에 넣은 데이터들을 리스트로 만들기
        List<DailyPrice> testData = dailyPriceRepository.findAll();
        // 조회된 데이터가 존재하는지 확인
        assertEquals(3, testData.size());
    }

    @Order(2)
    @Test
    public void insert() {
        // 주식 정보 추가
        Stock stock4 = Stock.builder()
                .stockCode("mnop-456")
                .stockName("LG전자")
                .stockType(StockType.KOSDAQ)
                .build();

        // 주식 정보 저장
        Stock saveStock4 = stockRepository.save(stock4);

        // 랜덤 설정한 주식 종목 가격 정보 객체 생성
        Long openPrice = randomOpenPrice();
        Long closePrice = randomClosePrice();
        Long highPrice = randomHighPrice();
        Long lowPrice = randomLowPrice();
        Long volume = randomVolume();

        // 시간
        LocalDate priceDate = LocalDate.now();

        // 새로운 주식 종목 가격 정보
        DailyPrice dailyPrice = DailyPrice.builder()
                .stock(saveStock4)
                .openPrice(openPrice)
                .closePrice(closePrice)
                .highPrice(highPrice)
                .lowPrice(lowPrice)
                .volume(volume)
                .priceDate(priceDate)
                .build();

        // 새로운 데이터 삽입
        DailyPrice newData = dailyPriceRepository.save(dailyPrice);

        // 저장된 데이터가 null인지 확인
        assertNotNull(newData);

        // 저장된 데이터가 저장하기 전 데이터와 같은지 확인
        assertEquals(saveStock4.getId(), newData.getStock().getId());
        assertEquals(openPrice, newData.getOpenPrice());
        assertEquals(closePrice, newData.getClosePrice());
        assertEquals(highPrice, newData.getHighPrice());
        assertEquals(lowPrice, newData.getLowPrice());
        assertEquals(volume, newData.getVolume());
        assertEquals(priceDate, newData.getPriceDate());

        // 새로 삽입한 데이터의 PK 값을 리스트에 추가
        pks.add(newData.getId());
    }

    @Order(3)
    @Test
    public void update() {
        // PK 리스트 범위 내에서 랜덤 인덱스 생성
        int index = randomIndex();

        // 주식 종목 가격 조회
        Optional<DailyPrice> byId = dailyPriceRepository.findById(pks.get(index));
        // 주식 종목 가격이 없으면 예외 발생
        DailyPrice newDailyPrice = byId.orElseThrow();

        // 기존 주식 정보 저장
        Stock stock = newDailyPrice.getStock();

        // 기존 수정 시간 저장
        LocalDate beforeUpdatedAt = newDailyPrice.getPriceDate();

        // 랜덤하게 설정된 수정할 주식 종목 가격 객체 생성
        long openPrice = randomOpenPrice();
        long closePrice = randomClosePrice();
        long highPrice = randomHighPrice();
        long lowPrice = randomLowPrice();
        long volume = randomVolume();

        // 주식 종목 가격 정보 수정
        newDailyPrice.update(
                openPrice,
                closePrice,
                highPrice,
                lowPrice,
                volume
        );

        // 수정된 데이터 저장
        DailyPrice save = dailyPriceRepository.save(newDailyPrice);

        // 저장된 데이터가 null인지 확인
        assertNotNull(save);

        // 저장된 데이터가 저장하기 전 데이터와 같은지 확인
        assertEquals(stock.getId(), save.getStock().getId());
        assertEquals(openPrice, save.getOpenPrice());
        assertEquals(closePrice, save.getClosePrice());
        assertEquals(highPrice, save.getHighPrice());
        assertEquals(lowPrice, save.getLowPrice());
        assertEquals(volume, save.getVolume());

        // 수정된 시간이 갱신되었는지 확인
        assertNotNull(save.getPriceDate());
        assertTrue(save.getPriceDate().isAfter(beforeUpdatedAt));
    }

    @Order(4)
    @Test
    public void delete() {
        // 0이상 4미만의 랜덤 인덱스 생성
        int index = randomIndex();

        // 주식 종목 가격 조회 (랜덤하게 만들어진 인덱스 사용)
        Optional<DailyPrice> byId1 = dailyPriceRepository.findById(pks.get(index));
        // 주식 종목 가격이 없으면 예외 발생
        DailyPrice dailyPrice = byId1.orElseThrow();

        // 주식 종목 가격 삭제
        dailyPriceRepository.delete(dailyPrice);

        // 주식 종목 가격 조회 (랜덤하게 만들어진 인덱스 사용)
        Optional<DailyPrice> byId2 = dailyPriceRepository.findById(pks.get(index));
        // 삭제한 데이터가 없는지 확인
        assertTrue(byId2.isEmpty());

        // PK 리스트에 추가된 PK 제거
        pks.remove(index);
    }
}
