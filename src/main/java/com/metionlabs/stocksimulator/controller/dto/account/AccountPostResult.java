package com.metionlabs.stocksimulator.controller.dto.account;

import lombok.Getter;

/**
 * 계좌 개설 응답.
 *
 * <p>생성 직후에는 id 만 돌려준다. 상세가 필요하면 클라이언트가 {@code GET /accounts/{id}} 를
 * 부르면 된다. 응답에 이것저것 채워 넣을수록 <b>바꾸기 어려워진다</b> —
 * 한 번 나간 응답 형태는 사실상 계약이라, 나중에 필드를 빼면 클라이언트가 깨진다.
 */
@Getter
public class AccountPostResult {

    private final Long accountId;

    public AccountPostResult(Long accountId) {
        this.accountId = accountId;
    }
}