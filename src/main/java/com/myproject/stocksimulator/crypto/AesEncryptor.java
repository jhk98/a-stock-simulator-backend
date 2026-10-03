package com.myproject.stocksimulator.crypto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES 암호화 및 복호화를 담당하는 클래스.
 *
 * 주민등록번호처럼 원본 데이터를 다시 확인해야 할 수 있는
 * 민감정보를 안전하게 저장하기 위해 사용한다.
 *
 * 비밀번호에 사용하는 BCrypt는 단방향 해시이기 때문에
 * 원래 값을 복원할 수 없지만,
 * AES는 같은 비밀키를 이용하여 암호화와 복호화가 가능하다.
 *
 * 이 클래스에서는 AES/GCM/NoPadding 방식을 사용한다.
 */
@Component
public class AesEncryptor {

    /**
     * AES-GCM에서 사용하는 IV(Initialization Vector)의 길이.
     *
     * GCM에서는 일반적으로 12바이트(96bit) IV를 사용한다.
     */
    private static final int IV_LENGTH = 12;

    /**
     * GCM 인증 태그 길이.
     *
     * 128bit 인증 태그를 사용하여
     * 암호문이 변조되었는지도 검증할 수 있다.
     */
    private static final int TAG_LENGTH = 128;

    /**
     * AES 암호화에 사용할 비밀키.
     */
    private final SecretKeySpec secretKey;

    /**
     * 안전한 랜덤 IV를 생성하기 위해 사용한다.
     */
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * application.properties에 설정한
     * encryption.aes.key 값을 전달받는다.
     *
     * 실제 값은 MEMBER_AES_KEY 환경변수에서 가져온다.
     *
     * @param encodedKey Base64로 인코딩된 AES 비밀키
     */
    public AesEncryptor(
            @Value("${encryption.aes.key}") String encodedKey
    ) {

        // 환경변수에 저장되어 있는 Base64 문자열을
        // 실제 AES에서 사용할 byte 배열로 변환한다.
        byte[] keyBytes = Base64.getDecoder().decode(encodedKey);

        // AES-256을 사용하기 때문에
        // 복호화된 키는 정확히 32바이트여야 한다.
        if (keyBytes.length != 32) {
            throw new IllegalArgumentException(
                    "AES 키는 32바이트(AES-256)여야 합니다."
            );
        }

        // byte 배열을 AES에서 사용할 수 있는 SecretKeySpec 객체로 만든다.
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * 문자열을 AES 방식으로 암호화한다.
     *
     * 매번 새로운 랜덤 IV를 생성하기 때문에
     * 같은 주민등록번호를 여러 번 암호화하더라도
     * 서로 다른 암호문이 생성된다.
     *
     * 최종 저장 형태:
     *
     * Base64(IV + 암호문)
     *
     * @param plainText 암호화할 원본 문자열
     * @return Base64 형태의 암호화된 문자열
     */
    public String encrypt(String plainText) {

        try {
            // 12바이트 크기의 IV를 생성한다.
            byte[] iv = new byte[IV_LENGTH];

            // SecureRandom을 이용하여
            // 예측하기 어려운 랜덤 값을 IV에 채운다.
            secureRandom.nextBytes(iv);

            // AES/GCM 방식의 Cipher 객체를 생성한다.
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            // GCM에서 사용할 인증 태그 길이와 IV를 설정한다.
            GCMParameterSpec parameterSpec =
                    new GCMParameterSpec(TAG_LENGTH, iv);

            // Cipher를 암호화 모드로 초기화한다.
            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    secretKey,
                    parameterSpec
            );

            // 원본 문자열을 UTF-8 byte 배열로 변환한 뒤 암호화한다.
            byte[] encrypted =
                    cipher.doFinal(
                            plainText.getBytes(StandardCharsets.UTF_8)
                    );

            // 복호화할 때 IV도 필요하기 때문에
            // IV와 암호문을 하나의 byte 배열로 합친다.
            ByteBuffer byteBuffer =
                    ByteBuffer.allocate(iv.length + encrypted.length);

            byteBuffer.put(iv);
            byteBuffer.put(encrypted);

            // DB에는 byte[]를 직접 저장하지 않고
            // Base64 문자열로 변환해서 저장한다.
            return Base64.getEncoder()
                    .encodeToString(byteBuffer.array());

        } catch (Exception e) {
            throw new IllegalStateException(
                    "AES 암호화에 실패했습니다.",
                    e
            );
        }
    }

    /**
     * AES로 암호화된 문자열을 원본 문자열로 복호화한다.
     *
     * 암호화할 때 저장했던 데이터에서
     * IV와 실제 암호문을 분리한 뒤 같은 AES 비밀키로 복호화한다.
     *
     * @param encryptedText 암호화된 Base64 문자열
     * @return 복호화된 원본 문자열
     */
    public String decrypt(String encryptedText) {

        try {
            // DB에 저장된 Base64 문자열을
            // 원래 byte 배열로 변환한다.
            byte[] decoded =
                    Base64.getDecoder().decode(encryptedText);

            ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);

            // 앞쪽 12바이트는 암호화할 때 사용한 IV이다.
            byte[] iv = new byte[IV_LENGTH];
            byteBuffer.get(iv);

            // 나머지 부분은 실제 암호문이다.
            byte[] encrypted =
                    new byte[byteBuffer.remaining()];

            byteBuffer.get(encrypted);

            // 암호화할 때와 동일한 AES/GCM 방식을 사용한다.
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            GCMParameterSpec parameterSpec =
                    new GCMParameterSpec(TAG_LENGTH, iv);

            // 이번에는 복호화 모드로 초기화한다.
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    secretKey,
                    parameterSpec
            );

            // 암호문을 원래 byte 배열로 복호화한다.
            byte[] decrypted = cipher.doFinal(encrypted);

            // UTF-8 문자열로 변환하여 반환한다.
            return new String(
                    decrypted,
                    StandardCharsets.UTF_8
            );

        } catch (Exception e) {
            throw new IllegalStateException(
                    "AES 복호화에 실패했습니다.",
                    e
            );
        }
    }
}