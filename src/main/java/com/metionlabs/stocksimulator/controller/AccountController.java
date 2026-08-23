package com.metionlabs.stocksimulator.controller;

import com.metionlabs.stocksimulator.controller.dto.AccountGetQuery;
import com.metionlabs.stocksimulator.controller.dto.AccountGetResult;
import com.metionlabs.stocksimulator.controller.dto.AccountPostCommand;
import com.metionlabs.stocksimulator.controller.dto.AccountPostResult;
import com.metionlabs.stocksimulator.domain.Account;
import com.metionlabs.stocksimulator.domain.Member;
import com.metionlabs.stocksimulator.repository.AccountRepository;
import com.metionlabs.stocksimulator.repository.MemberRepository;
import com.metionlabs.stocksimulator.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    AccountService accountService;

    @GetMapping("/{id}/")
    public ResponseEntity<AccountGetResult> get(@PathVariable Long id) {
        Account account = accountRepository.findById(id).orElseThrow();
        AccountGetResult result = new AccountGetResult(account);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/memberId/{memberId}")
    public ResponseEntity<?> post(
            @RequestBody AccountPostCommand accountPostCommand,
            @PathVariable Long memberId
    ) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("member id must not be null"));
        Account create = accountPostCommand.toAccount(member);
        Account created = accountService.createAccount(create);
        AccountPostResult result = AccountPostResult.builder()
                .accountId(created.getId())
                .build();
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> patch(@PathVariable Long id) {

//        return new ResponseEntity<>(HttpStatus.OK);
        return ResponseEntity.ok(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
//        return new ResponseEntity<>(HttpStatus.OK);
        return ResponseEntity.ok(id);
    }
}