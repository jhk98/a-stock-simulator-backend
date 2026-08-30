package com.metionlabs.stocksimulator.controller.dto.account;

import com.metionlabs.stocksimulator.domain.Account;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 계좌 조회 응답.
 *
 * <h3>🔴 엔티티를 그대로 응답에 넣지 않는다</h3>
 * 예전 코드는 {@code Member member} 필드를 그대로 갖고 있었다. 그러면:
 * <ul>
 *   <li>회원의 <b>비밀번호·주민번호까지 JSON 에 실려 나간다</b></li>
 *   <li>지연 로딩이면 직렬화 시점에 세션이 닫혀 있어 예외가 난다</li>
 *   <li>엔티티에 필드를 하나 추가하는 순간 <b>API 응답이 말없이 바뀐다</b></li>
 * </ul>
 * 그래서 필요한 {@code memberId} 만 꺼내 담는다.
 *
 * <h3>🐛 예전 코드의 실제 버그</h3>
 * 생성자에서 {@code deposit} 대입이 <b>빠져 있었다.</b> 필드는 선언돼 있으니 컴파일도 되고
 * 테스트도 없어서, <b>예수금이 항상 null 로 응답되는</b> 상태였다.
 * 손으로 필드를 하나씩 옮겨 적는 매핑 코드는 이런 누락이 잘 생긴다 —
 * DTO 계층을 늘릴수록 이런 자리도 같이 늘어난다.
 */
@Getter
public class AccountGetResult {

    private final Long id;
    private final Long memberId;
    private final String accountNum;
    private final Long deposit;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;

    private AccountGetResult(Account account) {
        this.id = account.getId();
        this.memberId = account.getMember() != null ? account.getMember().getId() : null;
        this.accountNum = account.getAccountNum();
        this.deposit = account.getDeposit();
        this.createdAt = account.getCreatedAt();
        this.modifiedAt = account.getModifiedAt();
    }

    /**
     * 엔티티 → 응답 DTO.
     *
     * <p>생성자 대신 정적 팩터리를 쓰면 호출부가 {@code AccountGetResult.from(account)} 로
     * 읽혀서 "무엇으로부터 만드는지" 가 이름에 드러난다.
     */
    public static AccountGetResult from(Account account) {
        return new AccountGetResult(account);
    }
}