package com.rathod.delivery.dto;

import com.rathod.delivery.entity.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderPlaced {
    private int orderId;
    private OrderStatus status;
    private int agentId;
}
