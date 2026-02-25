package com.server.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "orders") // SQL 예약어 'order'와 충돌 방지
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long price;

    @Column(nullable = false)
    private String itemName; // 상품명 (예: "피자 외 1건")

    @Column(nullable = false, unique = true)
    private String orderUid; // 주문 고유 번호 (PG사 연동용 merchant_uid)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Builder
    public Order(Long price, String itemName, Member member) {
        this.price = price;
        this.itemName = itemName;
        this.member = member;
        this.orderUid = UUID.randomUUID().toString();
        this.status = OrderStatus.READY;
    }

    // 결제 성공 시 주문 상태 변경 (Dirty Checking)
    public void changeStatus(OrderStatus status) {
        this.status = status;
    }
}
