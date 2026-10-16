package com.myproject.stocksimulator.signup.controller;

import com.myproject.stocksimulator.signup.dto.SignupRequest;
import com.myproject.stocksimulator.signup.dto.SignupResponse;
import com.myproject.stocksimulator.signup.service.SignupService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 회원가입 API Controller
 *
 * <p>
 * 주요 역할
 * 1. 회원가입 HTTP 요청 처리
 * 2. 로그인 아이디 중복 확인
 * 3. 이메일 중복 확인
 * 4. 요청 데이터 유효성 검증
 * 5. SignupService 호출 및 결과 반환
 * </p>
 *
 * 설계 원칙
 * - Controller는 Service만 의존한다.
 * - Repository에 직접 접근하지 않는다.
 * - 비즈니스 로직은 Service에서 처리한다.
 */
@RestController
@RequestMapping("/signup")
@RequiredArgsConstructor
@Validated
public class SignupController {

    // 회원가입 비즈니스 로직을 담당하는 Service
    private final SignupService signupService;


    // ============================================================
    // 1. 회원가입 API
    // ============================================================

    /**
     * 회원가입
     *
     * <p>
     * HTTP Method : POST
     * URL         : /signup
     * </p>
     *
     * <p> @Valid
     * - SignupRequest에 선언된 유효성 검증 조건을 확인한다.
     * - 검증 실패 시 Service를 호출하지 않는다. </p>
     *
     * @param request   회원가입 요청 정보
     * @return          생성된 회원 ID와 HTTP 201 Created
     */
    @PostMapping
    public ResponseEntity<SignupResponse> signup(
            @Valid @RequestBody SignupRequest request
    ) {

        // 회원가입 처리
        SignupResponse response = signupService.signup(request);

        // 회원가입 성공 시 HTTP 201 Created 반환
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    // ============================================================
    // 2. 로그인 아이디 중복 확인 API
    // ============================================================

    /**
     * 로그인 아이디 중복 확인
     *
     * <p>
     * HTTP Method : GET
     * URL         : /signup/check-login-id?loginId=stockuser01
     * </p>
     *
     * @param loginId   중복 확인할 로그인 아이디
     * @return          true: 중복 / false: 사용 가능
     */
    @GetMapping("/check-login-id")
    public ResponseEntity<Boolean> checkLoginId(
            @RequestParam
            @NotBlank(message = "아이디를 입력해주세요.")
            @Pattern(
                    regexp = "^[a-zA-Z0-9]{4,20}$",
                    message = "아이디는 영문과 숫자로 4~20자 입력해야 합니다."
            )
            String loginId
    ) {

        // SignupService에서 아이디 중복 여부 확인
        boolean exists = signupService.checkLoginId(loginId);

        // 중복 여부 반환
        return ResponseEntity.ok(exists);
    }


    // ============================================================
    // 3. 이메일 중복 확인 API
    // ============================================================

    /**
     * 이메일 중복 확인
     *
     * <p>
     * HTTP Method : GET
     * URL         : /signup/check-email?email=test@gmail.com
     * </p>
     *
     * @param email     중복 확인할 이메일
     * @return          true: 중복 / false: 사용 가능
     */
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(
            @RequestParam
            @NotBlank(message = "이메일을 입력해주세요.")
            @Email(message = "올바른 이메일 형식이 아닙니다.")
            @Size(max = 255, message = "이메일은 255자 이하여야 합니다.")
            String email
    ) {

        // SignupService에서 이메일 중복 여부 확인
        boolean exists = signupService.checkEmail(email);

        // 중복 여부 반환
        return ResponseEntity.ok(exists);
    }
}