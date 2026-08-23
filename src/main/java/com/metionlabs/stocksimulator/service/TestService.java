package com.metionlabs.stocksimulator.service;

import com.metionlabs.stocksimulator.domain.Account;
import com.metionlabs.stocksimulator.domain.Member;
import com.metionlabs.stocksimulator.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestService {

    @Autowired
    private AccountRepository accountRepository;

//    public void test() {
//        Account account = Account
//                .builder()
//                .accountNum("wefwef")
//                .member(new Member())
//                .build();
//
//        accountRepository.save(account);
//    }
}
