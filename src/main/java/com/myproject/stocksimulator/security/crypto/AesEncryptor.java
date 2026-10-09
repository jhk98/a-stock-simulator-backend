package com.myproject.stocksimulator.security.crypto;

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
 * AES 암호화 및 복호화를 담당하는 클래스
 *
 * <p>주민등록번호처럼 원본 데이터를 다시 확인해야 할 수 있는
 * 민감정보를 암호화하여 저장하기 위해 사용한다.</p>
 *
 * <p>비밀번호에 사용하는 BCrypt는 단방향 해시이므로
 * 원래 값을 복원할 수 없지만,
 * AES는 비밀키를 이용하여 암호화와 복호화가 가능하다.</p>
 *
 * <p>암호화 알고리즘은 AES/GCM/NoPadding을 사용한다.</p>
 *
 * <p>AES-GCM은 데이터 암호화뿐 아니라
 * 암호문의 무결성 검증도 제공한다.</p>
 */
@Component
public class AesEncryptor {

    /**
     * AES-GCM에서 사용하는 IV(Initialization Vector)의 길이
     *
     * <p>GCM에서는 일반적으로 12바이트(96bit) IV를 사용한다.</p>
     */
    private static final int IV_LENGTH = 12;

    /**
     * GCM 인증 태그 길이
     *
     * <p>128bit 인증 태그를 사용하여
     * 암호문이 변조되었는지 검증한다.</p>
     */
    private static final int TAG_LENGTH = 128;

    /**
     * AES 암호화 및 복호화에 사용할 비밀키
     */
    private final SecretKeySpec secretKey;

    /**
     * 암호화할 때마다 새로운 랜덤 IV를 생성하기 위한 객체
     */
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * AES 비밀키 초기화
     *
     * <p>application.properties의 encryption.aes.key 설정값을
     * Spring으로부터 주입받는다.</p>
     *
     * <p>실제 비밀키는 MEMBER_AES_KEY 환경변수에서 관리한다.</p>
     *
     * <p>비밀키는 Base64로 인코딩된 문자열이며,
     * 디코딩한 결과가 32바이트여야 AES-256을 사용할 수 있다.</p>
     *
     * @param encodedKey Base64로 인코딩된 AES 비밀키
     */
    public AesEncryptor(@Value("${encryption.aes.key}") String encodedKey) {

        // 환경변수에 저장되어 있는 Base64 문자열을 실제 AES에서 사용할 byte 배열로 변환한다.
        byte[] keyBytes = Base64.getDecoder().decode(encodedKey);

        // AES-256을 사용하기 위해 32바이트인지 확인한다.
        if (keyBytes.length != 32) {
            throw new IllegalArgumentException(
                    "AES 키는 32바이트(AES-256)여야 합니다."
            );
        }

        // 바이트 배열을 AES 비밀키 객체로 생성한다.
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * 문자열을 AES-GCM 방식으로 암호화
     *
     * <p>암호화할 때마다 새로운 랜덤 IV를 생성한다.</p>
     *
     * <p>따라서 같은 주민등록번호를 여러 번 암호화하더라도
     * 서로 다른 암호문이 생성된다.</p>
     *
     * <p>DB에 저장하는 데이터 형식은 다음과 같다.</p>
     *
     * <p>Base64(IV + 암호문 + 인증 태그)</p>
     *
     * <p>IV는 복호화할 때 필요하므로 암호문과 함께 저장한다.
     * IV 자체는 비밀값이 아니지만, 동일한 AES 키로
     * 암호화할 때 재사용되지 않아야 한다.</p>
     *
     * @param plainText 암호화할 원본 문자열
     * @return Base64로 인코딩된 암호문
     */
    public String encrypt(String plainText) {
        try {
            // 1. 새로운 12바이트 IV 생성
            byte[] iv = new byte[IV_LENGTH];

            // 암호학적으로 안전한 난수로 IV를 채운다.
            secureRandom.nextBytes(iv);

            // 2. AES-GCM 암호화 객체 생성
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            // 3. 인증 태그 길이와 IV 설정
            GCMParameterSpec parameterSpec =
                    new GCMParameterSpec(TAG_LENGTH, iv);

            // 4. 암호화 모드 초기화
            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    secretKey,
                    parameterSpec
            );

            // 5. 원본 문자열을 UTF-8 바이트 배열로 변환하여 암호화
            byte[] encrypted = cipher.doFinal(
                    plainText.getBytes(StandardCharsets.UTF_8)
            );

            // 6. IV와 암호문을 하나의 바이트 배열로 합친다.
            // encrypted에는 GCM 인증 태그도 포함되어 있다.
            ByteBuffer byteBuffer =
                    ByteBuffer.allocate(iv.length + encrypted.length);

            byteBuffer.put(iv);
            byteBuffer.put(encrypted);

            // 7. DB에 저장할 수 있도록 Base64 문자열로 변환
            return Base64.getEncoder()
                    .encodeToString(byteBuffer.array());
        } catch (Exception e) {
            // 내부 예외는 유지하되 민감한 원본 데이터는 기록하지 않는다.
            throw new IllegalStateException("AES 암호화에 실패했습니다.", e);
        }
    }

    /**
     * AES-GCM으로 암호화된 문자열을 복호화
     *
     * <p>DB에 저장된 Base64 문자열을 디코딩한 뒤
     * IV와 암호문을 분리한다.</p>
     *
     * <p>암호화할 때 사용한 동일한 비밀키와 IV로
     * 원본 문자열을 복원한다.</p>
     *
     * <p>복호화 과정에서 인증 태그를 검증하므로
     * 암호문이 변조되었거나 키가 일치하지 않으면
     * 복호화에 실패한다.</p>
     *
     * @param encryptedText Base64로 인코딩된 암호문
     * @return 복호화된 원본 문자열
     */
    public String decrypt(String encryptedText) {
        try {
            // 1. Base64 문자열을 바이트 배열로 변환
            byte[] decoded =
                    Base64.getDecoder().decode(encryptedText);

            // 2. 암호문 최소 길이 검증
            // IV 12바이트 + 인증 태그 16바이트가 필요하다.
            if (decoded.length < IV_LENGTH + TAG_LENGTH / 8) {
                throw new IllegalArgumentException(
                        "올바르지 않은 AES 암호문 형식입니다."
                );
            }

            // 3. 디코딩한 데이터를 ByteBuffer로 감싼다.
            ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);

            // 4. 앞쪽 12바이트 IV 추출
            byte[] iv = new byte[IV_LENGTH];
            byteBuffer.get(iv);

            // 5. 나머지 암호문 및 인증 태그 추출
            byte[] encrypted =
                    new byte[byteBuffer.remaining()];

            byteBuffer.get(encrypted);

            // 6. AES-GCM 복호화 객체 생성
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            // 7. 암호화할 때 사용한 IV와 태그 길이 설정
            GCMParameterSpec parameterSpec =
                    new GCMParameterSpec(TAG_LENGTH, iv);

            // 8. 복호화 모드 초기화
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    secretKey,
                    parameterSpec
            );

            // 9. 인증 태그 검증 및 복호화 수행
            byte[] decrypted = cipher.doFinal(encrypted);

            // 10. 복호화된 바이트 배열을 UTF-8 문자열로 변환
            return new String(
                    decrypted,
                    StandardCharsets.UTF_8
            );
        } catch (Exception e) {
            // 복호화 실패 원인은 서버 내부에서만 확인하도록 한다.
            throw new IllegalStateException("AES 복호화에 실패했습니다.", e);
        }
    }
}