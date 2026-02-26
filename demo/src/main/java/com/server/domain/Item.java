package com.server.domain;

import com.server.exception.CustomException;
import com.server.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Item extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    private String name;

    private int price;

    private int stockQuantity;

    public Item(String name, int price, int stockQuantity) {
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    // 비즈니스 로직: 재고 감소
    public void removeStock(int quantity) {
        int restStock = this.stockQuantity - quantity;
        if (restStock < 0) {
            throw new CustomException(ErrorCode.OUT_OF_STOCK); 
        }
        this.stockQuantity = restStock;
    }
    
    // 비즈니스 로직: 재고 증가 (주문 취소 시)
    public void addStock(int quantity) {
        this.stockQuantity += quantity;
    }
}
