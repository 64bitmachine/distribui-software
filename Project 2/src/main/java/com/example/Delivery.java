package com.example;

import java.util.Map;

import com.example.Agent.AgentCommand;
import com.example.dto.PlaceOrder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

/**
 * Delivery Actor
 */
public class Delivery extends AbstractBehavior<Delivery.Command> {

    private final static Logger log = LoggerFactory.getLogger(Delivery.class);
    private Map<Integer,ActorRef<AgentCommand>> agentMap;
    private long orderId;
    // actor protocol
    interface Command {
    }

    public Delivery(ActorContext<Command> context) {
        super(context);
        orderId  = 1000l;
        log.info("Delivery started");
        //TODO ReadData from the InitData.txt file
        //TODO Create as many agents as specified in above file
        //TODO Put them in agentMap
    }

    public final static class ReInitializeResponse {
        public ReInitializeResponse() {
            log.info("ReInitializeResponse");
        }
    }

    public final static class RequestOrderResponse {
        public RequestOrderResponse() {
            log.info("ReInitializeResponse");
        }
    }


    public final static class RequestOrder implements Command {
        public final ActorRef<RequestOrderResponse> replyTo;
        public final PlaceOrder placeOrder;
        public RequestOrder(ActorRef<RequestOrderResponse> replyTo,PlaceOrder placeOrder) {
            this.replyTo = replyTo;
            this.placeOrder = placeOrder;
        }
    }

    public final static class ReInitialize implements Command {
        public final ActorRef<ReInitializeResponse> replyTo;

        public ReInitialize(ActorRef<ReInitializeResponse> replyTo) {
            this.replyTo = replyTo;
        }
    }
    public final static class ActionPerformed implements Command {
        public final String description;

        public ActionPerformed(String description) {
            this.description = description;
        }
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(Delivery::new);
    }

    private Behavior<Command> onReInitialize(ReInitialize command) {
        // Optional<User> maybeUser = users.stream()
        // .filter(user -> user.name.equals(command.name))
        // .findFirst();
        log.info("onReInitialize");
        command.replyTo.tell(new ReInitializeResponse());
        return this;
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(ReInitialize.class, this::onReInitialize)
                .onMessage(RequestOrder.class, this::onRequestOrder)
                .build();
    }

    private Behavior<Command> onRequestOrder(RequestOrder reqOrder) {
        getContext().spawn(FulFillOrder.create(reqOrder.placeOrder,agentMap), "Order-"+orderId);
        orderId++;
        log.info("onReInitialize");
        reqOrder.replyTo.tell(new RequestOrderResponse());
        return this;
    }
}
