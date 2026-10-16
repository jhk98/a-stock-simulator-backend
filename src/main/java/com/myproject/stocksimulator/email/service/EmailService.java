package com.myproject.stocksimulator.email.service;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Locale;

/**
 * 이메일 인증 서비스
 *
 * <p>
 * 주요 역할
 * 1. 6자리 인증번호 생성
 * 2. Gmail SMTP를 통한 인증번호 발송
 * 3. Redis에 인증번호 검증값 저장
 * 4. 인증번호 검증 및 실패 횟수 관리
 * 5. 이메일 인증 완료 상태 관리
 * </p>
 *
 * 인증 정책
 * - 인증번호 유효기간: 5분
 * - 재발송 제한: 5초 (개발 환경 기준)
 * - 인증 실패: 최대 5회
 * - 인증 완료 상태: 10분
 *
 * <p>
 * 인증번호 원문은 Redis에 저장하지 않고
 * HMAC-SHA256으로 변환한 값만 저장한다.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    // ============================================================
    // 1. 의존성
    // ============================================================

    // Gmail SMTP를 통한 이메일 발송
    private final JavaMailSender mailSender;

    // Redis 데이터 저장 및 조회
    private final StringRedisTemplate redisTemplate;

    // 예측하기 어려운 인증번호 생성
    private final SecureRandom secureRandom = new SecureRandom();


    // ============================================================
    // 2. 인증 정책
    // ============================================================

    // 인증번호 유효기간: 5분
    private static final Duration CODE_EXPIRATION =
            Duration.ofMinutes(5);

    // 재발송 제한: 5초 (개발용)
    private static final Duration RESEND_COOLDOWN =
            Duration.ofSeconds(5);

    // 인증 완료 상태 유지: 10분
    private static final Duration VERIFIED_EXPIRATION =
            Duration.ofMinutes(10);

    // 인증 실패 최대 횟수
    private static final int MAX_ATTEMPTS = 5;


    // ============================================================
    // 3. 환경변수
    // ============================================================

    // 발신자 Gmail 주소
    @Value("${spring.mail.username}")
    private String senderEmail;

    // HMAC-SHA256 비밀키
    @Value("${app.email.verification-secret}")
    private String verificationSecret;


    // ============================================================
    // 4. 인증번호 발송
    // ============================================================

    /**
     * 이메일 인증번호 발송
     *
     * <p>
     * 처리 과정
     * 1. 이메일 주소 정규화
     * 2. 재발송 제한 확인
     * 3. 인증번호 생성
     * 4. 인증번호를 이메일로 발송
     * 5. 발송 성공 시 Redis에 검증값 저장
     * </p>
     *
     * @param email 인증번호를 받을 이메일
     */
    public void sendCode(String email) {

        String normalizedEmail = normalizeEmail(email);

        String cooldownKey = cooldownKey(normalizedEmail);
        String codeKey = codeKey(normalizedEmail);
        String attemptsKey = attemptsKey(normalizedEmail);
        String verifiedKey = verifiedKey(normalizedEmail);

        // 1. Redis SET NX 명령으로 재발송 제한 설정
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(
                cooldownKey,
                "1",
                RESEND_COOLDOWN
        );

        if (!Boolean.TRUE.equals(acquired)) {
            throw new IllegalStateException(
                    "인증번호는 5초 후 다시 요청할 수 있습니다."
            );
        }

        // 2. 6자리 인증번호 생성
        String code = String.format(
                "%06d",
                secureRandom.nextInt(1_000_000)
        );

        // 3. 인증번호 HMAC-SHA256 변환
        String codeHash = hashCode(normalizedEmail, code);

        // 4. 이메일 메시지 생성
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(senderEmail);
        message.setTo(normalizedEmail);
        message.setSubject("[모의주식사이트] 이메일 인증번호");

        message.setText(
                "이메일 인증번호는 [" + code + "] 입니다.\n"
                        + "인증번호는 5분 동안 유효합니다."
        );

        // 5. 이메일 발송
        try {
            mailSender.send(message);
        } catch (RuntimeException e) {

            // 발송 실패 시 재시도할 수 있도록 제한 해제
            redisTemplate.delete(cooldownKey);

            throw new IllegalStateException(
                    "이메일 발송에 실패했습니다.", e
            );
        }

        // 6. 발송 성공 시 인증정보 저장
        redisTemplate.opsForValue().set(
                codeKey,
                codeHash,
                CODE_EXPIRATION
        );

        // 기존 실패 횟수 초기화
        redisTemplate.delete(attemptsKey);

        // 기존 인증 완료 상태 초기화
        redisTemplate.delete(verifiedKey);
    }


    // ============================================================
    // 5. 인증번호 검증
    // ============================================================

    /**
     * 이메일 인증번호 검증
     *
     * @param email     이메일 주소
     * @param code      사용자가 입력한 인증번호
     * @return          인증 성공 여부
     */
    public boolean verifyCode(String email, String code) {

        String normalizedEmail = normalizeEmail(email);

        String codeKey = codeKey(normalizedEmail);
        String attemptsKey = attemptsKey(normalizedEmail);
        String verifiedKey = verifiedKey(normalizedEmail);

        // 1. 인증번호 존재 여부 확인
        String savedHash = redisTemplate.opsForValue().get(codeKey);

        if (savedHash == null) {
            return false;
        }

        // 2. 실패 횟수 조회
        String attemptsValue =
                redisTemplate.opsForValue().get(attemptsKey);

        int attempts = attemptsValue == null
                ? 0
                : Integer.parseInt(attemptsValue);

        // 최대 실패 횟수에 도달한 경우
        if (attempts >= MAX_ATTEMPTS) {
            return false;
        }

        // 3. 입력한 인증번호를 HMAC으로 변환
        String inputHash = hashCode(normalizedEmail, code);

        // 4. 일정 시간 비교 방식으로 인증번호 확인
        boolean matches = MessageDigest.isEqual(
                savedHash.getBytes(StandardCharsets.UTF_8),
                inputHash.getBytes(StandardCharsets.UTF_8)
        );

        // 5. 인증번호가 틀린 경우
        if (!matches) {

            // Redis INCR을 사용하여 실패 횟수 증가
            Long failedCount =
                    redisTemplate.opsForValue().increment(attemptsKey);

            // 첫 실패 시 인증번호와 동일한 만료시간 적용
            if (failedCount != null && failedCount == 1) {

                Long remainingSeconds =
                        redisTemplate.getExpire(
                                codeKey,
                                java.util.concurrent.TimeUnit.SECONDS
                        );

                if (remainingSeconds != null && remainingSeconds > 0) {
                    redisTemplate.expire(
                            attemptsKey,
                            Duration.ofSeconds(remainingSeconds)
                    );
                }
            }

            // 최대 실패 횟수 도달 시 인증번호 무효화
            if (failedCount != null && failedCount >= MAX_ATTEMPTS) {
                redisTemplate.delete(codeKey);
                redisTemplate.delete(attemptsKey);
            }

            return false;
        }

        // 6. 인증 성공 상태를 10분간 저장
        redisTemplate.opsForValue().set(
                verifiedKey,
                "true",
                VERIFIED_EXPIRATION
        );

        // 7. 사용한 인증번호와 실패 횟수 제거
        redisTemplate.delete(codeKey);
        redisTemplate.delete(attemptsKey);

        return true;
    }


    // ============================================================
    // 6. 인증 완료 여부 확인
    // ============================================================

    /**
     * 해당 이메일의 인증 완료 여부 확인
     *
     * <p> 회원가입 시 MemberService에서 호출한다. </p>
     */
    public boolean isVerified(String email) {

        String normalizedEmail = normalizeEmail(email);

        return "true".equals(
                redisTemplate.opsForValue().get(
                        verifiedKey(normalizedEmail)
                )
        );
    }


    // ============================================================
    // 7. 인증 완료 상태 제거
    // ============================================================

    /**
     * 인증 완료 상태 제거
     *
     * <p> 회원가입 완료 후 인증 상태를 정리할 때 사용한다. </p>
     */
    public void clearVerification(String email) {

        String normalizedEmail = normalizeEmail(email);

        redisTemplate.delete(
                verifiedKey(normalizedEmail)
        );
    }


    // ============================================================
    // 8. Redis Key 생성
    // ============================================================

    private String codeKey(String email) {
        return "email:code:" + email;
    }

    private String attemptsKey(String email) {
        return "email:attempts:" + email;
    }

    private String cooldownKey(String email) {
        return "email:cooldown:" + email;
    }

    private String verifiedKey(String email) {
        return "email:verified:" + email;
    }


    // ============================================================
    // 9. 이메일 주소 정규화
    // ============================================================

    /**
     * 이메일 앞뒤 공백 제거 및 소문자 변환
     *
     * <p>
     * 인증번호 발송과 검증 과정에서
     * 동일한 Redis Key를 사용하도록 처리한다.
     * </p>
     */
    private String normalizeEmail(String email) {

        return email.trim().toLowerCase(Locale.ROOT);
    }


    // ============================================================
    // 10. 인증번호 HMAC-SHA256 변환
    // ============================================================

    /**
     * 인증번호를 HMAC-SHA256으로 변환한다.
     *
     * <p> 인증번호 원문을 Redis에 저장하지 않기 위한 처리다. </p>
     */
    private String hashCode(String email, String code) {

        try {
            Mac mac = Mac.getInstance("HmacSHA256");

            SecretKeySpec secretKey = new SecretKeySpec(
                    verificationSecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );

            mac.init(secretKey);

            byte[] hash = mac.doFinal(
                    (email + ":" + code)
                            .getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "인증번호 검증값 생성에 실패했습니다.", e
            );
        }
    }
}