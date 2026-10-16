package com.myproject.stocksimulator.email.controller;

import com.myproject.stocksimulator.email.dto.EmailSendRequest;
import com.myproject.stocksimulator.email.dto.EmailVerifyRequest;
import com.myproject.stocksimulator.email.service.EmailService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 이메일 인증 API Controller
 *
 * <p>
 * 주요 역할
 * 1. 이메일 인증번호 발송 요청 처리
 * 2. 이메일 인증번호 검증 요청 처리
 * </p>
 *
 * Controller는 HTTP 요청 및 응답을 담당한다.
 * 인증번호 생성, Redis 저장, 이메일 발송, 인증번호 검증 등
 * 실제 비즈니스 로직은 EmailService에서 처리한다.
 */
@RestController
@RequestMapping("/emails")
@RequiredArgsConstructor
public class EmailController {

    // 이메일 인증 비즈니스 로직을 처리하는 Service
    private final EmailService emailService;


    // ============================================================
    // 1. 이메일 인증번호 발송
    // ============================================================

    /**
     * 이메일 인증번호 발송 API
     *
     * <p>
     * 요청
     * POST /emails/send
     * </p>
     *
     * 처리 과정
     * 1. 요청 JSON을 EmailSendRequest DTO로 변환
     * 2. @Valid를 통해 이메일 형식 검증
     * 3. EmailService에 인증번호 발송 요청
     * 4. 처리 결과를 HTTP 200 OK로 반환
     *
     * @param request   인증번호를 받을 이메일 정보
     * @return          인증번호 발송 요청 처리 메시지
     */
    @PostMapping("/send")
    public ResponseEntity<String> send(
            @Valid @RequestBody EmailSendRequest request
    ) {

        // 이메일 인증번호 발송 로직을 Service에 위임
        emailService.sendCode(request.email());

        // 정상 처리 시 HTTP 200 OK 반환
        return ResponseEntity.ok(
                "인증번호 발송 요청이 처리되었습니다."
        );
    }


    // ============================================================
    // 2. 이메일 인증번호 검증
    // ============================================================

    /**
     * 이메일 인증번호 검증 API
     *
     * <p>
     * 요청
     * POST /emails/verify
     * </p>
     *
     * 처리 과정
     * 1. 요청 JSON을 EmailVerifyRequest DTO로 변환
     * 2. @Valid를 통해 입력값 검증
     * 3. EmailService에서 인증번호 일치 여부 확인
     * 4. 인증 결과를 HTTP 200 OK로 반환
     *
     * @param request   이메일 및 인증번호
     * @return          인증 성공 true, 인증 실패 false
     */
    @PostMapping("/verify")
    public ResponseEntity<Boolean> verify(
            @Valid @RequestBody EmailVerifyRequest request
    ) {

        // 이메일과 인증번호를 Service에 전달하여 검증
        boolean verified = emailService.verifyCode(
                request.email(),
                request.code()
        );

        // 검증 결과 반환
        return ResponseEntity.ok(verified);
    }
}