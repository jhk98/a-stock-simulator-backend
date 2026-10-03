package com.myproject.stocksimulator.scheduler;

import com.myproject.stocksimulator.service.member.MemberCleanupService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 탈퇴 회원의 Hard Delete 작업을 자동으로 실행하는 Scheduler.
 *
 * 회원이 탈퇴하면 즉시 DB에서 삭제하지 않고
 * INACTIVE 상태로 변경하여 30일 동안 데이터를 보관한다.
 *
 * 이 Scheduler는 정해진 시간마다 MemberCleanupService를 호출하여
 * 탈퇴 후 30일이 지난 회원을 실제 DB에서 삭제하는 작업을 실행한다.
 */
@Component
public class MemberCleanupScheduler {

    /**
     * 탈퇴 회원의 실제 삭제 로직을 처리하는 Service.
     *
     * Scheduler는 삭제 조건이나 DB 처리 로직을 직접 구현하지 않고
     * MemberCleanupService에 실제 작업을 위임한다.
     */
    private final MemberCleanupService memberCleanupService;

    /**
     * 생성자 주입을 통해 MemberCleanupService를 전달받는다.
     *
     * @param memberCleanupService 탈퇴 회원 정리 작업을 처리하는 Service
     */
    public MemberCleanupScheduler(MemberCleanupService memberCleanupService) {
        this.memberCleanupService = memberCleanupService;
    }

    /**
     * 탈퇴 후 30일이 지난 회원을 정기적으로 삭제한다.
     *
     * cron = "0 0 3 * * *"
     *
     * 초   : 0
     * 분   : 0
     * 시   : 3
     * 일   : 매일
     * 월   : 매월
     * 요일 : 모든 요일
     *
     * 따라서 매일 오전 3시에 실행된다.
     *
     * zone = "Asia/Seoul"
     * 서버의 운영체제 시간대와 관계없이
     * 한국 시간 기준 오전 3시에 실행되도록 설정한다.
     */
    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    public void cleanupWithdrawnMembers() {

        // 실제 삭제 조건 확인 및 Hard Delete 처리는
        // MemberCleanupService에서 담당한다.
        memberCleanupService.deleteExpiredWithdrawnMembers();
    }
}