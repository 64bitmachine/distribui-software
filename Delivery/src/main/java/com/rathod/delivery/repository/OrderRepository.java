package com.rathod.delivery.repository;

import com.rathod.delivery.entity.Order;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    
}
