package com.rathod.restaurant.entity;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Restaurant {

	private Integer restaurantId;
	private Integer numOfItems;
	private List<Item> items;
}
