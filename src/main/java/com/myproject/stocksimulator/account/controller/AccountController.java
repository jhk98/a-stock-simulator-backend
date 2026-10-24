package com.myproject.stocksimulator.account.controller;

import com.myproject.stocksimulator.account.service.AccountService;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 로그인한 회원의 모의계좌 개설 API
 */
@RestController
public class AccountController {

    private final AccountService accounts;

    public AccountController(AccountService accounts) {
        this.accounts = accounts;
    }

    /**
     * 현재 프론트와 SecurityConfig가 사용하는 주소를 유지한다.
     *
     * 회원 ID와 초기 금액은 요청 본문으로 받지 않는다.
     * JWT의 회원 ID와 서버의 초기자금 설정만 사용한다.
     */
    @PostMapping("/dashboard/account")
    public ResponseEntity<AccountService.AccountResponse> openAccount(
            @AuthenticationPrincipal Long memberId
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(accounts.openAccount(memberId));
    }
}