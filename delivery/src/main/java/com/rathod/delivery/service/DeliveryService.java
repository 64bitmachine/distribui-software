package com.rathod.delivery.service;

import java.util.List;

import com.rathod.delivery.dto.OrderPlaced;
import com.rathod.delivery.entity.DeliveryAgent;
import com.rathod.delivery.entity.DeliveryAgentStatus;
import com.rathod.delivery.entity.Order;
import com.rathod.delivery.entity.OrderStatus;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class DeliveryService {
    
    private final ReadDB readDB;
    private List<DeliveryAgent> deliveryAgents;
    private List<Order> orders;

    public DeliveryService() {
        readDB = new ReadDB();
        deliveryAgents = readDB.readDeliveryAgentIDFromFile();
    }

    /**
     * deletes all the orders from the order list
     * and marks all the agents as signed out
     * @param null
     */
    public void reinitialize() {
        // delete all orders
        orders.clear();

        // all agents - signed out
        for (DeliveryAgent agent : deliveryAgents) {
            agent.setStatus(DeliveryAgentStatus.SIGNED_OUT);
        }
    }

    /**
     * get agent by number
     * @param agentNum
     */
    public DeliveryAgent getAgent(int num) {
        for (DeliveryAgent agent : deliveryAgents) {
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
        for (Order order : orders) {
            if (order.getOrderId() == num) {
                if (order.getStatus() == OrderStatus.UNASSIGNED) {
                    return new OrderPlaced(num, order.getStatus(), -1);
                }
                return new OrderPlaced(num, order.getStatus(), order.getAgentId());
            }
        }
        return null;
    }
}
