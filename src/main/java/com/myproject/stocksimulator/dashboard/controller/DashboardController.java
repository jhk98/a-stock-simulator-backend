package com.myproject.stocksimulator.dashboard.controller;

import com.myproject.stocksimulator.dashboard.dto.DashboardResponse;
import com.myproject.stocksimulator.dashboard.dto.DashboardResponse.AccountView;
import com.myproject.stocksimulator.dashboard.service.DashboardService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * JWT로 인증된 본인의 메인 화면 정보만 조회한다.
     */
    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(
            @AuthenticationPrincipal Long memberId
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(dashboardService.getDashboard(memberId));
    }

    /**
     * 요청 본문으로 회원 ID나 초기 금액을 받지 않는다.
     *
     * <p> 신규 개설 또는 기존 계좌 반환 모두 HTTP 200이다. </p>
     */
    @PostMapping("/account")
    public ResponseEntity<AccountView> openAccount(
            @AuthenticationPrincipal Long memberId
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(dashboardService.openAccount(memberId));
    }
}