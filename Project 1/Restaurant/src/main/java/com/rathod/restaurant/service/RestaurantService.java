package com.rathod.restaurant.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.rathod.restaurant.dto.RefillAndAcceptItemDto;
import com.rathod.restaurant.entity.Item;
import com.rathod.restaurant.entity.Restaurant;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class RestaurantService {
	
	private final RecordInitializer recordInitializer;
	private List<Restaurant> restaurants;
	
	public void reInitialize() throws Exception {
		restaurants.clear();
		restaurants = recordInitializer.readInitializerFile();
	}

	public boolean refillItem(RefillAndAcceptItemDto refill) {
		
		Boolean itemExistFlag = false;
		for(Restaurant restaurant: restaurants)
		{
			if(restaurant.getRestaurantId().equals(refill.getRestId()))
			{
				for(Item item: restaurant.getItems())
				{
					if(item.getItemId().equals(refill.getItemId()))
					{
						log.info("Item before refill :" + item.getItemId()+" ||  "+item.getQuantity());
						item.setQuantity(item.getQuantity() + refill.getQty());
						log.info("Item after refill :" + item.getItemId()+" ||  "+item.getQuantity());
						itemExistFlag = true;
					}
				}
			}
		}
		
		return itemExistFlag;
	}

	public Item acceptOrder(RefillAndAcceptItemDto accept) {
	    Item acceptedItem = null;
		for(Restaurant restaurant: restaurants)
		{
			if(restaurant.getRestaurantId().equals(accept.getRestId()))
			{
				for(Item item: restaurant.getItems())
				{
					if(item.getItemId().equals(accept.getItemId()) && item.getQuantity() >= accept.getQty())
					{
						log.info("restaurant id :" + restaurant.getRestaurantId());
						log.info("Item before accept :" + item.getItemId()+" ||  "+item.getQuantity());
						item.setQuantity(item.getQuantity() - accept.getQty());
						acceptedItem = new Item(item.getItemId(), item.getPrice(), accept.getQty());
						log.info("Item after accept :" + item.getItemId()+" ||  "+item.getQuantity());
					}
				}
			}
		}
		
		return acceptedItem;
	}
	
	
}
