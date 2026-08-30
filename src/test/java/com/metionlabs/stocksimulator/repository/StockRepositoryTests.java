//package com.metionlabs.stocksimulator.repository;
//
//import com.metionlabs.stocksimulator.domain.Stock;
//import org.junit.jupiter.api.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
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
//public class StockRepositoryTests {
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
//        // 첫 번째 데이터
//        Stock stock1 = Stock.builder()
//                .stockCode(randomStockCode())
//                .stockName(randomStockName())
//                .stockType(randomStockType())
//                .build();
//
//        // 두 번째 데이터
//        Stock stock2 = Stock.builder()
//                .stockCode(randomStockCode())
//                .stockName(randomStockName())
//                .stockType(randomStockType())
//                .build();
//
//        // 세 번째 데이터
//        Stock stock3 = Stock.builder()
//                .stockCode(randomStockCode())
//                .stockName(randomStockName())
//                .stockType(randomStockType())
//                .build();
//
//        // 세 개의 데이터 삽입
//        Stock save1 = stockRepository.save(stock1);
//        Stock save2 = stockRepository.save(stock2);
//        Stock save3 = stockRepository.save(stock3);
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
//        // 모든 데이터 삭제
//        stockRepository.deleteAll();
//
//        // 전체 데이터 조회
//        List<Stock> stocks = stockRepository.findAll();
//        // 삭제한 데이터가 없는지 확인
//        assertTrue(stocks.isEmpty());
//    }
//
//    // crud
//    @Order(1)
//    @Test
//    public void findAll() {
//        // 테스트 준비에 넣은 데이터들을 리스트로 만들기
//        List<Stock> testData = stockRepository.findAll();
//        // 조회된 데이터가 존재하는지 확인
//        assertEquals(3, testData.size());
//    }
//
//    @Order(2)
//    @Test
//    public void insert() {
//        // 랜덤 설정한 주식 정보 객체 생성
//        String stockCode = randomStockCode();
//        String stockName = randomStockName();
//        String stockType = randomStockType();
//
//        // 새로운 주식 정보
//        Stock stock = Stock.builder()
//                .stockCode(stockCode)
//                .stockName(stockName)
//                .stockType(stockType)
//                .build();
//
//        // 새로운 데이터 삽입
//        Stock newData = stockRepository.save(stock);
//
//        // 저장된 데이터가 null인지 확인
//        assertNotNull(newData);
//
//        // 저장된 데이터가 저장하기 전 데이터와 같은지 확인
//        assertEquals(stockCode, newData.getStockCode());
//        assertEquals(stockName, newData.getStockName());
//        assertEquals(stockType, newData.getStockType());
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
//        // 주식 조회 (랜덤하게 만들어진 인덱스 사용)
//        Optional<Stock> byId = stockRepository.findById(pks.get(index));
//        // 주식이 없으면 예외 발생
//        Stock stock = byId.orElseThrow();
//
//        // 랜덤하게 설정된 수정할 주식 정보 객체 생성
//        String stockCode = randomStockCode();
//        String stockName = randomStockName();
//        String stockType = randomStockType();
//
//        // 주식 정보 수정
//        stock.setStockCode(stockCode);
//        stock.setStockName(stockName);
//        stock.setStockType(stockType);
//
//        // 새로운 데이터 삽입
//        Stock save = stockRepository.save(stock);
//
//        // 저장된 데이터가 null인지 확인
//        assertNotNull(save);
//
//        // 저장된 데이터가 저장하기 전 데이터와 같은지 확인
//        assertEquals(stockCode, save.getStockCode());
//        assertEquals(stockName, save.getStockName());
//        assertEquals(stockType, save.getStockType());
//    }
//
//    @Order(4)
//    @Test
//    public void delete() {
//        // 0이상 4미만의 랜덤 인덱스 생성
//        int index = randomIndex();
//
//        // 주식 조회 (랜덤하게 만들어진 인덱스 사용)
//        Optional<Stock> byId1 = stockRepository.findById(pks.get(index));
//        // 주식이 없으면 예외 발생
//        Stock stock = byId1.orElseThrow();
//
//        // 회원 삭제
//        stockRepository.delete(stock);
//
//        // 주식 조회 (랜덤하게 만들어진 인덱스 사용)
//        Optional<Stock> byId2 = stockRepository.findById(pks.get(index));
//        // 삭제한 데이터가 없는지 확인
//        assertTrue(byId2.isEmpty());
//
//        // PK 리스트에 추가된 PK 제거
//        pks.remove(index);
//    }
//}