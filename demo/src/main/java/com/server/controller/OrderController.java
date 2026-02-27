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
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/api/orders")
    public ResponseEntity<OrderResponse> createOrder(
            @AuthenticationPrincipal UserDetails principal, 
            @RequestBody OrderRequest request) {
        
        // 로그인한 사용자의 ID와 요청받은 상품 ID로 주문 생성
        Order order = orderService.createOrder(principal.getUsername(), request.getItemId());
        
        // 엔티티를 DTO로 변환하여 반환 (orderUid 필드 포함됨)
        return ResponseEntity.ok(new OrderResponse(order));
    }

}
