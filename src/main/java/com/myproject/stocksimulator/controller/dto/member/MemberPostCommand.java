package com.myproject.stocksimulator.controller.dto.member;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원가입 요청 데이터를 전달받기 위한 요청 DTO
 *
 * 클라이언트에서 회원가입 요청을 보낼 때 전달한 JSON 데이터를 Spring이 MemberPostCommand 객체로 변환하여 사용한다.
 *
 * 회원가입은 새로운 회원 정보를 생성하는 작업이므로 아래 값들은 모두 필수로 입력받는다.
 */
@Getter
@NoArgsConstructor
public class MemberPostCommand {

    @NotBlank(message = "로그인 아이디는 필수입니다.")
    private String loginId;

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @NotBlank(message = "이메일은 필수입니다.")
    private String email;

    @NotBlank(message = "전화번호는 필수입니다.")
    private String phoneNumber;

    @NotBlank(message = "주민등록번호는 필수입니다.")
    private String rgstNumber;

    @NotBlank(message = "비밀번호는 필수입니다.")
    private String passwd;
}