package com.metionlabs.stocksimulator.controller.dto;

import com.metionlabs.stocksimulator.domain.Account;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class AccountPatchCommand {

    private String accountNumber;

    private Long deposit;

    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

    public Account toAccount() {
        return Account.builder()
                .accountNum(this.accountNumber)
                .deposit(this.deposit)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();
    }
}
