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
import java.util.concurrent.TimeUnit;

/**
 * 이메일 인증번호 발송과 검증을 담당한다.
 * 인증번호와 인증 완료 상태는 만료시간이 있는 Redis 데이터로 관리한다.
 * 인증번호 원문 대신 HMAC-SHA256 검증값을 저장한다.
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Duration CODE_EXPIRATION = Duration.ofMinutes(5);

    // 현재 개발 환경의 재발송 제한을 유지한다.
    private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(5);

    private static final Duration VERIFIED_EXPIRATION = Duration.ofMinutes(10);

    private static final int MAX_ATTEMPTS = 5;

    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;

    // 일반 난수 대신 인증번호 생성에 적합한 난수 생성기를 사용한다.
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Value("${app.email.verification-secret}")
    private String verificationSecret;

    /**
     * 인증번호를 발송하고 Redis에 검증값을 저장한다.
     */
    public void sendCode(String email) {
        String normalizedEmail = normalizeEmail(email);

        String cooldownKey = cooldownKey(normalizedEmail);
        String codeKey = codeKey(normalizedEmail);
        String attemptsKey = attemptsKey(normalizedEmail);
        String verifiedKey = verifiedKey(normalizedEmail);

        // 제한 키가 없을 때만 생성하여 짧은 시간의 반복 발송을 막는다.
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(
                cooldownKey,
                "1",
                RESEND_COOLDOWN
        );

        if (!Boolean.TRUE.equals(acquired)) {
            throw new IllegalStateException("인증번호는 5초 후 다시 요청할 수 있습니다.");
        }

        String code = String.format("%06d", secureRandom.nextInt(1_000_000));

        String codeHash = hashCode(normalizedEmail, code);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(normalizedEmail);
        message.setSubject("[모의주식사이트] 이메일 인증번호");
        message.setText("이메일 인증번호는 [" + code + "] 입니다.\n" + "인증번호는 5분 동안 유효합니다.");

        try {
            mailSender.send(message);
        } catch (RuntimeException e) {
            // 이메일 발송에 실패하면 재시도할 수 있도록 제한을 해제한다.
            redisTemplate.delete(cooldownKey);

            throw new IllegalStateException("이메일 발송에 실패했습니다.", e);
        }

        // 발송이 완료된 인증번호의 검증값을 5분간 저장한다.
        redisTemplate.opsForValue().set(
                codeKey,
                codeHash,
                CODE_EXPIRATION
        );

        // 새 인증번호를 발송했으므로 이전 인증 상태를 초기화한다.
        redisTemplate.delete(attemptsKey);
        redisTemplate.delete(verifiedKey);
    }

    /**
     * 인증번호를 검사하고 성공하면 인증 완료 상태를 저장한다.
     */
    public boolean verifyCode(String email, String code) {
        String normalizedEmail = normalizeEmail(email);

        String codeKey = codeKey(normalizedEmail);
        String attemptsKey = attemptsKey(normalizedEmail);
        String verifiedKey = verifiedKey(normalizedEmail);

        String savedHash = redisTemplate.opsForValue().get(codeKey);

        // 인증번호가 없거나 만료된 경우다.
        if (savedHash == null) {
            return false;
        }

        String attemptsValue =
                redisTemplate.opsForValue().get(attemptsKey);

        int attempts = attemptsValue == null
                ? 0
                : Integer.parseInt(attemptsValue);

        if (attempts >= MAX_ATTEMPTS) {
            return false;
        }

        String inputHash = hashCode(normalizedEmail, code);

        // 문자열의 일반 비교 대신 검증값 비교용 메서드를 사용한다.
        boolean matches = MessageDigest.isEqual(
                savedHash.getBytes(StandardCharsets.UTF_8),
                inputHash.getBytes(StandardCharsets.UTF_8)
        );

        if (!matches) {
            Long failedCount =
                    redisTemplate.opsForValue().increment(attemptsKey);

            // 첫 실패 시 실패 횟수 키에도 만료시간을 설정한다.
            if (failedCount != null && failedCount == 1) {
                Long remainingSeconds = redisTemplate.getExpire(
                        codeKey,
                        TimeUnit.SECONDS
                );

                if (remainingSeconds != null && remainingSeconds > 0) {
                    redisTemplate.expire(
                            attemptsKey,
                            Duration.ofSeconds(remainingSeconds)
                    );
                }
            }

            // 실패 한도에 도달하면 해당 인증번호를 폐기한다.
            if (failedCount != null && failedCount >= MAX_ATTEMPTS) {
                redisTemplate.delete(codeKey);
                redisTemplate.delete(attemptsKey);
            }

            return false;
        }

        // 회원가입 서비스에서 확인할 인증 완료 상태를 저장한다.
        redisTemplate.opsForValue().set(
                verifiedKey,
                "true",
                VERIFIED_EXPIRATION
        );

        // 사용이 끝난 인증번호와 실패 횟수를 제거한다.
        redisTemplate.delete(codeKey);
        redisTemplate.delete(attemptsKey);

        return true;
    }

    /**
     * 회원가입 시 해당 이메일의 인증 완료 여부를 확인한다.
     */
    public boolean isVerified(String email) {
        String normalizedEmail = normalizeEmail(email);

        return "true".equals(redisTemplate.opsForValue().get(verifiedKey(normalizedEmail)));
    }

    /**
     * 회원가입이 완료된 이메일의 임시 인증 상태를 제거한다.
     */
    public void clearVerification(String email) {
        String normalizedEmail = normalizeEmail(email);
        redisTemplate.delete(verifiedKey(normalizedEmail));
    }

    // Redis 키는 목적별 접두사를 붙여 서로 구분한다.
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

    /**
     * 발송·검증·가입에서 같은 이메일을 같은 키로 조회하도록 정규화한다.
     */
    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * 이메일과 인증번호를 비밀키 기반 검증값으로 변환한다.
     */
    private String hashCode(String email, String code) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");

            SecretKeySpec secretKey = new SecretKeySpec(
                    verificationSecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );

            mac.init(secretKey);

            byte[] hash = mac.doFinal((email + ":" + code).getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("인증번호 검증값 생성에 실패했습니다.", e);
        }
    }
}