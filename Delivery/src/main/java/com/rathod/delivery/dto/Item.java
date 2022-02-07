package com.rathod.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Item {
	private  Integer itemId;
	private  Integer price;
	private  Integer quantity;
}
