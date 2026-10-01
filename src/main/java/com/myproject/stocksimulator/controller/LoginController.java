package com.myproject.stocksimulator.controller;

import com.myproject.stocksimulator.controller.dto.login.LoginPostCommand;
import com.myproject.stocksimulator.controller.dto.login.LoginPostResult;
import com.myproject.stocksimulator.domain.member.Member;
import com.myproject.stocksimulator.service.LoginService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/login")
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    // ========================================================================
    // POST /login
    // ========================================================================

    // 로그인
    @PostMapping
    public ResponseEntity<LoginPostResult> login(
            @RequestBody @Valid LoginPostCommand command
    ) {
        Member member = loginService.login(command);

        return ResponseEntity.ok(
                LoginPostResult.from(member)
        );
    }
}