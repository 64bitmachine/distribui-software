package com.example.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderDelivered {
    private int orderId;

    @JsonCreator
    public OrderDelivered(@JsonProperty("orderId") int orderId) {
        this.orderId = orderId;
    }
}