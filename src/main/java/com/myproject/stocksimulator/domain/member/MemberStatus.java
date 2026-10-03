package com.myproject.stocksimulator.domain.member;

/**
 * 회원의 상태를 정의하는 Enum
 *
 * DB의 a_stock_member.status 컬럼에는 Enum 이름이 문자열 그대로 저장됨
 */
public enum MemberStatus {

    // 정상 회원 (활성화)
    ACTIVE,

    // 탈퇴 회원 (비활성화)
    INACTIVE
}