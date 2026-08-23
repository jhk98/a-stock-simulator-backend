package com.metionlabs.stocksimulator.service;

import com.metionlabs.stocksimulator.domain.Account;
import com.metionlabs.stocksimulator.repository.AccountRepository;
import com.metionlabs.stocksimulator.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    AccountRepository accountRepository;

    public Account createAccount(Account account) {
        return accountRepository.save(account);
    }

}
