package com.metionlabs.stocksimulator.service;

import com.metionlabs.stocksimulator.controller.dto.holding.HoldingPatchCommand;
import com.metionlabs.stocksimulator.controller.dto.holding.HoldingPostCommand;
import com.metionlabs.stocksimulator.domain.Account;
import com.metionlabs.stocksimulator.domain.Holding;
import com.metionlabs.stocksimulator.domain.stock.Stock;
import com.metionlabs.stocksimulator.repository.AccountRepository;
import com.metionlabs.stocksimulator.repository.HoldingRepository;
import com.metionlabs.stocksimulator.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class HoldingService {

    private final HoldingRepository holdingRepository;
    private final StockRepository stockRepository;
    private final AccountRepository accountRepository;

    // 생성자 주입
    public HoldingService(HoldingRepository holdingRepository,
                          StockRepository stockRepository,
                          AccountRepository accountRepository) {
        this.holdingRepository = holdingRepository;
        this.stockRepository = stockRepository;
        this.accountRepository = accountRepository;
    }

    // ========================================================================
    // C — 생성
    // ========================================================================

    // 회원 보유 주식 생성
    public Holding create(HoldingPostCommand command) {
        // 주식이 실제로 존재하는지 확인
        Stock stock = stockRepository.findById(command.getStockId())
                .orElseThrow(() -> new NoSuchElementException(
                        "주식을 찾을 수 없습니다. stockId = " + command.getStockId()));

        // 계좌가 실제로 존재하는지 확인
        Account account = accountRepository.findById(command.getAccountId())
                .orElseThrow(() -> new NoSuchElementException(
                        "계좌를 찾을 수 없습니다. accountId = " + command.getAccountId()));

        // 같은 계좌가 같은 주식을 이미 보유하고 있는지 확인
        if(holdingRepository.existsByStockIdAndAccountId(command.getStockId(), command.getAccountId())) {
            throw new IllegalStateException("이미 보유하고 있는 주식입니다. stockId = "
                                                + command.getStockId()
                                                + ", accountId = "
                                                + command.getAccountId());
        }

        LocalDateTime now = LocalDateTime.now();

        Holding holding = Holding.builder()
                .stock(stock)
                .account(account)
                .quantity(command.getQuantity())
                .avgPrice(command.getAvgPrice())
                .createdAt(now)
                .modifiedAt(now)
                .build();

        return holdingRepository.save(holding);
    }

    // ========================================================================
    // R — 조회
    // ========================================================================

    // id로 조회 없으면 예외
    @Transactional(readOnly = true)
    public Holding getById(Long id) {
        return holdingRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("보유 주식을 찾을 수 없습니다. id = " + id));
    }

    // 계좌 id로 조회 없으면 예외
    @Transactional(readOnly = true)
    public List<Holding> getByAccountId(Long accountId) {
        List<Holding> holdings = holdingRepository.findByAccountId(accountId);

        if(holdings.isEmpty()) {
            throw new NoSuchElementException(
                    "해당 계좌의 보유 주식이 없습니다. accountId = " + accountId);
        }

        return holdings;
    }

    // ========================================================================
    // U — 수정
    // ========================================================================

    // 회원 보유 주식 수정
    public Holding update(Long id, HoldingPatchCommand command) {
        Holding holding = getById(id);

        holding.update(
                command.getQuantity(),
                command.getAvgPrice()
        );

        return holding;
    }

    // ========================================================================
    // D — 삭제
    // ========================================================================

    // 회원 보유 주식 삭제
    public void delete(Long id) {
        Holding holding = getById(id);
        holdingRepository.delete(holding);
    }
}