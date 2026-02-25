package com.server.domain;

public enum OrderStatus {
    READY,      // 주문 생성 (결제 대기)
    PAID,       // 결제 완료
    CANCELLED   // 주문 취소
}