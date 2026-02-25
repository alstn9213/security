package com.server.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long amount;

    private String impUid; // PG사 결제 고유 번호 (결제 성공 후 갱신)

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Builder
    public Payment(Long amount, Order order) {
        this.amount = amount;
        this.order = order;
        this.status = PaymentStatus.READY;
    }

    public void paymentSuccess(String impUid) {
        this.impUid = impUid;
        this.status = PaymentStatus.OK;
    }

    public void paymentFail(String impUid) {
        this.impUid = impUid;
        this.status = PaymentStatus.FAILED;
    }
}
