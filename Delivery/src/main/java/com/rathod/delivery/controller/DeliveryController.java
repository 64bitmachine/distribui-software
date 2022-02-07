package com.rathod.delivery.controller;

import com.rathod.delivery.dto.AgentSignInOut;
import com.rathod.delivery.dto.OrderDelivered;
import com.rathod.delivery.dto.OrderInvoice;
import com.rathod.delivery.dto.OrderPlaced;
import com.rathod.delivery.dto.PlaceOrder;
import com.rathod.delivery.entity.DeliveryAgent;
import com.rathod.delivery.service.DeliveryService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/")
public class DeliveryController {
    
    private final DeliveryService deliveryService;

    @PostMapping("/requestOrder")
    public ResponseEntity<OrderInvoice> requestOrder(@RequestBody PlaceOrder placeOrder) {
        OrderInvoice orderInvoice = deliveryService.placeOrder(placeOrder);
        if (orderInvoice != null) {
            return new ResponseEntity<>(orderInvoice, HttpStatus.CREATED);
        }
        else
            return new ResponseEntity<>(orderInvoice ,HttpStatus.GONE);
    }

    @PostMapping("/agentSignIn")
    public ResponseEntity<String> agentSignIn(@RequestBody AgentSignInOut agentSignInOut) {
        deliveryService.agentSignIn(agentSignInOut.getAgentId());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/agentSignOut")
    public ResponseEntity<String> agentSignOut(@RequestBody AgentSignInOut agentSignInOut) {
        deliveryService.agentSignOut(agentSignInOut.getAgentId());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/orderDelivered")
    public ResponseEntity<String> orderDelivered(@RequestBody OrderDelivered orderDelivered) {
        deliveryService.orderDelivered(orderDelivered.getOrderId());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/order/{num}")
    public ResponseEntity<OrderPlaced> getOrder(@PathVariable("num") int num) {
        OrderPlaced order = deliveryService.getOrder(num);
        if (order == null) {
            return new ResponseEntity<OrderPlaced>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<OrderPlaced>(order, HttpStatus.OK);
    }

    @GetMapping("/agent/{num}")
    public ResponseEntity<DeliveryAgent> getAgent(@PathVariable("num") int num) {
        return new ResponseEntity<DeliveryAgent>(deliveryService.getAgent(num), HttpStatus.OK);
    }

    @PostMapping("reInitialize")
    public ResponseEntity<String> reInitialize() {
        deliveryService.reinitialize();
        return new ResponseEntity<String>("reinitialized !!!!", HttpStatus.CREATED);
    }
}
