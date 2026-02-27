package com.server.dto;

import com.server.domain.Order;
import lombok.Getter;

@Getter
public class OrderResponse {
    private String orderUid;

    public OrderResponse(Order order) {
        this.orderUid = order.getOrderUid();
    }
}
