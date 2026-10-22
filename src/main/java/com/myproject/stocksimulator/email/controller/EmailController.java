package com.myproject.stocksimulator.email.controller;

import com.myproject.stocksimulator.email.dto.EmailSendRequest;
import com.myproject.stocksimulator.email.dto.EmailVerifyRequest;
import com.myproject.stocksimulator.email.service.EmailService;
import com.myproject.stocksimulator.global.exception.ErrorResponse;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 이메일 인증 API
 * 요청 형식을 검증하고 실제 인증 처리는 EmailService에 맡긴다.
 */
@RestController
@RequestMapping("/emails")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    /**
     * 입력한 이메일로 인증번호를 발송한다.
     * POST /emails/send
     */
    @PostMapping("/send")
    public ResponseEntity<String> send(@Valid @RequestBody EmailSendRequest request) {
        emailService.sendCode(request.email());

        return ResponseEntity.ok("인증번호 발송 요청이 처리되었습니다.");
    }

    /**
     * 인증번호를 확인한다.
     * 성공하면 true, 실패하면 오류 응답을 반환한다.
     * POST /emails/verify
     */
    @PostMapping("/verify")
    public ResponseEntity<?> verify(@Valid @RequestBody EmailVerifyRequest request) {
        boolean verified = emailService.verifyCode(
                request.email(),
                request.code()
        );

        if (!verified) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(
                            400,
                            "EMAIL_VERIFICATION_FAILED",
                            "인증번호가 올바르지 않거나 만료되었습니다. "
                                    + "인증번호를 확인하거나 다시 발송해주세요."
                    ));
        }

        return ResponseEntity.ok(true);
    }
}