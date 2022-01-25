package com.rathod.delivery.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    private int orderId;
    private int custId;
    private int restId;
    private int itemId;
    private int qty;
    private OrderStatus status;
    private int agentId;
}
