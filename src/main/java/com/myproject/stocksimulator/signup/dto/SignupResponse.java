package com.myproject.stocksimulator.signup.dto;

/**
 * 회원가입 성공 응답 DTO
 *
 * <p> 회원가입이 정상적으로 완료되면 MySQL에서 생성된 회원 ID를 반환한다. </p>
 *
 * 비밀번호와 같은 민감한 정보는 응답에 포함하지 않는다.
 */
public record SignupResponse(

        // 회원가입 후 생성된 회원 기본키(PK)
        Long id
) {
}