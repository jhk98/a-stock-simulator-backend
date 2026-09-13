package com.metionlabs.stocksimulator.controller.dto.member;

import com.metionlabs.stocksimulator.domain.member.Member;
import com.metionlabs.stocksimulator.domain.member.MemberRole;
import com.metionlabs.stocksimulator.domain.member.MemberStatus;
import lombok.Getter;

import java.time.LocalDateTime;

// 회원 조회 시 반환할 정보
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

    // 엔티티를 전달받아서 DTO객체로 변환하는 생성자
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
    public static MemberGetResult from(Member member) {
        return new MemberGetResult(member);
    }
}