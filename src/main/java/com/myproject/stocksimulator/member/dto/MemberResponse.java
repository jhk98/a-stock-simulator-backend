package com.myproject.stocksimulator.member.dto;

import com.myproject.stocksimulator.member.entity.Member;

import java.time.LocalDateTime;

/**
 * 회원정보 응답 DTO
 *
 * <p>
 * 주요 역할
 * 1. 회원정보 조회 결과 반환
 * 2. 회원정보 수정 결과 반환 (추후 구현)
 * </p>
 *
 * <p>
 * 설계 원칙
 * - Entity를 Controller에 직접 반환하지 않는다.
 * - 클라이언트에 필요한 회원정보만 전달한다.
 * - 비밀번호 등 민감한 인증정보는 응답에 포함하지 않는다.
 * - 회원가입 결과는 SignupResponse에서 담당한다.
 * </p>
 *
 * 사용 예시
 * GET   /members/me
 * PATCH /members/me
 */
public record MemberResponse(

        // ============================================================
        // 1. 회원 기본정보
        // ============================================================

        // 회원 고유 식별자 (PK)
        Long id,

        // 로그인 아이디
        String loginId,

        // 회원 이름
        String name,

        // 전화번호 (010-1234-5678 형식)
        String phoneNumber,

        // 이메일
        String email,


        // ============================================================
        // 2. 회원 활동 및 관리 일시
        // ============================================================

        // 마지막 로그인 일시
        LocalDateTime lastAccessTime,

        // 회원가입 일시
        LocalDateTime createdAt,

        // 회원정보 마지막 수정 일시
        LocalDateTime modifiedAt

) {

    /**
     * Member Entity를 MemberResponse DTO로 변환한다.
     *
     * <p> Service에서 조회한 Member Entity를 클라이언트에 전달할 응답 데이터로 변환한다. </p>
     *
     * @param member 조회한 회원 Entity
     * @return 회원정보 응답 DTO
     */
    public static MemberResponse from(Member member) {

        return new MemberResponse(

                // 회원 기본정보
                member.getId(),
                member.getLoginId(),
                member.getName(),
                member.getPhoneNumber(),
                member.getEmail(),

                // 회원 활동 및 관리 일시
                member.getLastAccessTime(),
                member.getCreatedAt(),
                member.getModifiedAt()
        );
    }
}