package com.metionlabs.stocksimulator.util;

import com.metionlabs.stocksimulator.domain.member.MemberRole;
import com.metionlabs.stocksimulator.domain.member.MemberStatus;
import com.metionlabs.stocksimulator.domain.orders.OrderStatus;
import com.metionlabs.stocksimulator.domain.orders.OrderType;
import com.metionlabs.stocksimulator.domain.stock.StockType;

import java.math.BigDecimal;

public class TestRandomGenerator {
    // -------------------------------------------------------------------------
    // Member 테이블
    // -------------------------------------------------------------------------

    // 로그인 아이디 랜덤 생성
    public static String randomLoginId() {
        // 랜덤 문자열
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        // 랜덤 문자열 객체 생성
        StringBuilder randomstr = new StringBuilder();

        // 랜덤으로 문자열을 만들기 위해 반복문 사용
        for(int i = 0; i < 6; i++) {
            int index = (int) (Math.random() * alphabet.length());
            randomstr.append(alphabet.charAt(index));
        }

        // 랜덤 정수 가져오기 (0 ~ 999)
        int randomNum = (int) (Math.random() * 1000);

        // 랜덤 문자열 + 랜덤 정수 반환
        return randomstr + String.format("%03d", randomNum);
    }

    // 이름 랜덤 생성
    public static String randomName() {
        String hangeul = "가나다라마바사아자차카타파하";
        StringBuilder randomStr = new StringBuilder();

        for(int i = 0; i < 2; i++) {
            int index = (int) (Math.random() * hangeul.length());
            randomStr.append(hangeul.charAt(index));
        }

        return randomStr.toString();
    }

    // 이메일 랜덤 생성
    public static String randomEmail() {
        // 랜덤 문자열
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        // 랜덤 문자열 객체 생성
        StringBuilder randomStr = new StringBuilder();

        for(int i = 0; i < 5; i++) {
            int index = (int) (Math.random() * alphabet.length());
            randomStr.append(alphabet.charAt(index));
        }

        // 숫자 2자리 랜덤 생성
        int randomNum = (int) (Math.random() * 100);

        // @naver.com, @gmail.com 둘 중 하나 랜덤으로 선택
        String domain = Math.random() < 0.5 ? "@naver.com" : "@gmail.com";

        return randomStr + String.format("@02d", randomNum) + domain;
    }

    // 전화번호 랜덤 생성
    public static String randomPhoneNumber() {
        // 첫 번째 3자리
        int first = (int) (Math.random() * 1000);

        // 두 번째 4자리
        int second = (int) (Math.random() * 10000);

        // 세 번재 4자리
        int third = (int) (Math.random() * 10000);

        return String.format("%03d-%04d-%04d", first, second, third);
    }

    // 주민등록번호 랜덤 생성
    public static String randomRgstNumber() {
        // 첫 번째 6자리
        int first = (int) (Math.random() * 1000000);

        // 두 번째 7자리
        int second = (int) (Math.random() * 10000000);

        return String.format("%06d-%07d", first, second);
    }

    // 비밀번호 랜덤 생성
    public static String randomPasswd() {
        // 랜덤 문자열
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        // 랜덤 문자열 객체 생성
        StringBuilder randomstr = new StringBuilder();

        // 랜덤으로 문자열을 만들기 위해 반복문 사용
        for(int i = 0; i < 7; i++) {
            int index = (int) (Math.random() * alphabet.length());
            randomstr.append(alphabet.charAt(index));
        }

        // 랜덤 정수 가져오기 (0 ~ 999)
        int randomNum = (int) (Math.random() * 1000);

        // 랜덤 문자열 + 랜덤 정수 반환
        return randomstr + String.format("%03d", randomNum);
    }

    // 회원 상태 랜덤 생성
    public static MemberStatus randomStatus() {
        // Math.random() 값이 0이상 0.5미만인 경우는 ACTIVE, 0.5이상 1미만인 경우는 INACTIVE
        return Math.random() < 0.5 ? MemberStatus.ACTIVE : MemberStatus.INACTIVE;
    }

    // 회원 역할 랜덤 생성
    public static MemberRole randomRole() {
        // Math.random() 값이 0이상 0.5미만인 경우는 USER, 0.5이상 1미만인 경우는 ADMIN
        return Math.random() < 0.5 ? MemberRole.USER : MemberRole.ADMIN;
    }

    // -------------------------------------------------------------------------
    // Stock 테이블
    // -------------------------------------------------------------------------

    // 주식코드 랜덤 생성
    public static String randomStockCode() {
        // 랜덤 문자열
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        // 랜덤 문자열 객체 생성
        StringBuilder randomStr = new StringBuilder();

        // 랜덤으로 문자열을 만들기 위해 반복문 사용
        for(int i = 0; i < 4; i++) {
            // 1. Math.random() 은 0.0 이상 1.0 미만의 랜덤 실수 생성
            // 2. Math.random() * 26 은 0 이상 26미만의 실수가 만들어짐
            // 3. 형변환(Casting)으로 실수에서 정수로 표현
            int index = (int) (Math.random() * alphabet.length());

            // alphabet에서 index번째 인데스의 문자를 가져와 randomStr에 추가
            randomStr.append(alphabet.charAt(index));
        }

        // 랜덤 정수 가져오기 (0 ~ 999)
        int randomNum = (int) (Math.random() * 1000);

        // 랜덤 문자열 + "-" + 랜덤 정수 반환
        // String.format 은 원하는 형식으로 문자열 생성
        // %03d -> %: 형식 지정 / 0: 빈자리를 0으로 채움 / 3: 최소 3자리로 생성 / d: 정수를 다룸
        return randomStr + "-" + String.format("%03d", randomNum);
    }

    // 주식이름 랜덤 생성
    public static String randomStockName() {
        String hangeul = "가나다라마바사아자차카타파하";
        StringBuilder randomStr = new StringBuilder();

        for(int i = 0; i < 5; i++) {
            int index = (int) (Math.random() * hangeul.length());
            randomStr.append(hangeul.charAt(index));
        }

        // randomStr 타입은 StringBuilder 타입이기 때문에 toString() 을 써서 String 타입으로 변환
        return randomStr.toString();
    }

    // 주식타입 (KOSPI / KOSDAQ) 랜덤 생성
    public static StockType randomStockType() {
        // Math.random() 값이 0이상 0.5미만인 경우는 KOSPI, 0.5이상 1미만인 경우는 KOSDAQ
        return Math.random() < 0.5 ? StockType.KOSPI : StockType.KOSDAQ;
    }

    // -------------------------------------------------------------------------
    // Account 테이블
    // -------------------------------------------------------------------------

    // 계좌번호 랜덤 생성
    public static String randomAccountNum() {
        // 첫 번째 4자리
        int first = (int) (Math.random() * 10000);

        // 두 번째 2자리
        int second = (int) (Math.random() * 100);

        // 세 번째 7자리
        int third = (int) (Math.random() * 10000000);

        return String.format("%04d-%02d-%07d", first, second, third);
    }

    // 예수금 랜덤 생성
    public static Long randomDeposit() {
        return (long) (Math.random() * 100000000);
    }

    // -------------------------------------------------------------------------
    // Holding 테이블
    // -------------------------------------------------------------------------

    // 보유 수량 랜덤 생성
    public static int randomQuantityH() {
        return (int) (Math.random() * 100);
    }

    // 평균 금액 랜덤 생성
    public static Long randomAvgPrice() {
        return (long) (Math.random() * 100000000);
    }

    // -------------------------------------------------------------------------
    // Order 테이블
    // -------------------------------------------------------------------------

    // 주문 종류 (BUY / SELL) 랜덤 생성
    public static OrderType randomOrderType() {
        return Math.random() < 0.5 ? OrderType.BUY : OrderType.SELL;
    }

    // 주문 가격 랜덤 생성
    public static Long randomOrderPrice() {
        return (long) (Math.random() * 100000000);
    }

    // 주문 상태 (WAIT / COMPLETE / CANCEL) 랜덤 생성
    public static OrderStatus randomOrderStatus() {
        return Math.random() < 0.5 ? OrderStatus.COMPLETE : OrderStatus.CANCEL;
    }

    // 주문 수량 랜덤 생성
    public static int randomQuantityO() {
        return (int) (Math.random() * 100);
    }

    // -------------------------------------------------------------------------
    // DailyPrice 테이블
    // -------------------------------------------------------------------------

    // 시가 랜덤 생성
    public static Long randomOpenPrice() {
        return (long) (Math.random() * 100000);
    }

    // 종가 랜덤 생성
    public static Long randomClosePrice() {
        return (long) (Math.random() * 100000);
    }

    // 고가 랜덤 생성
    public static Long randomHighPrice() {
        return (long) (Math.random() * 100000);
    }

    // 저가 랜덤 생성
    public static Long randomLowPrice() {
        return (long) (Math.random() * 100000);
    }

    // 거래량 랜덤 생성
    public static Long randomVolume() {
        return (long) (Math.random() * 1000000);
    }

    // -------------------------------------------------------------------------
    // CurrentPrice 테이블
    // -------------------------------------------------------------------------

    // 현재가 랜덤 생성
    public static Long randomCurrentPrice() {
        return (long) (Math.random() * 100000);
    }

    // 변동가 랜덤 생성
    public static Long randomChangePrice() {
        return (long) (Math.random() * 1000);
    }

    // 변동 비율 랜덤 생성
    public static BigDecimal randomChangeRate() {
        return BigDecimal.valueOf(Math.random() * 100);
    }

    // 현재 거래량 랜덤 생성
    public static Long randomCurrentVolume() {
        return (long) (Math.random() * 1000);
    }
}