package com.myproject.stocksimulator.repository;

import com.myproject.stocksimulator.domain.Account;
import com.myproject.stocksimulator.domain.Holding;
import com.myproject.stocksimulator.domain.member.MemberRole;
import com.myproject.stocksimulator.domain.member.MemberStatus;
import com.myproject.stocksimulator.domain.stock.Stock;
import com.myproject.stocksimulator.domain.member.Member;
import com.myproject.stocksimulator.domain.stock.StockType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static com.myproject.stocksimulator.util.TestRandomGenerator.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
public class HoldingRepositoryTests {

    @Autowired
    HoldingRepository holdingRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    StockRepository stockRepository;

    @Autowired
    AccountRepository accountRepository;

    // PK에 해당되는 id값들의 리스트 생성
    List<Long> pks = new ArrayList<>();

    // 랜덤 인덱스 생성
    private int randomIndex() {
        return (int) (Math.random() * pks.size());
    }

    // 테스트 준비
    @BeforeAll
    public void prepare() {
        // 테스트용 회원 3개 생성
        Member member1 = Member.builder()
                .loginId("test123")
                .name("테스트1")
                .email("test01@naver.com")
                .phoneNumber("010-1234-1234")
                .rgstNumber("980101-1011111")
                .passwd("test1234")
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.USER)
                .lastAccessTime(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();

        Member member2 = Member.builder()
                .loginId("test234")
                .name("테스트2")
                .email("test02@naver.com")
                .phoneNumber("010-2345-2345")
                .rgstNumber("980202-1022222")
                .passwd("test2345")
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.USER)
                .lastAccessTime(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();

        Member member3 = Member.builder()
                .loginId("test345")
                .name("테스트3")
                .email("test03@naver.com")
                .phoneNumber("010-3456-3456")
                .rgstNumber("980303-1033333")
                .passwd("test3456")
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.USER)
                .lastAccessTime(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();

        // 회원 데이터 3개 삽입
        Member saveMember1 = memberRepository.save(member1);
        Member saveMember2 = memberRepository.save(member2);
        Member saveMember3 = memberRepository.save(member3);

        // 테스트용 계좌 3개 생성
        Account account1 = Account.builder()
                .member(saveMember1)
                .accountNum("1111-11-1111111")
                .deposit(1234567L)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();

        // 두 번째 데이터
        Account account2 = Account.builder()
                .member(saveMember2)
                .accountNum("2222-22-2222222")
                .deposit(1234567L)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();

        // 세 번째 데이터
        Account account3 = Account.builder()
                .member(saveMember3)
                .accountNum("3333-33-3333333")
                .deposit(1234567L)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();

        // 계좌 데이터 3개 삽입
        Account saveAccount1 = accountRepository.save(account1);
        Account saveAccount2 = accountRepository.save(account2);
        Account saveAccount3 = accountRepository.save(account3);

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
        Holding holding1 = Holding.builder()
                .stock(saveStock1)
                .account(saveAccount1)
                .quantity(randomQuantityH())
                .avgPrice(randomAvgPrice())
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();

        // 두 번째 데이터
        Holding holding2 = Holding.builder()
                .stock(saveStock2)
                .account(saveAccount2)
                .quantity(randomQuantityH())
                .avgPrice(randomAvgPrice())
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();

        // 세 번째 데이터
        Holding holding3 = Holding.builder()
                .stock(saveStock3)
                .account(saveAccount3)
                .quantity(randomQuantityH())
                .avgPrice(randomAvgPrice())
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();

        // 세 개의 데이터 삽입
        Holding save1 = holdingRepository.save(holding1);
        Holding save2 = holdingRepository.save(holding2);
        Holding save3 = holdingRepository.save(holding3);

        // 세 개의 데이터에서 PK값들을 리스트에 저장
        pks.add(save1.getId());
        pks.add(save2.getId());
        pks.add(save3.getId());
    }

    // 테스트 끝
    @AfterAll
    public void done() {
        // 모든 보유 주식 데이터 삭제
        holdingRepository.deleteAll();
        // 전체 데이터 조회
        List<Holding> holdings = holdingRepository.findAll();
        // 보유 주식 데이터가 없는지 확인
        assertTrue(holdings.isEmpty());

        // 모든 주식 데이터 삭제
        stockRepository.deleteAll();
        // 전체 데이터 조회
        List<Stock> stocks = stockRepository.findAll();
        // 주식 데이터가 없는지 확인
        assertTrue(stocks.isEmpty());

        // 모든 계좌 데이터 삭제
        accountRepository.deleteAll();
        // 전체 데이터 조회
        List<Account> accounts = accountRepository.findAll();
        // 계좌 데이터가 없는지 확인
        assertTrue(accounts.isEmpty());

        // 모든 회원 데이터 삭제
        memberRepository.deleteAll();
        // 전체 데이터 조회
        List<Member> members = memberRepository.findAll();
        // 회원 데이터가 없는지 확인
        assertTrue(members.isEmpty());
    }

    // crud
    @Order(1)
    @Test
    public void findAll() {
        // 테스트 준비에 넣은 데이터들을 리스트로 만들기
        List<Holding> testData = holdingRepository.findAll();
        // 조회된 데이터가 존재하는지 확인
        assertEquals(3, testData.size());
    }

    @Order(2)
    @Test
    public void insert() {
        // 회원 정보 추가
        Member member4 = Member.builder()
                .loginId("test456")
                .name("테스트4")
                .email("test04@naver.com")
                .phoneNumber("010-4567-4567")
                .rgstNumber("980404-1044444")
                .passwd("test4567")
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.USER)
                .lastAccessTime(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();

        // 회원 정보 저장
        Member saveMember4 = memberRepository.save(member4);

        // 계좌 정보 추가
        Account account4 = Account.builder()
                .member(saveMember4)
                .accountNum("4444-44-4444444")
                .deposit(1234567L)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();

        // 계좌 정보 저장
        Account saveAccount4 = accountRepository.save(account4);

        // 주식 정보 추가
        Stock stock4 = Stock.builder()
                .stockCode("mnop-456")
                .stockName("LG전자")
                .stockType(StockType.KOSDAQ)
                .build();

        // 주식 정보 저장
        Stock saveStock4 = stockRepository.save(stock4);

        // 랜덤 설정한 보유 주식 정보 객체 생성
        int quantity = randomQuantityH();
        Long avgPrice = randomAvgPrice();

        // 시간
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime modifiedAt = LocalDateTime.now();

        // 새로운 보유 주식 정보
        Holding holding = Holding.builder()
                .stock(saveStock4)
                .account(saveAccount4)
                .quantity(quantity)
                .avgPrice(avgPrice)
                .createdAt(createdAt)
                .modifiedAt(modifiedAt)
                .build();

        // 새로운 데이터 삽입
        Holding newData = holdingRepository.save(holding);

        // 저장된 데이터가 null인지 확인
        assertNotNull(newData);

        // 저장된 데이터가 저장하기 전 데이터와 같은지 확인
        assertEquals(saveStock4.getId(), newData.getStock().getId());
        assertEquals(account4.getId(), newData.getAccount().getId());
        assertEquals(quantity, newData.getQuantity());
        assertEquals(avgPrice, newData.getAvgPrice());
        assertEquals(createdAt, newData.getCreatedAt());
        assertEquals(modifiedAt, newData.getModifiedAt());

        // 새로 삽입한 데이터의 PK 값을 리스트에 추가
        pks.add(newData.getId());
    }

    @Order(3)
    @Test
    public void update() {
        // PK 리스트 범위 내에서 랜덤 인덱스 생성
        int index = randomIndex();

        // 보유 주식 조회
        Optional<Holding> byId = holdingRepository.findById(pks.get(index));
        // 보유 주식 없으면 예외 발생
        Holding holding = byId.orElseThrow();

        // 생성일 저장
        LocalDateTime createdAt = holding.getCreatedAt();

        // 기존 주식 정보 저장
        Stock stock = holding.getStock();

        // 기존 계좌 정보 저장
        Account account = holding.getAccount();

        // 랜덤하게 설정된 수정할 보유 주식 정보 객체 생성
        int quantity = randomQuantityH();
        Long avgPrice = randomAvgPrice();

        // 보유 주식 정보 수정
        holding.update(quantity, avgPrice);

        // 새로운 데이터 삽입
        Holding save = holdingRepository.save(holding);

        // 저장된 데이터가 null인지 확인
        assertNotNull(save);

        // 저장된 데이터가 저장하기 전 데이터와 같은지 확인
        assertEquals(stock.getId(), save.getStock().getId());
        assertEquals(account.getId(), save.getAccount().getId());
        assertEquals(quantity, save.getQuantity());
        assertEquals(avgPrice, save.getAvgPrice());
        assertEquals(createdAt, save.getCreatedAt());

        // ⚠️ 수정일은 update() 안에서 now() 로 새로 찍히므로,
        //    테스트가 미리 만들어 둔 modifiedAt 과 같을 수 없다.
        //    "언제 찍혔는지" 를 비교하지 말고 **갱신되었는지**를 본다.
        assertNotNull(save.getModifiedAt());
        assertTrue(save.getModifiedAt().isAfter(createdAt)
                || save.getModifiedAt().isEqual(createdAt));
    }

    @Order(4)
    @Test
    public void delete() {
        // 0이상 4미만의 랜덤 인덱스 생성
        int index = randomIndex();

        // 보유 주식 조회 (랜덤하게 만들어진 인덱스 사용)
        Optional<Holding> byId1 = holdingRepository.findById(pks.get(index));
        // 보유 주식 없으면 예외 발생
        Holding holding = byId1.orElseThrow();

        // 보유 주식 삭제
        holdingRepository.delete(holding);

        // 보유 주식 조회 (랜덤하게 만들어진 인덱스 사용)
        Optional<Holding> byId2 = holdingRepository.findById(pks.get(index));
        // 삭제한 데이터가 없는지 확인
        assertTrue(byId2.isEmpty());

        // PK 리스트에 추가된 PK 제거
        pks.remove(index);
    }
}