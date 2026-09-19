package com.myproject.stocksimulator.controller.dto.member;

import lombok.Getter;
import lombok.NoArgsConstructor;

// 회원이 수정할 수 있는 정보
@Getter
@NoArgsConstructor
public class MemberPatchCommand {

    private String name;

    private String email;

    private String phoneNumber;

    private String passwd;
}