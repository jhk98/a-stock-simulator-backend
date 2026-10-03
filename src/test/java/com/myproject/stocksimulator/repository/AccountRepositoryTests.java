//package com.myproject.stocksimulator.repository;
//
//import com.myproject.stocksimulator.domain.Account;
//import com.myproject.stocksimulator.domain.member.Member;
//import com.myproject.stocksimulator.domain.member.MemberRole;
//import com.myproject.stocksimulator.domain.member.MemberStatus;
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
//import static com.myproject.stocksimulator.util.TestRandomGenerator.*;
//
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@SpringBootTest
//public class AccountRepositoryTests {
//
//    @Autowired
//    AccountRepository accountRepository;
//
//    @Autowired
//    MemberRepository memberRepository;
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
//        // 테스트용 회원 3개 생성
//        Member member1 = Member.builder()
//                .loginId("test123")
//                .name("테스트1")
//                .email("test01@naver.com")
//                .phoneNumber("010-1234-1234")
//                .rgstNumber("980101-1011111")
//                .passwd("test1234")
//                .status(MemberStatus.ACTIVE)
//                .role(MemberRole.USER)
//                .lastAccessTime(LocalDateTime.now())
//                .createdAt(LocalDateTime.now())
//                .modifiedAt(LocalDateTime.now())
//                .build();
//
//        Member member2 = Member.builder()
//                .loginId("test234")
//                .name("테스트2")
//                .email("test02@naver.com")
//                .phoneNumber("010-2345-2345")
//                .rgstNumber("980202-1022222")
//                .passwd("test2345")
//                .status(MemberStatus.ACTIVE)
//                .role(MemberRole.USER)
//                .lastAccessTime(LocalDateTime.now())
//                .createdAt(LocalDateTime.now())
//                .modifiedAt(LocalDateTime.now())
//                .build();
//
//        Member member3 = Member.builder()
//                .loginId("test345")
//                .name("테스트3")
//                .email("test03@naver.com")
//                .phoneNumber("010-3456-3456")
//                .rgstNumber("980303-1033333")
//                .passwd("test3456")
//                .status(MemberStatus.ACTIVE)
//                .role(MemberRole.USER)
//                .lastAccessTime(LocalDateTime.now())
//                .createdAt(LocalDateTime.now())
//                .modifiedAt(LocalDateTime.now())
//                .build();
//
//        // 회원 데이터 3개 삽입
//        Member saveMember1 = memberRepository.save(member1);
//        Member saveMember2 = memberRepository.save(member2);
//        Member saveMember3 = memberRepository.save(member3);
//
//        // 첫 번째 데이터
//        Account account1 = Account.builder()
//                .member(saveMember1)
//                .accountNum(randomAccountNum())
//                .deposit(randomDeposit())
//                .createdAt(LocalDateTime.now())
//                .modifiedAt(LocalDateTime.now())
//                .build();
//
//        // 두 번째 데이터
//        Account account2 = Account.builder()
//                .member(saveMember2)
//                .accountNum(randomAccountNum())
//                .deposit(randomDeposit())
//                .createdAt(LocalDateTime.now())
//                .modifiedAt(LocalDateTime.now())
//                .build();
//
//        // 세 번째 데이터
//        Account account3 = Account.builder()
//                .member(saveMember3)
//                .accountNum(randomAccountNum())
//                .deposit(randomDeposit())
//                .createdAt(LocalDateTime.now())
//                .modifiedAt(LocalDateTime.now())
//                .build();
//
//        // 세 개의 데이터 삽입
//        Account save1 = accountRepository.save(account1);
//        Account save2 = accountRepository.save(account2);
//        Account save3 = accountRepository.save(account3);
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
//        // 모든 계좌 데이터 삭제
//        accountRepository.deleteAll();
//        // 전체 데이터 조회
//        List<Account> accounts = accountRepository.findAll();
//        // 계좌 데이터가 없는지 확인
//        assertTrue(accounts.isEmpty());
//
//        // 모든 회원 데이터 삭제
//        memberRepository.deleteAll();
//        // 전체 데이터 조회
//        List<Member> members = memberRepository.findAll();
//        // 회원 데이터가 없는지 확인
//        assertTrue(members.isEmpty());
//    }
//
//    //crud
//    @Order(1)
//    @Test
//    public void findAll() {
//        // 테스트 준비에 넣은 데이터들을 리스트로 만들기
//        List<Account> testData = accountRepository.findAll();
//        // 조회된 데이터가 존재하는지 확인
//        assertEquals(3, testData.size());
//    }
//
//    @Order(2)
//    @Test
//    public void insert() {
//        // 회원 정보 추가
//        Member member4 = Member.builder()
//                .loginId("test456")
//                .name("테스트4")
//                .email("test04@naver.com")
//                .phoneNumber("010-4567-4567")
//                .rgstNumber("980404-1044444")
//                .passwd("test4567")
//                .status(MemberStatus.ACTIVE)
//                .role(MemberRole.USER)
//                .lastAccessTime(LocalDateTime.now())
//                .createdAt(LocalDateTime.now())
//                .modifiedAt(LocalDateTime.now())
//                .build();
//
//        // 회원 정보 저장
//        Member saveMember4 = memberRepository.save(member4);
//
//        // 랜덤 설정한 계좌 정보 객체 생성
//        String accountNum = randomAccountNum();
//        Long deposit = randomDeposit();
//
//        // 시간
//        LocalDateTime createdAt = LocalDateTime.now();
//        LocalDateTime modifiedAt = LocalDateTime.now();
//
//        // 새로운 계좌 정보
//        Account account = Account.builder()
//                .member(saveMember4)
//                .accountNum(accountNum)
//                .deposit(deposit)
//                .createdAt(createdAt)
//                .modifiedAt(modifiedAt)
//                .build();
//
//        // 새로운 데이터 삽입
//        Account newData = accountRepository.save(account);
//
//        // 저장된 데이터가 null인지 확인
//        assertNotNull(newData);
//
//        // 저장된 데이터가 저장히기 전 데이터와 같은지 확인
//        assertEquals(member4.getId(), newData.getMember().getId());
//        assertEquals(accountNum, newData.getAccountNum());
//        assertEquals(deposit, newData.getDeposit());
//        assertEquals(createdAt, newData.getCreatedAt());
//        assertEquals(modifiedAt, newData.getModifiedAt());
//
//        // 새로 삽입한 데이터의 PK 값을 리스트에 추가
//        pks.add(newData.getId());
//    }
//
//    @Order(3)
//    @Test
//    public void update() {
//        // PK 리스트 범위 내에서 랜덤 인덱스 생성
//        int index = randomIndex();
//
//        // 계좌 조회
//        Optional<Account> byId = accountRepository.findById(pks.get(index));
//        // 계좌 없으면 예외 발생
//        Account account = byId.orElseThrow();
//
//        // 생성일 저장
//        LocalDateTime createdAt = account.getCreatedAt();
//
//        // (수정일은 update() 가 스스로 찍으므로 여기서 만들지 않는다)
//
//        // 기존 회원 정보 저장
//        Member member = account.getMember();
//
//        // 랜덤하게 설정된 수정할 계좌 정보 객체 생성
//        String accountNum = randomAccountNum();
//        Long deposit = randomDeposit();
//
//        // 🐛 이 테스트는 원래 깨져 있었다.
//        //    랜덤 값을 만들어놓고 **계좌에 적용하지 않은 채** 같은지 검증해서
//        //    assertEquals(accountNum, save.getAccountNum()) 이 항상 실패했다.
//        //    값을 바꾸는 코드가 빠져 있었던 것 — 이제 엔티티가 스스로 바꾼다.
//        account.update(accountNum, deposit);
//
//        // 새로운 데이터 삽입
//        Account save = accountRepository.save(account);
//
//        // 저장된 데이터가 null인지 확인
//        assertNotNull(save);
//
//        // 저장된 데이터가 수정한 값과 같은지 확인
//        assertEquals(member.getId(), save.getMember().getId());
//        assertEquals(accountNum, save.getAccountNum());
//        assertEquals(deposit, save.getDeposit());
//
//        // 생성일은 수정해도 그대로여야 한다
//        assertEquals(createdAt, save.getCreatedAt());
//
//        // ⚠️ 수정일은 update() 안에서 now() 로 새로 찍히므로,
//        //    테스트가 미리 만들어 둔 modifiedAt 과 같을 수 없다.
//        //    "언제 찍혔는지" 를 비교하지 말고 **갱신되었는지**를 본다.
//        assertNotNull(save.getModifiedAt());
//        assertTrue(save.getModifiedAt().isAfter(createdAt)
//                || save.getModifiedAt().isEqual(createdAt));
//    }
//
//    @Order(4)
//    @Test
//    public void delete() {
//        // 0이상 4미만의 랜덤 인덱스 생성
//        int index = randomIndex();
//
//        // 계좌 조회 (랜덤하게 만들어진 인덱스 사용)
//        Optional<Account> byId1 = accountRepository.findById(pks.get(index));
//        // 계좌 없으면 예외 발생
//        Account account = byId1.orElseThrow();
//
//        // 계좌 삭제
//        accountRepository.delete(account);
//
//        // 계좌 조회 (랜덤하게 만들어진 인덱스 사용)
//        Optional<Account> byId2 = accountRepository.findById(pks.get(index));
//        // 삭제한 데이터가 없는지 확인
//        assertTrue(byId2.isEmpty());
//
//        // PK 리스트에 추가된 PK 제거
//        pks.remove(index);
//    }
//}