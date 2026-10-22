package com.myproject.stocksimulator.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 로그인 요청 DTO
 *
 * <p> React에서 전달한 로그인 정보를 받는다.
 * record는 데이터 전달용 불변 객체를 간단하게 정의할 때 사용한다. </p>
 */
public record LoginRequest(

        // 로그인 아이디
        @NotBlank(message = "아이디를 입력해주세요.")
        String loginId,

        // 로그인 비밀번호
        @NotBlank(message = "비밀번호를 입력해주세요.")
        String password
) {
}