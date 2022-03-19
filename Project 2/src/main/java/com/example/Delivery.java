package com.example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.Agent.AgentCommand;
import com.example.DBInit.ReadDB;
import com.example.dto.DeliveryAgent;
import com.example.dto.OrderStatus;
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
    private Map<Integer, ActorRef<AgentCommand>> agentMap;
    private Map<Integer, ActorRef<FulFillOrder.Command>> orderMap;
    private Integer orderId;

    // actor protocol
    interface Command {
    }

    public Delivery(ActorContext<Command> context) {
        super(context);
        orderId = 1000;
        log.info("Delivery started");
        // TODO ReadData from the InitData.txt file
        ReadDB readfile = new ReadDB();
        List<DeliveryAgent> deliveryAgents = readfile.readDeliveryAgentIDFromFile();
        agentMap = new HashMap<>();
        orderMap = new HashMap<>();
        // TODO Create as many agents as specified in above file and Put entry in agentMap
        for (DeliveryAgent agent : deliveryAgents) {
            log.info("Sending Agent for creation with agentId {}, agentStatus {}",agent.getAgentId(), agent.getStatus());
            ActorRef<Agent.AgentCommand> agentRef = context.spawn(
                    Agent.create(agent.getAgentId(), agent.getStatus()), "agent-" + agent.getAgentId());
            agentMap.put(agent.getAgentId(), agentRef);
        }
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

    public final static class AgentSignIn implements Command {
        public final Integer agentId;
        public final ActorRef<AgentSignIn> replyTo;
        public AgentSignIn(ActorRef<AgentSignIn> replyTo,Integer agentId) {
            this.agentId = agentId;
            this.replyTo = replyTo;
        }
    }

    public final static class RequestOrder implements Command {
        public final ActorRef<RequestOrderResponse> replyTo;
        public final PlaceOrder placeOrder;

        public RequestOrder(ActorRef<RequestOrderResponse> replyTo, PlaceOrder placeOrder) {
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
                .onMessage(AgentSignIn.class, this::onAgentSignIn)
                .build();
    }

    private Behavior<Command> onAgentSignIn(AgentSignIn agentSignIn)
    {
        log.info("Agent with Id {} has signed in",agentSignIn.agentId);
        agentMap.get(agentSignIn.agentId).tell(new Agent.SignIn());
        return Behaviors.same();
    }
    private Behavior<Command> onRequestOrder(RequestOrder reqOrder) {
        /**For each Order Request we are spawning new actor and storing it
        * order map.
        */
        ActorRef<FulFillOrder.Command> orderActor = getContext().spawn(
                FulFillOrder.create(reqOrder.placeOrder, orderId, agentMap),
                "Order-" + orderId);
        orderMap.put(orderId,orderActor);

        orderId++;
        log.info("onReInitialize");
        reqOrder.replyTo.tell(new RequestOrderResponse());
        return this;
    }
}