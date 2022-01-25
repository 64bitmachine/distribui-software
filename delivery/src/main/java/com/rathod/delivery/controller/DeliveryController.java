package com.rathod.delivery.controller;

import com.rathod.delivery.dto.OrderPlaced;
import com.rathod.delivery.entity.DeliveryAgent;
import com.rathod.delivery.service.DeliveryService;
import com.rathod.delivery.service.ReadDB;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/")
public class DeliveryController {
    
    private final DeliveryService deliveryService;
    private final ReadDB readDB;

    @PostMapping("/requestOrder")
    public ResponseEntity<String> requestOrder() {
        // TODO: implement this method
        return new ResponseEntity<>("", null, null);
    }

    @PostMapping("/agentSignIn")
    public ResponseEntity<String> agentSignIn() {
        // TODO: implement this method
        return new ResponseEntity<>("", null, null);
    }

    @PostMapping("/agentSignOut")
    public ResponseEntity<String> agentSignOut() {
        // TODO: implement this method
        return new ResponseEntity<>("", null, null);
    }

    @PostMapping("/orderDelivered")
    public ResponseEntity<String> orderDelivered() {
        // TODO: implement this method
        return new ResponseEntity<>("", null, null);
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
