package com.server.service;

import com.server.domain.Item;
import com.server.domain.Member;
import com.server.domain.Order;
import com.server.domain.OrderStatus;
import com.server.exception.CustomException;
import com.server.exception.ErrorCode;
import com.server.repository.ItemRepository;
import com.server.repository.MemberRepository;
import com.server.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;

    /**
     * 주문 생성
     * 1. 회원 조회
     * 2. 상품 조회
     * 3. 재고 감소
     * 4. 주문 저장
     */
    public Order createOrder(String memberId, Long itemId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(ErrorCode.ITEM_NOT_FOUND));

        // 재고 감소 (수량 1개 고정 가정, 필요 시 파라미터로 받아서 처리)
        item.removeStock(1);

        // Order 생성 (생성자 내부에서 가격, 이름 설정 및 UUID 생성됨)
        Order order = Order.builder()
                .member(member)
                .item(item)
                .build();

        return orderRepository.save(order);
    }

    /**
     * 주문 취소 (재고 복구 포함)
     */
    public void cancelOrder(String orderUid) {
        Order order = orderRepository.findByOrderUid(orderUid)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new CustomException(ErrorCode.ALREADY_CANCELLED);
        }

        // 재고 복구
        order.getItem().addStock(1);
        
        // 상태 변경
        order.changeStatus(OrderStatus.CANCELLED);
    }
}