package com.example;

import java.util.Map;

import com.example.Agent.AgentCommand;
import com.example.dto.PlaceOrder;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class FulFillOrder extends AbstractBehavior<FulFillOrder.Command> {
    private Map<Integer, ActorRef<AgentCommand>> agentMap;
    private PlaceOrder placeOrder;

    public FulFillOrder(ActorContext<Command> context, PlaceOrder placeOrder,
            Map<Integer, ActorRef<AgentCommand>> agentMap) {
        super(context);
        this.placeOrder = placeOrder;
        this.agentMap = agentMap;

        //@TODO Using hhtp client connect to wallet and restaurant
        placeOrder();
    }

    interface Command {
    }

    public static class ActorStatus implements Command {

    }

    public static class ActorStatusResponse {

    }

    public static class OrderDelivered implements Command {

    }

    public static Behavior<Command> create(PlaceOrder placeOrder, Map<Integer, ActorRef<AgentCommand>> agentMap) {
        return Behaviors.setup(context -> new FulFillOrder(context, placeOrder, agentMap));
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
        .onMessage(ActorStatus.class, this::onActorStatus)
        .onMessage(OrderDelivered.class, this::onOrderDelivered)
        .build();
    }

    private Behavior<Command> onActorStatus(ActorStatus actorStatus)
    {
        //TODO Implement onActorStatus
        return null;
    }

    private Behavior<Command> onOrderDelivered(OrderDelivered orderDelivered)
    {
        //TODO Implement OrderDelivered
        return null;
    }

    private boolean placeOrder()
    {
        return false;
    }
}
