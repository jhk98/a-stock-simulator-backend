package com.myproject.stocksimulator.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 개인정보 수정 요청
 *
 * <p>
 * currentPassword는 필수다.
 * 수정하지 않는 항목은 생략하거나 null로 전달한다.
 * 이메일을 변경할 때는 emailCode도 함께 전달한다.
 * </p>
 */
public record MemberUpdateRequest(

        @NotBlank(message = "현재 비밀번호를 입력해주세요.")
        @Size(max = 100, message = "현재 비밀번호가 너무 깁니다.")
        String currentPassword,

        // 회원가입과 같은 비밀번호 규칙을 사용한다.
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s])\\S{8,20}$",
                message = "새 비밀번호는 영문, 숫자, 특수문자를 포함한 8~20자로 입력해주세요."
        )
        String newPassword,

        // 입력 예: 01012345678
        @Pattern(
                regexp = "^010[0-9]{8}$",
                message = "전화번호는 하이픈 없이 010으로 시작하는 숫자 11자리로 입력해주세요."
        )
        String phoneNumber,

        @Email(message = "올바른 이메일 형식이 아닙니다.")
        @Size(min = 1, max = 255, message = "이메일은 1~255자로 입력해주세요.")
        String email,

        @Pattern(
                regexp = "^[0-9]{6}$",
                message = "이메일 인증번호는 숫자 6자리로 입력해주세요."
        )
        String emailCode
) {
}