package com.rathod.restaurant.service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sound.sampled.Line;

import org.springframework.stereotype.Service;

import com.rathod.restaurant.CONSTANTS;
import com.rathod.restaurant.entity.Item;
import com.rathod.restaurant.entity.Restaurant;
import com.rathod.restaurant.exception.RestaurantException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class RecordInitializer {

	private final String FILE_PATH;
	private final List<Restaurant> restaurants; 
	public RecordInitializer()
	{
		FILE_PATH = CONSTANTS.INIT_RECORD_FILE_PATH;
		restaurants = new ArrayList<>();
	}
	
	public List<Restaurant> readInitializerFile() throws Exception
	{
		try {
			BufferedReader bReader = new BufferedReader(new FileReader(FILE_PATH));
			String line = null;
			while((line = bReader.readLine())!=null) {
				log.info("Restaurant : "+ line);
				String[] split = line.split("\\s+");
				if(split.length == 2)
				{
					Integer restaurantId  = Integer.parseInt(split[0]);
					Integer numberOFItems = Integer.parseInt(split[1]);
					
					Restaurant restaurant = new Restaurant();
					List<Item> list = new ArrayList<>();
					for(int i = 0;i<numberOFItems ;i++)
					{
						String []item_desc = bReader.readLine().split("\\s+");
						Item item = new Item();
						item.setItemId(Integer.parseInt(item_desc[0]));
						item.setPrice(Integer.parseInt(item_desc[1]));
						item.setQuantity(Integer.parseInt(item_desc[2]));
						list.add(item);
						log.info("Items :"+Arrays.toString(item_desc));
					}
					restaurant.setItems(list);
					restaurant.setRestaurantId(restaurantId);
					restaurant.setNumOfItems(numberOFItems);
					
					restaurants.add(restaurant);
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			throw new RestaurantException("There is some problem in reading the Initializer File");
		}
		return restaurants;
	}
	
	
}
