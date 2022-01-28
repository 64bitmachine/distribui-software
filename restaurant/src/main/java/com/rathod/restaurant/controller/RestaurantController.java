package com.rathod.restaurant.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rathod.restaurant.dto.RefillAndAcceptItemDto;
import com.rathod.restaurant.entity.Item;
import com.rathod.restaurant.service.RestaurantService;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/")
public class RestaurantController {

	private final RestaurantService restaurantService;
	@PostMapping("/reInitialize")
	public ResponseEntity<String> reInitialize() throws Exception
	{
		restaurantService.reInitialize();
		
		return new ResponseEntity<>("Read Initializer file is successful", HttpStatus.CREATED);
	}
	
	@PostMapping("/refillItem")
	public ResponseEntity<String> refillItem(@RequestBody RefillAndAcceptItemDto refill)
	{
		restaurantService.refillItem(refill);
		return new ResponseEntity<String>("Refill Succesful", HttpStatus.CREATED);
	}
	
	@PostMapping("/acceptOrder")
	public ResponseEntity<Item> acceptOrder(@RequestBody RefillAndAcceptItemDto accept)
	{
		Item item = restaurantService.acceptOrder(accept);
		if(item != null)
			return new ResponseEntity<Item>(item,HttpStatus.CREATED);
		return new ResponseEntity<Item>(item,HttpStatus.GONE);
	}
}
