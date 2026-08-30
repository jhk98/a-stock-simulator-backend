package com.metionlabs.stocksimulator.controller.dto.member;

import com.metionlabs.stocksimulator.domain.Member;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MemberGetResult {

    private final Long id;
    private final String loginId;
    private final String name;
    private final String email;
    private final String phoneNumber;
    private final String rgstNumber;
    private final String passwd;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;

    private MemberGetResult(Member member) {
        this.id = member.getId();
        this.loginId = member.getLoginId();
        this.name = member.getName();
        this.email = member.getEmail();
        this.phoneNumber = member.getPhoneNumber();
        this.rgstNumber = member.getRgstNumber();
        this.passwd = member.getPasswd();
        this.createdAt = member.getCreatedAt();
        this.modifiedAt = member.getModifiedAt();
    }

    public static MemberGetResult from(Member member) {
        return new MemberGetResult(member);
    }
}