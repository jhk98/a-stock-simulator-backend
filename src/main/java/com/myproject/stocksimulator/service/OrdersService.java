package com.myproject.stocksimulator.service;

import com.myproject.stocksimulator.controller.dto.orders.OrdersPatchCommand;
import com.myproject.stocksimulator.controller.dto.orders.OrdersPostCommand;
import com.myproject.stocksimulator.domain.Account;
import com.myproject.stocksimulator.domain.orders.Orders;
import com.myproject.stocksimulator.domain.stock.Stock;
import com.myproject.stocksimulator.repository.AccountRepository;
import com.myproject.stocksimulator.repository.OrdersRepository;
import com.myproject.stocksimulator.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class OrdersService {

    private final OrdersRepository ordersRepository;
    private final StockRepository stockRepository;
    private final AccountRepository accountRepository;

    // 생성자 주입
    public OrdersService(OrdersRepository ordersRepository,
                         StockRepository stockRepository,
                         AccountRepository accountRepository) {
        this.ordersRepository = ordersRepository;
        this.stockRepository = stockRepository;
        this.accountRepository = accountRepository;
    }

    // ========================================================================
    // C — 생성
    // ========================================================================

    // 주문 생성
    public Orders create(OrdersPostCommand command) {
        // 주식이 실제로 존재하는지 확인
        Stock stock = stockRepository.findById(command.getStockId())
                .orElseThrow(() -> new NoSuchElementException(
                        "주식을 찾을 수 없습니다. stockId=" + command.getStockId()));

        // 계좌가 실제로 존재하는지 확인
        Account account = accountRepository.findById(command.getAccountId())
                .orElseThrow(() -> new NoSuchElementException(
                        "계좌를 찾을 수 없습니다. accountId=" + command.getAccountId()));

        LocalDateTime now = LocalDateTime.now();
        Orders orders = Orders.builder()
                .stock(stock)
                .account(account)
                .orderType(command.getOrderType())
                .orderPrice(command.getOrderPrice())
                .orderStatus(command.getOrderStatus())
                .quantity(command.getQuantity())
                .createdAt(now)
                .build();

        return ordersRepository.save(orders);
    }

    // ========================================================================
    // R — 조회
    // ========================================================================

    // id로 조회 없으면 예외
    @Transactional(readOnly = true)
    public Orders getById(Long id) {
        return ordersRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("주문을 찾을 수 없습니다. id=" + id));
    }

    // 주식 id로 조회 없으면 예외
    @Transactional(readOnly = true)
    public List<Orders> getByStockId(Long stockId) {
        List<Orders> orders = ordersRepository.findByStockId(stockId);

        if(orders.isEmpty()) {
            throw new NoSuchElementException(
                    "해당 주식의 주문이 없습니다. stockId = " + stockId);
        }

        return orders;
    }

    // 계좌 id로 조회 없으면 예외
    @Transactional(readOnly = true)
    public List<Orders> getByAccountId(Long accountId) {
        List<Orders> orders = ordersRepository.findByAccountId(accountId);

        if(orders.isEmpty()) {
            throw new NoSuchElementException(
                    "해당 계좌의 주문이 없습니다. accountId = " + accountId);
        }

        return orders;
    }

    // ========================================================================
    // U — 수정
    // ========================================================================

    // 주문 수정
    public Orders update(Long id, OrdersPatchCommand command) {
        Orders orders = getById(id);

        orders.update(
                command.getOrderPrice(),
                command.getQuantity()
        );

        return orders;
    }

    // ========================================================================
    // D — 삭제
    // ========================================================================

    // 주문 삭제
    public void delete(Long id) {
        Orders orders = getById(id);
        ordersRepository.delete(orders);
    }
}