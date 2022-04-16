package com.example;

import akka.actor.typed.ActorRef;

public class TrackOrder {
    /**
     * Track Order status and reference of the FulFillOrder Actor
     * 
     * @arg orderRef - ActorRef of FulFillOrder Actor
     * @arg agentAssigned - Boolean variable which will be true is agent is assigned
     *      else false.
     */
    private ActorRef<FulFillOrder.Command> orderRef;
    private String status;

    public TrackOrder(ActorRef<FulFillOrder.Command> orderRef, String status) {
        this.orderRef = orderRef;
        this.status = status;
    }

    public ActorRef<FulFillOrder.Command> getOrderRef() {
        return this.orderRef;
    }

    public void setOrderRef(ActorRef<FulFillOrder.Command> orderRef) {
        this.orderRef = orderRef;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
   
}

