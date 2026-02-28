package com.server.dto;

import com.server.domain.Item;
import lombok.Getter;

@Getter
public class ItemResponse {
    private Long id;
    private String name;
    private int price;

    public ItemResponse(Item item) {
        this.id = item.getId();
        this.name = item.getName();
        this.price = item.getPrice();
    }
}