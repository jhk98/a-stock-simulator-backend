package com.myproject.stocksimulator.service.member;

import com.myproject.stocksimulator.domain.member.Member;
import com.myproject.stocksimulator.domain.member.MemberStatus;
import com.myproject.stocksimulator.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 탈퇴 회원의 실제 데이터 삭제를 처리하는 Service
 *
 * 회원 탈퇴 시에는 회원 데이터를 즉시 삭제하지 않고
 * MemberService에서 Soft Delete 방식으로 INACTIVE 상태로 변경한다.
 *
 * 이후 탈퇴일로부터 30일이 지난 회원을 조회하여
 * 실제 DB에서 삭제(Hard Delete)하는 작업을 담당한다.
 */
@Service
@Transactional
public class MemberCleanupService {

    /**
     * 회원 데이터를 조회하고 삭제하기 위해 사용하는 Repository
     */
    private final MemberRepository memberRepository;

    /**
     * 생성자 주입을 통해 MemberRepository를 전달받는다.
     *
     * @param memberRepository 회원 데이터 접근을 담당하는 Repository
     */
    public MemberCleanupService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /**
     * 탈퇴 후 30일이 지난 회원을 실제 DB에서 삭제한다.
     *
     * 현재 시간을 기준으로 30일 전 시간을 계산한 뒤
     *
     * 1. status가 INACTIVE이고
     * 2. withdrawnAt이 30일 전 시간보다 이전이거나 같은
     *
     * 회원들을 조회한다.
     *
     * 조회된 회원은 memberRepository.deleteAll()을 통해
     * 실제 DB에서 삭제(Hard Delete)한다.
     */
    public void deleteExpiredWithdrawnMembers() {

        // 현재 시간을 기준으로 30일 전 시간을 계산한다.
        //
        // 예)
        // 현재 시간이 2026-10-02이라면
        // 약 2026-09-02 이전에 탈퇴한 회원이 삭제 대상이 된다.
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);

        // INACTIVE 상태이면서 탈퇴 후 30일 이상 지난 회원을 조회한다.
        List<Member> members =
                memberRepository.findByStatusAndWithdrawnAtLessThanEqual(
                        MemberStatus.INACTIVE,
                        threshold
                );

        // 조회된 회원들을 실제 DB에서 삭제한다.
        //
        // MemberService.withdraw()의 Soft Delete와 달리
        // 여기서는 실제 DELETE SQL이 실행되는 Hard Delete이다.
        memberRepository.deleteAll(members);
    }
}