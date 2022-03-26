package com.example.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PlaceOrder {
    private int custId;
    private int restId;
    private int itemId;
    private int qty;

    @JsonCreator
    public PlaceOrder(@JsonProperty("custId") int custId, @JsonProperty("restId") int restId,
            @JsonProperty("itemId") int itemId, @JsonProperty("qty") int qty) {
        this.custId = custId;
        this.restId = restId;
        this.itemId = itemId;
        this.qty = qty;
    }
}