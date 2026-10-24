package com.myproject.stocksimulator.stock.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * 일반 예약 작업과 시세 수집 작업의 실행 스레드를 분리한다.
 *
 * <p>
 * 외부 API 요청 간격과 동시 요청 제한은
 * 기존 KiwoomHttpClient가 계속 담당한다.
 * </p>
 */
@Configuration
public class SchedulingConfig {

    /**
     * 종목 목록 동기화, 거래대금 순위 등 기존 예약 작업용
     */
    @Bean(name = "taskScheduler")
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

        scheduler.setPoolSize(2);
        scheduler.setThreadNamePrefix("scheduled-");

        return scheduler;
    }

    /**
     * 시세 수집 전용
     *
     * <p> 하나의 작업만 실행하여 수집 요청이 무제한으로 쌓이지 않게 한다. </p>
     */
    @Bean(name = "quoteTaskScheduler")
    public ThreadPoolTaskScheduler quoteTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("quote-collector-");

        return scheduler;
    }
}