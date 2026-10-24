package com.myproject.stocksimulator.stock.entity.orders;

/**
 * 모의 주식 거래 방향
 * DB에는 enum 이름을 문자열로 저장한다.
 */
public enum OrderType {

    // 주식을 사고 계좌 현금을 차감한다.
    BUY,

    // 보유 주식을 팔고 계좌 현금을 증가시킨다.
    SELL
}