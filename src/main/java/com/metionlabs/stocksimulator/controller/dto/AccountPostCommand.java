package com.metionlabs.stocksimulator.controller.dto;

import com.metionlabs.stocksimulator.domain.Account;
import com.metionlabs.stocksimulator.domain.Member;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AccountPostCommand {

    private String accountNum;

    private Long deposit;

    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

    public Account toAccount(Member member) {
        return Account.builder()
                .accountNum(this.accountNum)
                .deposit(this.deposit)
                .member(member)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();
    }
}
