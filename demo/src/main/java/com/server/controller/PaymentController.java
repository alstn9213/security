package com.server.controller;

import com.server.dto.PaymentCallbackRequest;
import com.server.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/webhook")
    public ResponseEntity<String> validationPayment(@RequestBody PaymentCallbackRequest request) {
        paymentService.paymentByCallback(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cancel")
    public ResponseEntity<String> cancelPayment(@RequestBody PaymentCallbackRequest request) {
        paymentService.refund(request);
        return ResponseEntity.ok("결제가 취소되었습니다.");
    }
}