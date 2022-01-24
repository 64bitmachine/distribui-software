package com.rathod.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefillIAndAcceptItemDto {

	private Integer restId;
	private Integer itemId;
	private Integer qty;
	
}
