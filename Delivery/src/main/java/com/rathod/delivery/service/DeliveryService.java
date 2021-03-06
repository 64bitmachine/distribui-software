package com.rathod.delivery.service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.PostConstruct;

import com.rathod.delivery.dto.Customer;
import com.rathod.delivery.dto.Item;
import com.rathod.delivery.dto.OrderInvoice;
import com.rathod.delivery.dto.OrderPlaced;
import com.rathod.delivery.dto.PlaceOrder;
import com.rathod.delivery.dto.RefillAndAcceptItemDto;
import com.rathod.delivery.entity.DeliveryAgent;
import com.rathod.delivery.entity.DeliveryAgentStatus;
import com.rathod.delivery.entity.Order;
import com.rathod.delivery.entity.OrderStatus;
import com.rathod.delivery.repository.DeliveryAgentRepository;
import com.rathod.delivery.repository.OrderRepository;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
public class DeliveryService {

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private DeliveryAgentRepository deliveryAgentRepository;
    
    private  ReadDB readDB;
    private  WebClient.Builder webClientBuilder;
    
    // private List<DeliveryAgent> deliveryAgents;
    
    private WebClient restaurantWebClient;
    private WebClient walletWebClient;
    
    @Value("${url.restaurant.microservice}")
    private String restaurant_url;
    
    @Value("${url.wallet.microservice}")
    private String wallet_url;
    
    private static int orderId;
    
    @Autowired
    public DeliveryService(ReadDB readDB, WebClient.Builder webClientBuilder) {
    	this.readDB = readDB;
    	this.webClientBuilder = webClientBuilder;
    }
    
    public DeliveryService() {}

    @PostConstruct
    public void init() {
    	restaurantWebClient = webClientBuilder.baseUrl(restaurant_url).
    			defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).build();
    	walletWebClient = webClientBuilder.baseUrl(wallet_url)
    			.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).build();
    	orderId = 1000;
    	
		// adding delivery agents to the database
		readDB.readDeliveryAgentIDFromFile().forEach(deliveryAgent -> {
			deliveryAgentRepository.save(deliveryAgent);
		});
    }
    

    /**
     * deletes all the orders from the order list
     * and marks all the agents as signed out
     * @param null
     */
    public void reinitialize() {
        // delete all orders
		orderRepository.deleteAll();
        orderId = 1000;	
        // all agents - signed out
        for (DeliveryAgent agent : deliveryAgentRepository.findAll()) {
            agent.setStatus(DeliveryAgentStatus.signed_out);
			deliveryAgentRepository.save(agent);
        }
    }

    /**
     * get agent by number
     * @param agentNum
     */
    public DeliveryAgent getAgent(int num) {
        for (DeliveryAgent agent : deliveryAgentRepository.findAll()) {
            if (agent.getAgentId() == num) {
                return agent;
            }
        }
        return null;
    }

    /**
     * get the order from the order list based on the order number
     * @param num
     * @return
     */
    public OrderPlaced getOrder(int num) {
        for (Order order : orderRepository.findAll()) {
            if (order.getOrderId() == num) {
                if (order.getStatus() == OrderStatus.unassigned) {
                    return new OrderPlaced(num, order.getStatus(), -1);
                }
                return new OrderPlaced(num, order.getStatus(), order.getAgentId());
            }
        }
        return null;
    }

    /**
     * sign in the agent and assign the order to the agent
     * @param agentId
     */
    public void agentSignIn(int agentId) {
        for (DeliveryAgent agent : deliveryAgentRepository.findAll()) {
            if (agent.getAgentId() == agentId) {

                // if agent is signed out then sign in
                if (agent.getStatus().equals(DeliveryAgentStatus.signed_out)) {
                    for (Order order : orderRepository.findAll()) {
                
                        // if order is unassigned then assign it to the agent
                        if (order.getStatus() == OrderStatus.unassigned) {
                            agent.setStatus(DeliveryAgentStatus.unavailable);
                            order.setStatus(OrderStatus.assigned);
                            order.setAgentId(agentId);
							orderRepository.save(order);
                            return;
                        }
                    }
                    agent.setStatus(DeliveryAgentStatus.available);
					deliveryAgentRepository.save(agent);
                }
                break;
            }
        }
    }

    /**
     * sign out the agent
     * @param agentId
     */
    public void agentSignOut(int agentId) {
        for (DeliveryAgent agent : deliveryAgentRepository.findAll()) {
            if (agent.getAgentId() == agentId) {
                if (agent.getStatus().equals(DeliveryAgentStatus.available)) {
                    agent.setStatus(DeliveryAgentStatus.signed_out);
					deliveryAgentRepository.save(agent);
                }
                break;
            }
        }
    }

    public void orderDelivered(int orderId) {
    	/**
    	 * @Todo {"orderId":num} assigned state -> delivered state only unavailable ->available
    	 * @Todo assign this delivery agent to any pending order.
    	 */
    	Order order = searchForOrder(orderId);
    	if(order == null || order.getStatus() != OrderStatus.assigned) return;
    	
    	order.setStatus(OrderStatus.delivered);

		// update the order in the database
		orderRepository.save(order);

    	int deliveryAgentId = order.getAgentId();
    	DeliveryAgent deliveryAgent = findDeliveryAgentById(deliveryAgentId);
    	deliveryAgent.setStatus(DeliveryAgentStatus.available);
		deliveryAgentRepository.save(deliveryAgent);
    	
		// this part is critical section so, it is synchronized
		synchronized (this) {
			Order order2 = findLowestOrderIdThatIsUnassigned();
			if(order2 != null)
			{
				deliveryAgent.setStatus(DeliveryAgentStatus.unavailable);
				order2.setAgentId(deliveryAgentId);
				order2.setStatus(OrderStatus.assigned);
				orderRepository.save(order2);
			}
		}
    }

    private DeliveryAgent findDeliveryAgentById(int deliveryAgentId) {
    	for(DeliveryAgent agent : deliveryAgentRepository.findAll())
    	{
    		if(agent.getAgentId() == deliveryAgentId)
    			return agent;
    	}
		return null;
	}

	private Order findLowestOrderIdThatIsUnassigned() {
    	for(Order order : orderRepository.findAll()) {
    		if(order.getStatus() == OrderStatus.unassigned)
    			return order;
    	}
		return null;
	}

	private Order searchForOrder(int orderId) {
    	for(Order order : orderRepository.findAll()) {
    		if(order.getOrderId() == orderId)
    		{
    			return order;
    		}
    	}
		return null;
	}

	public OrderInvoice placeOrder(PlaceOrder placeOrder) {
    	/**
    	 * @Todo Fetch price of the item given restaurant Id and Item Id
    	 * @Todo Billing Amount
    	 * @Todo deduct from wallet 
    	 * @Todo generate the orderID - unassigned 
    	 * @Todo Look for available delivery agent - if found - assigned state of order and unavailable
    	 *  state of delivery agent. 
    	 */
    	
    	RefillAndAcceptItemDto accept = 
    			new RefillAndAcceptItemDto(placeOrder.getRestId(),placeOrder.getItemId(),placeOrder.getQty());
    	
    	Item item = null;
    	try {
    		item = restaurantWebClient.post().uri("/acceptOrder")
    			.bodyValue(accept).retrieve()
    			.onStatus(HttpStatus.GONE::equals, response ->  response.bodyToMono(Item.class).map(null))
    			.bodyToMono(Item.class).block(); //block() to make async request sync.
    	}
    	catch (Exception e) {
    		log.error(e.getMessage());
		}
    	if(item == null)
    		return null;
    	
    	Integer billingAmt = placeOrder.getQty() * item.getPrice();
    	
    	Customer customer = new Customer(placeOrder.getCustId(),billingAmt);
    	String isSuccessful = null;
    	try {
    		isSuccessful =  walletWebClient.post().uri("/deductBalance").
    							bodyValue(customer).retrieve()
    						    .bodyToMono(String.class).block();
    	}
    	catch (Exception e) {
    		log.error(e.getMessage());
		}
    	
    	if(isSuccessful == null || isSuccessful.equals("Unsuccessful"))
    	{
    		try {
    		restaurantWebClient.post().uri("/refillItem")
        			.bodyValue(accept).retrieve()
        			.bodyToMono(String.class).block();
    		}
    		catch (Exception e) {

    			log.error(e.getMessage());
			}
    		return null;
    	}
    	
		Order order = null;

		// this part is critical section so, it is synchronized
		synchronized (this) {
			order = getOrderObject(placeOrder,orderId);
    		orderId += 1;
			DeliveryAgent agent = findLowestAvailableAgentId();
			if(agent != null)
			{
				order.setAgentId(agent.getAgentId());
				order.setStatus(OrderStatus.assigned);
				agent.setStatus(DeliveryAgentStatus.unavailable);
				deliveryAgentRepository.save(agent);
			}
		}

		orderRepository.save(order);
    	return new OrderInvoice(order.getOrderId());
    }
    
    private Order getOrderObject(PlaceOrder placeOrder,int orderId) {
    	Order order = new Order();
    	order.setOrderId(orderId);
    	order.setCustId(placeOrder.getCustId());
    	order.setItemId(placeOrder.getItemId());
    	order.setRestId(placeOrder.getRestId());
    	order.setQty(placeOrder.getQty());
    	order.setStatus(OrderStatus.unassigned);
		return order;
	}

	public DeliveryAgent findLowestAvailableAgentId() {
		List<DeliveryAgent> agents = deliveryAgentRepository.findAll();
    	Collections.sort(agents, Comparator.comparing(DeliveryAgent::getAgentId));
    	for(DeliveryAgent agent : agents) {
    		if(agent.getStatus().equals(DeliveryAgentStatus.available)){
    			return agent;
    		}
    	}
    	
    	return null;
    }
}
