package com.metionlabs.stocksimulator.controller.dto.account;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 계좌 부분 수정 요청(PATCH).
 *
 * <p><b>모든 필드가 선택이다.</b> null 이면 "이 필드는 안 바꾼다" 는 뜻이다.
 * 그래서 {@code @NotNull} 을 붙이면 안 된다 — 붙이는 순간 전부 필수가 되어
 * PATCH 가 아니라 PUT 이 되어버린다.
 *
 * <p>단 <b>값을 보냈다면</b> 그 값은 유효해야 하므로 {@code @PositiveOrZero} 는 남긴다.
 * (검증 애노테이션은 값이 null 이면 통과시키고, 값이 있을 때만 검사한다)
 *
 * <p>{@code toAccount()} 를 없앤 이유는 {@link AccountPostCommand} 와 같다.
 * 게다가 이 메서드는 <b>id 없는 새 Account 를 만들어</b> 반환했는데, 그걸 저장하면
 * 수정이 아니라 새 행이 하나 더 생긴다. 수정은 "기존 엔티티의 값을 바꾸는 것"이다.
 */
@Getter
@NoArgsConstructor
public class AccountPatchCommand {

    private String accountNum;

    @PositiveOrZero(message = "예수금은 0 이상이어야 합니다.")
    private Long deposit;
}
