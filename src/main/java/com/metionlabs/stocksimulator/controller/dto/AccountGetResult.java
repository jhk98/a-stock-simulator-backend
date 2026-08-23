package com.metionlabs.stocksimulator.controller.dto;

import com.metionlabs.stocksimulator.domain.Account;
import com.metionlabs.stocksimulator.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class AccountGetResult {

    private Long id;
    private Member member;
    private String accountNum;
    private Long deposit;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public AccountGetResult(Account account) {
        this.id = account.getId();
        this.member = account.getMember();
        this.accountNum = account.getAccountNum();
        this.createdAt = account.getCreatedAt();
        this.modifiedAt = account.getModifiedAt();
    }
}
