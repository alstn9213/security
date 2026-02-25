package com.server.domain;

public enum PaymentStatus {
    READY,      // 결제 요청 전
    OK,         // 결제 성공 (검증 완료)
    FAILED,     // 결제 실패
    CANCELLED   // 결제 취소
}