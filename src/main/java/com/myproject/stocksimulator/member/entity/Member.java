package com.myproject.stocksimulator.member.entity;

import jakarta.persistence.*;

import lombok.Builder;
import lombok.Getter;

import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

/**
 * 회원정보
 *
 * <p>
 * 회원가입과 로그인에서 사용하는 기존 필드를 유지한다.
 * 비밀번호는 해시 처리된 값만 저장한다.
 * </p>
 */
@Entity
@Table(name = "a_stock_member")
@Getter
@DynamicUpdate
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "login_id", nullable = false, unique = true, length = 50)
    private String loginId;

    // 평문 비밀번호가 아닌 BCrypt 해시값.
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "name", nullable = false, length = 10)
    private String name;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "last_access_time")
    private LocalDateTime lastAccessTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "modified_at", nullable = false)
    private LocalDateTime modifiedAt;

    // JPA가 조회한 데이터를 엔티티로 만들 때 사용한다.
    protected Member() {
    }

    // 기존 SignupService의 회원가입 빌더 호출을 유지한다.
    @Builder
    private Member(
            String loginId,
            String password,
            String name,
            String phoneNumber,
            String email
    ) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    /**
     * 서비스에서 해시 처리한 새 비밀번호를 반영한다.
     */
    public void changePassword(String encodedPassword) {
        if (encodedPassword == null || encodedPassword.isBlank()) {
            throw new IllegalArgumentException("비밀번호 해시값이 필요합니다.");
        }

        this.password = encodedPassword;
    }

    /**
     * 서비스에서 검증하고 형식을 정리한 전화번호를 반영한다.
     */
    public void changePhoneNumber(String phoneNumber) {
        if (phoneNumber == null
                || !phoneNumber.matches("^010-[0-9]{4}-[0-9]{4}$")) {
            throw new IllegalArgumentException("전화번호 형식이 올바르지 않습니다.");
        }

        this.phoneNumber = phoneNumber;
    }

    /**
     * 인증과 중복 확인을 마친 새 이메일을 반영한다.
     */
    public void changeEmail(String email) {
        if (email == null || email.isBlank() || email.length() > 255) {
            throw new IllegalArgumentException("이메일이 올바르지 않습니다.");
        }

        this.email = email;
    }

    // 기존 로그인 서비스에서 호출한다.
    public void updateLastAccessTime() {
        this.lastAccessTime = LocalDateTime.now().withNano(0);
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        this.createdAt = now;
        this.modifiedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.modifiedAt = LocalDateTime.now().withNano(0);
    }
}