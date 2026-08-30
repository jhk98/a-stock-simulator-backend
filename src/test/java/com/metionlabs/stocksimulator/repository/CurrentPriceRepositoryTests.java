//package com.metionlabs.stocksimulator.repository;
//
//import com.metionlabs.stocksimulator.domain.CurrentPrice;
//import com.metionlabs.stocksimulator.domain.DailyPrice;
//import com.metionlabs.stocksimulator.domain.Stock;
//import org.junit.jupiter.api.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static com.metionlabs.stocksimulator.util.TestRandomGenerator.*;
//
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@SpringBootTest
//public class CurrentPriceRepositoryTests {
//
//    @Autowired
//    CurrentPriceRepository currentPriceRepository;
//
//    @Autowired
//    StockRepository stockRepository;
//
//    // PK에 해당되는 id값들의 리스트 생성
//    List<Long> pks = new ArrayList<>();
//
//    // 랜덤 인덱스 생성
//    private int randomIndex() {
//        return (int) (Math.random() * pks.size());
//    }
//
//    // 테스트 준비
//    @BeforeAll
//    public void prepare() {
//        // 테스트용 주식 3개 생성
//        Stock stock1 = Stock.builder()
//                .stockCode("abcd-123")
//                .stockName("삼성전자")
//                .stockType("KOSDAQ")
//                .build();
//
//        Stock stock2 = Stock.builder()
//                .stockCode("efgh-234")
//                .stockName("현대자동차")
//                .stockType("KOSDAQ")
//                .build();
//
//        Stock stock3 = Stock.builder()
//                .stockCode("ijkl-345")
//                .stockName("SK하이닉스")
//                .stockType("KOSDAQ")
//                .build();
//
//        // 주식 데이터 3개 삽입
//        Stock saveStock1 = stockRepository.save(stock1);
//        Stock saveStock2 = stockRepository.save(stock2);
//        Stock saveStock3 = stockRepository.save(stock3);
//
//        // 첫 번째 데이터
//        CurrentPrice currentPrice1 = CurrentPrice.builder()
//                .stock(saveStock1)
//                .currentPrice(randomCurrentPrice())
//                .changePrice(randomChangePrice())
//                .changeRate(randomChangeRate())
//                .currentVolume(randomCurrentVolume())
//                .updatedAt(LocalDateTime.now())
//                .build();
//
//        // 두 번째 데이터
//        CurrentPrice currentPrice2 = CurrentPrice.builder()
//                .stock(saveStock2)
//                .currentPrice(randomCurrentPrice())
//                .changePrice(randomChangePrice())
//                .changeRate(randomChangeRate())
//                .currentVolume(randomCurrentVolume())
//                .updatedAt(LocalDateTime.now())
//                .build();
//
//        // 세 번째 데이터
//        CurrentPrice currentPrice3 = CurrentPrice.builder()
//                .stock(saveStock3)
//                .currentPrice(randomCurrentPrice())
//                .changePrice(randomChangePrice())
//                .changeRate(randomChangeRate())
//                .currentVolume(randomCurrentVolume())
//                .updatedAt(LocalDateTime.now())
//                .build();
//
//        // 세 개의 데이터 삽입
//        CurrentPrice save1 = currentPriceRepository.save(currentPrice1);
//        CurrentPrice save2 = currentPriceRepository.save(currentPrice2);
//        CurrentPrice save3 = currentPriceRepository.save(currentPrice3);
//
//        // 세 개의 데이터에서 PK값들을 리스트에 저장
//        pks.add(save1.getId());
//        pks.add(save2.getId());
//        pks.add(save3.getId());
//    }
//
//    // 테스트 끝
//    @AfterAll
//    public void done() {
//        // 모든 주식 종목 가격 데이터 삭제
//        currentPriceRepository.deleteAll();
//        // 전체 데이터 조회
//        List<CurrentPrice> currentPrices = currentPriceRepository.findAll();
//        // 주식 종목 가격 데이터가 없는지 확인
//        assertTrue(currentPrices.isEmpty());
//
//        // 모든 주식 데이터 삭제
//        stockRepository.deleteAll();
//        // 전체 데이터 조회
//        List<Stock> stocks = stockRepository.findAll();
//        // 주식 데이터가 없는지 확인
//        assertTrue(stocks.isEmpty());
//    }
//
//    // crud
//    @Order(1)
//    @Test
//    public void findAll() {
//        // 테스트 준비에 넣은 데이터들을 리스트로 만들기
//        List<CurrentPrice> testData = currentPriceRepository.findAll();
//        // 조회된 데이터가 존재하는지 확인
//        assertEquals(3, testData.size());
//    }
//
//    @Order(2)
//    @Test
//    public void insert() {
//        // 주식 정보 추가
//        Stock stock4 = Stock.builder()
//                .stockCode("mnop-456")
//                .stockName("LG전자")
//                .stockType("KOSDAQ")
//                .build();
//
//        // 주식 정보 저장
//        Stock saveStock4 = stockRepository.save(stock4);
//
//        // 랜덤 설정한 주식 현재가 정보 객체 생성
//        Long currentPrice = randomCurrentPrice();
//        Long changePrice = randomChangePrice();
//        Double changeRate = randomChangeRate();
//        Long currentVolume = randomCurrentVolume();
//
//        // 시간
//        LocalDateTime updatedAt = LocalDateTime.now();
//
//        // 새로운 주식 현재가 정보
//        CurrentPrice newCurrentPrice = CurrentPrice.builder()
//                .stock(saveStock4)
//                .currentPrice(currentPrice)
//                .changePrice(changePrice)
//                .changeRate(changeRate)
//                .currentVolume(currentVolume)
//                .updatedAt(updatedAt)
//                .build();
//
//        // 새로운 데이터 삽입
//        CurrentPrice newData = currentPriceRepository.save(newCurrentPrice);
//
//        // 저장된 데이터가 null인지 확인
//        assertNotNull(newData);
//
//        // 저장된 데이터가 저장하기 전 데이터와 같은지 확인
//        assertEquals(stock4.getId(), newData.getStock().getId());
//        assertEquals(currentPrice, newData.getCurrentPrice());
//        assertEquals(changePrice, newData.getChangePrice());
//        assertEquals(changeRate, newData.getChangeRate());
//        assertEquals(currentVolume, newData.getCurrentVolume());
//        assertEquals(updatedAt, newData.getUpdatedAt());
//
//        // 새로 삽입한 데이터의 PK 값을 리스트에 추가
//        pks.add(newData.getId());
//    }
//
//    @Order(3)
//    @Test
//    public void update() {
//        // 0이상 4미만의 랜덤 인덱스 생성
//        int index = randomIndex();
//
//        // 주식 현재가 조회
//        Optional<CurrentPrice> byId = currentPriceRepository.findById(pks.get(index));
//        // 주식 현재가가 없으면 예외 발생
//        CurrentPrice newCurrentPrice = byId.orElseThrow();
//
//        // 가격 기록 시간 생성
//        LocalDateTime updatedAt = LocalDateTime.now();
//
//        // 기존 주식 정보 저장
//        Stock stock = newCurrentPrice.getStock();
//
//        // 랜덤하게 설정된 수정할 주식 현재가 객체 생성
//        long currentPrice = randomCurrentPrice();
//        long changePrice = randomChangePrice();
//        double changeRate = randomChangeRate();
//        long currentVolume = randomCurrentVolume();
//
//        // 주식 현재가 정보 수정
//        newCurrentPrice.setCurrentPrice(currentPrice);
//        newCurrentPrice.setChangePrice(changePrice);
//        newCurrentPrice.setChangeRate(changeRate);
//        newCurrentPrice.setCurrentVolume(currentVolume);
//        newCurrentPrice.setUpdatedAt(updatedAt);
//
//        // 새로운 데이터 삽입
//        CurrentPrice save = currentPriceRepository.save(newCurrentPrice);
//
//        // 저장된 데이터가 null인지 확인
//        assertNotNull(save);
//
//        // 저장된 데이터가 저장하기 전 데이터와 같은지 확인
//        assertEquals(stock.getId(), save.getStock().getId());
//        assertEquals(currentPrice, save.getCurrentPrice());
//        assertEquals(changePrice, save.getChangePrice());
//        assertEquals(changeRate, save.getChangeRate());
//        assertEquals(currentVolume, save.getCurrentVolume());
//        assertEquals(updatedAt, save.getUpdatedAt());
//    }
//
//    @Order(4)
//    @Test
//    public void delete() {
//        // 0이상 4미만의 랜덤 인덱스 생성
//        int index = randomIndex();
//
//        // 주식 종목 가격 조회 (랜덤하게 만들어진 인덱스 사용)
//        Optional<CurrentPrice> byId1 = currentPriceRepository.findById(pks.get(index));
//        // 주식 종목 가격이 없으면 예외 발생
//        CurrentPrice currentPrice = byId1.orElseThrow();
//
//        // 주식 종목 가격 삭제
//        currentPriceRepository.delete(currentPrice);
//
//        // 주식 종목 가격 조회 (랜덤하게 만들어진 인덱스 사용)
//        Optional<CurrentPrice> byId2 = currentPriceRepository.findById(pks.get(index));
//        // 삭제한 데이터가 없는지 확인
//        assertTrue(byId2.isEmpty());
//
//        // PK 리스트에 추가된 PK 제거
//        pks.remove(index);
//    }
//}