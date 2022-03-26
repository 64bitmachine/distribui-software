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
    private boolean isAgentAssigned;

    public TrackOrder(ActorRef<FulFillOrder.Command> orderRef, boolean isAgentAssigned) {
        this.orderRef = orderRef;
        this.isAgentAssigned = isAgentAssigned;
    }

    public ActorRef<FulFillOrder.Command> getOrderRef() {
        return this.orderRef;
    }

    public void setOrderRef(ActorRef<FulFillOrder.Command> orderRef) {
        this.orderRef = orderRef;
    }

    public boolean getIsAgentAssigned() {
        return this.isAgentAssigned;
    }

    public void setIsAgentAssigned(boolean agentAssigned) {
        this.isAgentAssigned = agentAssigned;
    }

}
