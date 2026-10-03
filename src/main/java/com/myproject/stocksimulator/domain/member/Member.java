package com.myproject.stocksimulator.domain.member;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 회원 정보를 나타내는 JPA Entity
 */
@Table(name="a_stock_member")
@Entity                         // 이 클래스를 JPA가 관리하는 Entity로 등록
@Getter                         // 모든 필드의 Getter를 Lombok이 자동 생성
@Builder                        // Member 객체를 Builder 방식으로 생성할 수 있도록 지원
@AllArgsConstructor             // 모든 필드를 매개변수로 받는 생성자를 Lombok이 자동 생성
public class Member {

    /**
     * 회원 테이블의 기본키 (PK)
     *
     * @GeneratedValue(strategy = IDENTITY)
     * - PK 값을 애플리케이션에서 직접 생성하지 않고 DB에 맡긴다.
     * - MySQL의 AUTO_INCREMENT와 연결된다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 회원 아이디
     */
    @Column(name = "login_id", nullable = false, unique = true, length = 50)
    private String loginId;

    /**
     * 회원 이름
     */
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    /**
     * 회원 이메일
     */
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    /**
     * 회원 전화번호
     */
    @Column(name = "phone_number", nullable = false, unique = true, length = 20)
    private String phoneNumber;

    /**
     * 회원 주민등록번호
     *
     * 실제 DB에는 원문 그대로 저장하지 않고 암호화된 값을 저장
     */
    @Column(name = "rgst_number", nullable = false, length = 255)
    private String rgstNumber;

    /**
     * 회원 비밀번호
     *
     * BCrypt 방식으로 해시 처리된 값을 저장
     */
    @Column(name = "passwd", nullable = false, length = 255)
    private String passwd;

    /**
     * 회원 상태
     *
     * MemberStatus는 Enum 타입
     *
     * @Enumerated(EnumType.STRING)
     * - 이름을 문자열로 DB에 저장
     *
     * Enum 순서가 변경되어도 DB 데이터가 영향을 받지 않기 때문에 ORDINAL 방식보다 STRING 방식을 사용하는 것이 안전
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MemberStatus status;

    /**
     * 회원 역할
     *
     * @Enumerated(EnumType.STRING)
     * - 이름을 문자열로 DB에 저장
     *
     * Enum 순서가 변경되어도 DB 데이터가 영향을 받지 않기 때문에 ORDINAL 방식보다 STRING 방식을 사용하는 것이 안전
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private MemberRole role;

    /**
     * 마지막 접속 시간
     *
     * 회원가입 직후에는 아직 로그인한 상태가 아니므로 NULL을 허용한다.
     *
     * Java의 LocalDateTime은 MySQL의 DATETIME과 매핑된다.
     */
    @Column(name = "last_access_time")
    private LocalDateTime lastAccessTime;

    /**
     * 생성일
     *
     * 회원가입 시 설정됨
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 수정일
     *
     * 회원가입 시 설정됨
     * 회원정보가 수정될 때 update() 메서드에서 현재 시간으로 변경
     */
    @Column(name = "modified_at", nullable = false)
    private LocalDateTime modifiedAt;

    /**
     * 회원 탈퇴 일시
     */
    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt;

    /**
     * JPA에서 사용하는 기본 생성자
     *
     * JPA(Hibernate)는 DB에서 조회한 데이터를 이용하여 Member 객체를 생성해야 하기 때문에 기본 생성자가 필요하다.
     *
     * 하지만 애플리케이션 코드에서 new Member() 처럼 아무 값도 없는 회원 객체를 만드는 것을 막기 위해 public이 아닌 protected로 선언
     */
    protected Member() {
    }

    /**
     * 회원정보 수정 메서드
     *
     * 전달된 값이 null인 경우 해당 필드는 수정하지 않음
     *
     * 하나 이상의 회원 정보가 변경되면 modifiedAt을 현재 시간으로 변경
     *
     * @param name          변경할 회원 이름
     * @param email         변경할 회원 이메일
     * @param phoneNumber   변경할 회원 전화번호
     * @param passwd        변경할 회원 비밀번호
     */
    public void update(String name, String email, String phoneNumber, String passwd) {
        if(name != null) {
            this.name = name;
        }
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

    /**
     * 회원의 마지막 접속 시간을 현재 시간으로 변경하는 메서드
     *
     * 로그인 성공 시점에 호출하여 해당 회원이 마지막으로 접속한 시간을 기록할 수 있음
     */
    public void updateLastAccessTime() {
        this.lastAccessTime = LocalDateTime.now();
    }

    /**
     * 회원 탈퇴 처리
     *
     * 회원 데이터를 즉시 DB에서 삭제하지 않고 상태를 INACTIVE로 변경하여 논리적으로 탈퇴 처리한다.
     *
     * withdrawnAt에는 실제 탈퇴 요청 시간을 기록하며 이후 30일이 경과하면 스케줄러에 의해 실제 데이터가 삭제된다.
     */
    public void withdraw() {
        LocalDateTime now = LocalDateTime.now();

        this.status = MemberStatus.INACTIVE;
        this.withdrawnAt = now;
        this.modifiedAt = now;
    }
}