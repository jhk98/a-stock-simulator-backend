package com.myproject.stocksimulator.controller.dto.member;

import lombok.Getter;

/**
 * 회원가입 완료 후 결과를 클라이언트에게 반환하기 위한 응답 DTO
 *
 * 회원가입이 성공하면 DB에서 새롭게 생성된 회원의 PK(memberId)를 클라이언트에게 반환하기 위해 사용한다.
 *
 * 회원가입 결과로 Member Entity 전체를 반환하지 않고 필요한 회원 ID만 반환하도록 구성한다.
 */
@Getter
public class MemberPostResult {

    private final Long memberId;

    /**
     * 생성된 회원의 ID를 전달받아 응답 DTO를 생성한다.
     *
     * @param memberId  회원가입 후 생성된 회원의 PK
     */
    public MemberPostResult(Long memberId) {
        this.memberId = memberId;
    }
}