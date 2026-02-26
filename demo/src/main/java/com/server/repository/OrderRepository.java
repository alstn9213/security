package com.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.server.domain.Order;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderUid(String orderUid);
}