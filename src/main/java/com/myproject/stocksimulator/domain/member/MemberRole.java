package com.myproject.stocksimulator.domain.member;

/**
 * 회원의 권한을 정의하는 Enum
 *
 * DB의 a_stock_member.role 컬럼에는 Enum 이름이 문자열 그대로 저장됨
 */
public enum MemberRole {

    // 일반 사용자
    USER,

    // 관리자
    ADMIN
}