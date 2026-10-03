package com.myproject.stocksimulator.controller.dto.member;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원 정보 수정 요청을 전달받기 위한 요청 DTO
 *
 * 클라이언트가 회원 정보 수정을 요청할 때 전달한 JSON 데이터를 Spring이 이 객체로 변환하여 사용한다.
 *
 * PATCH 요청은 전체 정보를 변경하는 것이 아니라 전달된 값만 부분적으로 수정하는 방식으로 사용한다.
 *
 * 따라서 각 필드는 필수값으로 지정하지 않고 null인 필드는 기존 값을 유지하도록 처리한다.
 */
@Getter
@NoArgsConstructor
public class MemberPatchCommand {

    private String name;

    private String email;

    private String phoneNumber;

    private String passwd;
}