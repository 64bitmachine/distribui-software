package com.rathod.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefillAndAcceptItemDto {

	private Integer restId;
	private Integer itemId;
	private Integer qty;
	
}
