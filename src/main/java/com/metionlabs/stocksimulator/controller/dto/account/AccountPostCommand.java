package com.metionlabs.stocksimulator.controller.dto.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 계좌 개설 요청.
 *
 * <p>⭐ 예전에 있던 {@code toAccount(Member)} 를 <b>없앴다.</b> 엔티티를 조립하려면
 * {@code Member} 를 DB 에서 꺼내야 하는데, 그러면 Controller 가 Repository 를 갖게 된다.
 * <b>DB 조회가 필요한 조립은 DTO 가 할 일이 아니다.</b> 조립은 Service 가 한다.
 *
 * <p>{@code createdAt}/{@code modifiedAt} 필드도 뺐다. 생성 시각은 서버가 정하는 값이라
 * 요청 본문에 있으면 안 된다. 예전 코드는 그 값을 받아놓고 {@code now()} 로 덮어썼는데,
 * <b>받아놓고 조용히 버리는 필드</b>는 API 를 쓰는 쪽을 혼란스럽게 만든다.
 *
 * <p>⚠️ Jackson 이 JSON → 객체로 만들 때 기본 생성자가 필요하다. {@code @NoArgsConstructor}
 * 가 없으면 역직렬화가 실패한다.
 */

// 계좌 생성 시 필요한 정보
@Getter
@NoArgsConstructor
public class AccountPostCommand {

    @NotNull(message = "memberId 는 필수입니다.")
    private Long memberId;

    @NotBlank(message = "계좌번호는 필수입니다.")
    private String accountNum;

    /** 예수금. 개설 시 0원도 허용하되 음수는 막는다. */
    @NotNull(message = "예수금은 필수입니다.")
    @PositiveOrZero(message = "예수금은 0 이상이어야 합니다.")
    private Long deposit;
}
