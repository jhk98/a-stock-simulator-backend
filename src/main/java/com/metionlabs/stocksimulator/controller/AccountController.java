package com.metionlabs.stocksimulator.controller;

import com.metionlabs.stocksimulator.controller.dto.account.AccountGetResult;
import com.metionlabs.stocksimulator.controller.dto.account.AccountPatchCommand;
import com.metionlabs.stocksimulator.controller.dto.account.AccountPostCommand;
import com.metionlabs.stocksimulator.controller.dto.account.AccountPostResult;
import com.metionlabs.stocksimulator.domain.Account;
import com.metionlabs.stocksimulator.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 계좌 API.
 *
 * <h3>⛔ 이 클래스가 지키는 단 하나의 규칙</h3>
 * <b>Controller 는 Service 만 안다. Repository 를 주입받지 않는다.</b>
 *
 * <p>검증 방법이 아주 간단하다 — 이 파일의 {@code import} 에 {@code ...repository...} 가
 * 있으면 잘못된 것이다. "적절히 분리하라" 같은 말보다 이 한 줄이 훨씬 강력하다.
 *
 * <p>이 규칙 하나에서 나머지가 따라온다:
 * <ul>
 *   <li>Controller 가 DB 를 못 만지니 → 조회를 Service 가 하게 되고</li>
 *   <li>조회와 저장이 한곳에 모이니 → {@code @Transactional} 을 걸 자리가 생기고</li>
 *   <li>그래서 → "트랜잭션 경계 = Service" 가 자연스럽게 성립한다</li>
 * </ul>
 *
 * <h3>Controller 가 책임지는 것</h3>
 * HTTP 에 관한 것만이다 — URL 경로, 상태 코드, 요청/응답 본문 변환.
 * 비즈니스 판단은 하지 않는다. 그래서 각 메서드가 세 줄을 넘지 않는다.
 *
 * <h3>DTO 는 왜 한 벌인가</h3>
 * {@code AccountPostCommand} 를 Service 까지 그대로 넘긴다. Controller 용과 Service 용을
 * 따로 두면 클래스가 두 배가 되고 <b>매핑 코드가 생기는데, 거기가 버그가 숨는 자리다.</b>
 * 계층 분리의 본질은 클래스 개수가 아니라 <b>의존 방향</b>이다 —
 * 위(Controller)가 아래(Service)를 아는 건 정상이고, 그 반대가 문제다.
 *
 * <p>DTO 를 두 벌로 나눌 때는 따로 있다. 아래 중 하나가 생기면 그때 나눈다:
 * <ol>
 *   <li>Service 를 부르는 곳이 둘 이상이 될 때 (REST + 배치 + 스케줄러…)</li>
 *   <li>모듈이 갈라져 Service 쪽이 웹 쪽을 import 할 수 없을 때</li>
 *   <li>웹 입력과 비즈니스 입력의 모양이 실제로 달라질 때</li>
 * </ol>
 * 지금은 셋 다 아니다. <b>필요해서 나누는 경험</b>이 미리 나눠둔 코드보다 많이 남는다.
 */
@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    // ========================================================================
    // C — POST /accounts
    // ========================================================================

    /**
     * 계좌 개설.
     *
     * <p>{@code @Valid} 를 붙여야 {@code AccountPostCommand} 의 검증 애노테이션이 동작한다.
     * 안 붙이면 <b>에러도 없이 검증이 통째로 무시된다.</b>
     *
     * <p>생성 성공은 201 Created 로 답하고 {@code Location} 헤더에 새 자원의 주소를 넣는 것이
     * REST 관례다. 여기서는 단순하게 200 + id 로 두되, 관례는 알아두자.
     */
    @PostMapping
    public ResponseEntity<AccountPostResult> create(@RequestBody @Valid AccountPostCommand command) {
        Account account = accountService.create(command);
        // 서비스는 도메인 객체를 주고, "응답에 무엇을 담을지" 는 Controller 가 정한다.
        // 지금은 id 만 필요해서 꺼내 쓴다.
        return ResponseEntity.ok(new AccountPostResult(account.getId()));
    }

    // ========================================================================
    // R — GET /accounts/{id}, GET /accounts?memberId=
    // ========================================================================

    /** 계좌 단건 조회. */
    @GetMapping("/{id}")
    public ResponseEntity<AccountGetResult> get(@PathVariable Long id) {
        Account account = accountService.getById(id);
        return ResponseEntity.ok(AccountGetResult.from(account));
    }

    /**
     * 회원의 계좌 조회.
     *
     * <p>{@code /accounts/{id}} 와 경로가 겹치지 않도록 쿼리 파라미터로 구분한다.
     * ({@code /accounts/member/1} 처럼 만들면 {@code /accounts/{id}} 와 헷갈릴 여지가 생긴다)
     */
    @GetMapping(params = "memberId")
    public ResponseEntity<AccountGetResult> getByMember(@RequestParam Long memberId) {
        Account account = accountService.getByMemberId(memberId);
        return ResponseEntity.ok(AccountGetResult.from(account));
    }

    // ========================================================================
    // U — PATCH /accounts/{id}
    // ========================================================================

    /**
     * 계좌 정보 부분 수정.
     *
     * <p>PATCH 이므로 <b>보낸 필드만</b> 바뀐다. 안 보낸 필드는 그대로다.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<AccountGetResult> update(@PathVariable Long id,
                                                   @RequestBody @Valid AccountPatchCommand command) {
        Account account = accountService.update(id, command);
        return ResponseEntity.ok(AccountGetResult.from(account));
    }

    // ========================================================================
    // D — DELETE /accounts/{id}
    // ========================================================================

    /**
     * 계좌 삭제.
     *
     * <p>삭제 성공에는 돌려줄 내용이 없으므로 204 No Content 가 맞다.
     * 지운 id 를 되돌려주는 건 의미가 없다 — 요청자가 이미 알고 있는 값이다.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        accountService.delete(id);
        return ResponseEntity.noContent().build();
    }
}