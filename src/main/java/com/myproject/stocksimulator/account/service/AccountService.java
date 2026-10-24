package com.myproject.stocksimulator.account.service;

import com.myproject.stocksimulator.account.entity.Account;
import com.myproject.stocksimulator.account.repository.AccountRepository;
import com.myproject.stocksimulator.member.entity.Member;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

/**
 * 모의계좌 개설 기능
 *
 * 최초 개설 시 가상자금 1억 원을 지급한다.
 * 이미 계좌가 있으면 기존 계좌를 반환하며 돈을 다시 지급하지 않는다.
 */
@Service
public class AccountService {

    private final AccountRepository accounts;

    @PersistenceContext
    private EntityManager entityManager;

    public AccountService(AccountRepository accounts) {
        this.accounts = accounts;
    }

    /**
     * 기존 프론트와 호환되는 계좌 개설 응답
     * Entity를 직접 반환하지 않아 회원 정보가 노출되지 않는다.
     */
    public record AccountResponse(
            long id,
            String accountNum,
            long deposit,
            LocalDateTime createdAt
    ) {
    }

    @Transactional
    public AccountResponse openAccount(Long memberId) {
        if (memberId == null) {
            throw new BadCredentialsException("로그인이 필요합니다.");
        }

        /*
         * 계좌가 아직 없을 때는 계좌 행을 잠글 수 없다.
         * 대신 회원 행을 잠가 같은 회원의 동시 계좌 개설을 직렬화한다.
         */
        Member member = entityManager.find(
                Member.class,
                memberId,
                LockModeType.PESSIMISTIC_WRITE
        );

        if (member == null) {
            throw new BadCredentialsException("존재하지 않는 회원입니다.");
        }

        Account account = accounts.findByMemberId(memberId)
                .orElse(null);

        if (account == null) {
            account = Account.builder()
                    .member(member)
                    .accountNum(String.format(
                            Locale.ROOT,
                            "SIM-%020d",
                            memberId
                    ))
                    .deposit(Account.INITIAL_BALANCE)
                    .build();

            accounts.saveAndFlush(account);
        }

        return new AccountResponse(
                account.getId(),
                account.getAccountNum(),
                account.getDeposit(),
                account.getCreatedAt()
        );
    }
}