package com.rathod.delivery.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "FoodOrder")
public class Order implements Comparable<Order> {

    @Id
    @GeneratedValue
    private int id;
    
    private int orderId;
    private int custId;
    private int restId;
    private int itemId;
    private int qty;
    private OrderStatus status;
    private int agentId;

    @Override
    public int compareTo(Order o) {
        return this.orderId - o.orderId;
    }
}
