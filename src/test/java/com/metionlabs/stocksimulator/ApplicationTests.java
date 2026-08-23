package com.metionlabs.stocksimulator;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 스프링 컨텍스트가 정상적으로 뜨는지만 확인하는 테스트.
 *
 * <p>단순해 보이지만 값어치가 크다. 빈 설정이 잘못됐거나, 의존성이 빠졌거나,
 * DB 접속 설정이 틀리면 여기서 바로 실패한다.
 * 즉 "일단 켜지긴 하는가" 를 지켜주는 최소한의 안전망이다.
 */
@SpringBootTest
class ApplicationTests {

    @Test
    void 컨텍스트가_뜬다() {
        // 이 메서드가 비어 있는 게 맞다.
        // @SpringBootTest 가 컨텍스트를 띄우는 것 자체가 검증이다.
    }
}
