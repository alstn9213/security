package com.server.dto;

import com.server.domain.Order;
import lombok.Getter;

@Getter
public class OrderResponse {
    private String orderUid;
    private Long price;
    private String buyerName;
    private String buyerEmail;

    public OrderResponse(Order order) {
        this.orderUid = order.getOrderUid();
        this.price = order.getPrice();
        this.buyerName = order.getMember().getName();
        this.buyerEmail = order.getMember().getId();
    }
}
