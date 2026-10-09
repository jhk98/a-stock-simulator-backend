package com.myproject.stocksimulator.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 로그인 요청 데이터
 */
public record LoginRequest(

        @NotBlank(message = "아이디를 입력해주세요.")
        String loginId,

        @NotBlank(message = "비밀번호를 입력해주세요.")
        String password

) {
}