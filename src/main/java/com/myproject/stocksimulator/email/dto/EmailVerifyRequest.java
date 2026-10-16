package com.myproject.stocksimulator.email.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 이메일 인증번호 검증 요청 DTO
 *
 * <p>
 * 사용자가 입력한 이메일 주소와
 * 이메일로 전달받은 6자리 인증번호를 함께 전달받는다.
 * </p>
 *
 * 검증 조건
 * 1. 이메일은 필수 입력값이다.
 * 2. 이메일 형식이 올바른지 확인한다.
 * 3. 인증번호는 숫자 6자리여야 한다.
 */
public record EmailVerifyRequest(

        // 인증번호를 발송한 이메일 주소
        @NotBlank(message = "이메일을 입력해주세요.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email,

        // 사용자가 이메일로 받은 6자리 인증번호
        @NotBlank(message = "인증번호를 입력해주세요.")
        @Pattern(
                regexp = "^[0-9]{6}$",
                message = "인증번호는 숫자 6자리여야 합니다."
        )
        String code

) {
}