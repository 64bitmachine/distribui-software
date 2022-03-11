package com.rathod.delivery.repository;

import com.rathod.delivery.entity.DeliveryAgent;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryAgentRepository extends JpaRepository<DeliveryAgent, Integer> {
    
}
