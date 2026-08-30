package com.metionlabs.stocksimulator.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Table(name="a_stock_member")
@Entity
@Getter
@Builder
@AllArgsConstructor
public class Member {

    // 멤버 테이블 아이디
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    // 로그인 아이디
    @Column(name="login_id")
    private String loginId;

    // 이름
    @Column(name="name")
    private String name;

    // 이메일
    @Column(name="email")
    private String email;

    // 전화번호
    @Column(name="phone_number")
    private String phoneNumber;

    // 주민등록번호
    @Column(name="rgst_number")
    private String rgstNumber;

    // 비밀번호
    @Column(name="passwd")
    private String passwd;

    // 생성일
    @Column(name="created_at")
    private LocalDateTime createdAt;

    // 수정일
    @Column(name="modified_at")
    private LocalDateTime modifiedAt;

    // JPA가 DB 데이터를 엔티티 객체로 생성할 때 사용하는 기본 생성자
    // 외부에서 빈 객체를 직접 생성하지 못하도록 protected로 선언
    protected Member() {
    }

    // 회원 정보 수정 메서드
    public void update(String email, String phoneNumber, String passwd) {
        if(email != null) {
            this.email = email;
        }
        if(phoneNumber != null) {
            this.phoneNumber = phoneNumber;
        }
        if(passwd != null) {
            this.passwd = passwd;
        }
        this.modifiedAt = LocalDateTime.now();
    }
}