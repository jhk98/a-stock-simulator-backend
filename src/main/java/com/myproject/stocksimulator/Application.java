package com.myproject.stocksimulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 애플리케이션 진입점.
 *
 * <p>{@code @SpringBootApplication} 하나가 사실 세 개를 합친 것이다.
 * <ul>
 *   <li>{@code @SpringBootConfiguration} — 이 클래스가 설정 클래스임을 표시</li>
 *   <li>{@code @EnableAutoConfiguration} — classpath 에 뭐가 있는지 보고 알아서 설정.
 *       예를 들어 H2 가 있으면 DataSource 를, webmvc 가 있으면 내장 톰캣을 띄운다</li>
 *   <li>{@code @ComponentScan} — <b>이 클래스가 있는 패키지와 그 하위</b>에서
 *       {@code @Component}/{@code @Service}/{@code @Controller} 를 찾는다</li>
 * </ul>
 *
 * <p>⚠️ 마지막 항목 때문에 이 클래스는 <b>최상위 패키지</b>에 있어야 한다.
 * 하위 패키지로 옮기면 형제 패키지의 빈을 못 찾아서
 * "분명 @Service 붙였는데 주입이 안 된다" 는 상황이 생긴다.
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);

//        new Member();
//        new CustomAccountRepository();
//        new AccountRepository();
    }
}
