package com.metionlabs.stocksimulator.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Table(name="a_stock_member")
@Entity
@Getter
@Setter
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

    protected Member() {
    }

}