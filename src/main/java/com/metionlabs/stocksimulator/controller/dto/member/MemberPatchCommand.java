package com.metionlabs.stocksimulator.controller.dto.member;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberPatchCommand {

    private String email;

    private String phoneNumber;

    private String passwd;
}