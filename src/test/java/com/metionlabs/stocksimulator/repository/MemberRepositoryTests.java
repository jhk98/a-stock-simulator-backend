package com.metionlabs.stocksimulator.repository;

import com.metionlabs.stocksimulator.domain.Member;
import com.metionlabs.stocksimulator.util.TestRandomGenerator;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static com.metionlabs.stocksimulator.util.TestRandomGenerator.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
public class MemberRepositoryTests {

    @Autowired
    MemberRepository memberRepository;

    // PK에 해당되는 id값들의 리스트 생성
    List<Long> pks = new ArrayList<>();

    // 랜덤 인덱스 생성
    private int randomIndex() {
        return (int) (Math.random() * pks.size());
    }

    // 테스트 준비
    @BeforeAll
    public void prepare() {
        // 첫 번째 데이터
        Member member1 = Member.builder()
                .loginId(randomLoginId())
                .name(randomName())
                .email(randomEmail())
                .phoneNumber(randomPhoneNumber())
                .rgstNumber(randomRgstNumber())
                .passwd(randomPasswd())
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();

        // 두 번째 데이터
        Member member2 = Member.builder()
                .loginId(randomLoginId())
                .name(randomName())
                .email(randomEmail())
                .phoneNumber(randomPhoneNumber())
                .rgstNumber(randomRgstNumber())
                .passwd(randomPasswd())
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();

        // 세 번째 데이터
        Member member3 = Member.builder()
                .loginId(randomLoginId())
                .name(randomName())
                .email(randomEmail())
                .phoneNumber(randomPhoneNumber())
                .rgstNumber(randomRgstNumber())
                .passwd(randomPasswd())
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();

        // 세 개의 데이터 삽입
        Member save1 = memberRepository.save(member1);
        Member save2 = memberRepository.save(member2);
        Member save3 = memberRepository.save(member3);

        // 세 개의 데이터에서 PK값들을 리스트에 저장
        pks.add(save1.getId());
        pks.add(save2.getId());
        pks.add(save3.getId());
    }

    // 테스트 끝
    @AfterAll
    public void done() {
        // 모든 데이터 삭제
        memberRepository.deleteAll();

        // 전체 데이터 조회
        List<Member> members = memberRepository.findAll();
        // 삭제한 데이터가 없는지 확인
        assertTrue(members.isEmpty());
    }

    // crud
    @Order(1)
    @Test
    public void findAll() {
        // 테스트 준비에 넣은 데이터들을 리스트로 만들기
        List<Member> testData = memberRepository.findAll();
        // 조회된 데이터가 존재하는지 확인
        assertEquals(3, testData.size());
    }

    @Order(2)
    @Test
    public void insert() {
        // 랜덤 설정한 회원 정보 객체 생성
        String loginId = randomLoginId();
        String name = randomName();
        String email = randomEmail();
        String phoneNumber = randomPhoneNumber();
        String rgstNumber = randomRgstNumber();
        String passwd = randomPasswd();

        // 시간
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime modifiedAt = LocalDateTime.now();

        // 새로운 회원 정보
        Member member = Member.builder()
                .loginId(loginId)
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .rgstNumber(rgstNumber)
                .passwd(passwd)
                .createdAt(createdAt)
                .modifiedAt(modifiedAt)
                .build();

        // 새로운 데이터 삽입
        Member newData = memberRepository.save(member);

        // 저장된 데이터가 null인지 확인
        assertNotNull(newData);

        // 저장된 데이터가 저장하기 전 데이터와 같은지 확인
        assertEquals(loginId, newData.getLoginId());
        assertEquals(name, newData.getName());
        assertEquals(email, newData.getEmail());
        assertEquals(phoneNumber, newData.getPhoneNumber());
        assertEquals(rgstNumber, newData.getRgstNumber());
        assertEquals(passwd, newData.getPasswd());
        assertEquals(createdAt, newData.getCreatedAt());
        assertEquals(modifiedAt, newData.getModifiedAt());

        // 새로 삽입한 데이터의 PK 값을 리스트에 추가
        pks.add(newData.getId());
    }

    @Order(3)
    @Test
    public void update() {
        // 0이상 4미만의 랜덤 인덱스 생성
        int index = randomIndex();

        // 회원 조회
        Optional<Member> byId = memberRepository.findById(pks.get(index));
        // 회원이 없으면 예외 발생
        Member member = byId.orElseThrow();

        // 생성일 저장
        LocalDateTime createdAt = member.getCreatedAt();

        // 수정일 생성
        LocalDateTime modifiedAt = LocalDateTime.now();

        // 랜덤하게 설정된 수정할 회원 정보 객체 생성
        String loginId = randomLoginId();
        String name = randomName();
        String email = randomEmail();
        String phoneNumber = randomPhoneNumber();
        String rgstNumber = randomRgstNumber();
        String passwd = randomPasswd();

        // 회원 정보 수정
        member.setLoginId(loginId);
        member.setName(name);
        member.setEmail(email);
        member.setPhoneNumber(phoneNumber);
        member.setRgstNumber(rgstNumber);
        member.setPasswd(passwd);
        member.setModifiedAt(modifiedAt);

        // 새로운 데이터 삽입
        Member save = memberRepository.save(member);

        // 저장된 데이터가 null인지 확인
        assertNotNull(save);

        // 저장된 데이터가 저장하기 전 데이터와 같은지 확인
        assertEquals(loginId, save.getLoginId());
        assertEquals(name, save.getName());
        assertEquals(email, save.getEmail());
        assertEquals(phoneNumber, save.getPhoneNumber());
        assertEquals(rgstNumber, save.getRgstNumber());
        assertEquals(passwd, save.getPasswd());
        assertEquals(createdAt, save.getCreatedAt());
        assertEquals(modifiedAt, save.getModifiedAt());
    }

    @Order(4)
    @Test
    public void delete() {
        // 0이상 4미만의 랜덤 인덱스 생성
        int index = randomIndex();

        // 회원 조회 (랜덤하게 만들어진 인덱스 사용)
        Optional<Member> byId1 = memberRepository.findById(pks.get(index));
        // 회원이 없으면 예외 발생
        Member member = byId1.orElseThrow();

        // 회원 삭제
        memberRepository.delete(member);

        // 회원 조회 (랜덤하게 만들어진 인덱스 사용)
        Optional<Member> byId2 = memberRepository.findById(pks.get(index));
        // 삭제한 데이터가 없는지 확인
        assertTrue(byId2.isEmpty());

        // PK 리스트에 추가된 PK 제거
        pks.remove(index);
    }
}