package com.myproject.stocksimulator.member.controller;

import com.myproject.stocksimulator.global.exception.ErrorResponse;
import com.myproject.stocksimulator.member.dto.MemberResponse;
import com.myproject.stocksimulator.member.dto.MemberUpdateRequest;
import com.myproject.stocksimulator.member.service.MemberService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 로그인한 회원의 개인정보 조회·수정 API
 */
@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     * 수정 화면에 표시할 현재 회원정보를 반환한다.
     */
    @GetMapping("/me")
    public ResponseEntity<MemberResponse> getMe(
            @AuthenticationPrincipal Long memberId
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(memberService.getById(memberId));
    }

    /**
     * 비밀번호·전화번호·이메일 중 전달된 항목만 변경한다.
     */
    @PatchMapping("/me")
    public ResponseEntity<MemberResponse> updateMe(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody MemberUpdateRequest request
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(memberService.update(memberId, request));
    }

    /**
     * 현재 비밀번호 불일치, 인증번호 오류 등의 요청 오류
     * 기존 로그인·회원가입 예외 처리에는 영향을 주지 않는다.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequest(
            IllegalArgumentException exception
    ) {
        return ResponseEntity.badRequest()
                .cacheControl(CacheControl.noStore())
                .body(new ErrorResponse(
                        400,
                        "MEMBER_UPDATE_INVALID",
                        exception.getMessage()
                ));
    }

    /**
     * 중복 확인 이후 다른 요청이 같은 이메일을 먼저 저장한 경우 등 DB 제약조건 충돌을 처리한다.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleConflict(
            DataIntegrityViolationException exception
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .cacheControl(CacheControl.noStore())
                .body(new ErrorResponse(
                        409,
                        "MEMBER_UPDATE_CONFLICT",
                        "다른 요청과 충돌하여 저장하지 못했습니다. "
                                + "이메일 중복 여부를 확인해주세요."
                ));
    }
}