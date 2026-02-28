package com.server.controller;

import com.server.domain.Order;
import com.server.dto.OrderRequest;
import com.server.dto.OrderResponse;
import com.server.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request,
                                                     @AuthenticationPrincipal UserDetails userDetails) {
        // 1. 주문 생성 (DB에 주문 정보 저장, 아직 결제 전 상태)
        Order order = orderService.createOrder(userDetails.getUsername(), request.getItemId());
        return ResponseEntity.ok(new OrderResponse(order));
    }
}