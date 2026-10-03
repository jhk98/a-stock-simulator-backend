package com.myproject.stocksimulator.controller.dto.member;

import com.myproject.stocksimulator.domain.member.Member;
import com.myproject.stocksimulator.domain.member.MemberRole;
import com.myproject.stocksimulator.domain.member.MemberStatus;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 회원 조회 결과를 클라이언트에게 반환하기 위한 응답 DTO
 *
 * DB에서 조회한 Member Entity를 그대로 클라이언트에게 반환하지 않고,
 * 실제 조회 응답에 필요한 정보만 선별하여 전달하기 위해 사용한다.
 *
 * 특히 비밀번호, 주민등록번호 같은 민감정보는 이 DTO에 포함하지 않기 때문에 API 응답으로 노출되지 않는다.
 *
 * Member Entity
 *      ↓
 * MemberGetResult
 *      ↓
 * Controller의 HTTP Response
 *      ↓
 * JSON 형태로 클라이언트에 전달
 */
@Getter
public class MemberGetResult {

    private final Long id;
    private final String loginId;
    private final String name;
    private final String email;
    private final String phoneNumber;
    private final MemberStatus status;
    private final MemberRole role;
    private final LocalDateTime lastAccessTime;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;

    /**
     * Member Entity를 전달받아 MemberGetResult DTO로 변환하는 생성자
     *
     * Member가 가지고 있는 값 중 클라이언트에게 반환할 값만 꺼내서 MemberGetResult의 각 필드에 저장한다.
     *
     * private 생성자로 선언했기 때문에 외부에서 new MemberGetResult(member) 형태로 직접 객체를 생성할 수 없다.
     *
     * 대신 아래의 from() 정적 메서드를 통해 생성하도록 제한한다.
     *
     * @param member    조회된 회원 Entity
     */
    private MemberGetResult(Member member) {
        this.id = member.getId();
        this.loginId = member.getLoginId();
        this.name = member.getName();
        this.email = member.getEmail();
        this.phoneNumber = member.getPhoneNumber();
        this.status = member.getStatus();
        this.role = member.getRole();
        this.lastAccessTime = member.getLastAccessTime();
        this.createdAt = member.getCreatedAt();
        this.modifiedAt = member.getModifiedAt();
    }

    // 엔티티로부터 GetResult DTO를 생성하는 메서드

    /**
     * Member Entity를 MemberGetResult DTO로 변환하는 정적 팩토리 메서드
     *
     * 외부에서는 생성자를 직접 호출하지 않고 이 메서드를 이용하여 응답 DTO를 생성한다.
     *
     * @param member    변환할 Member Entity
     * @return          Member 정보를 담은 MemberGetResult DTO
     */
    public static MemberGetResult from(Member member) {
        return new MemberGetResult(member);
    }
}