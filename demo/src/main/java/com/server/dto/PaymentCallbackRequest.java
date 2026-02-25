package com.server.dto;

import lombok.Data;

@Data
public class PaymentCallbackRequest {
    private String paymentUid; // 결제 고유 번호 (imp_uid)
    private String orderUid;   // 주문 고유 번호 (merchant_uid)
}