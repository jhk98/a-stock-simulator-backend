package com.myproject.stocksimulator.email.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 이메일 인증번호 발송 요청 DTO
 *
 * <p> 사용자가 회원가입 화면에서 입력한 이메일 주소를 전달받는다. </p>
 *
 * 검증 조건
 * 1. 이메일은 필수 입력값이다.
 * 2. 이메일 형식이 올바른지 확인한다.
 */
public record EmailSendRequest(

        // 인증번호를 받을 이메일 주소
        @NotBlank(message = "이메일을 입력해주세요.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email

) {
}