package com.example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.Agent.AgentCommand;
import com.example.DBInit.ReadDB;
import com.example.dto.DeliveryAgent;
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
        log.info("Delivery Actor created");
        // TODO ReadData from the InitData.txt file
        ReadDB readfile = new ReadDB();
        List<DeliveryAgent> deliveryAgents = readfile.readDeliveryAgentIDFromFile();
        agentMap = new HashMap<>();
        orderMap = new HashMap<>();
        // TODO Create as many agents as specified in above file and Put entry in
        // agentMap
        for (DeliveryAgent agent : deliveryAgents) {
            ActorRef<Agent.AgentCommand> agentRef = context.spawn(
                    Agent.create(agent.getAgentId(), agent.getStatus()), "agent-" + agent.getAgentId());
            agentMap.put(agent.getAgentId(), agentRef);
        }
    }

    public final static class ReInitializeResponse {
        public ReInitializeResponse() {
            log.info("ReInitialize successful");
        }
    }

    public final static class AgentSignInOutCommand implements Command {
        public final int agentId;
        public final ActorRef<AgentSignInOutResponse> replyTo;
        public final boolean isSignIn;

        public AgentSignInOutCommand(ActorRef<AgentSignInOutResponse> replyTo, int agentId, boolean isSignIn) {
            this.agentId = agentId;
            this.replyTo = replyTo;
            this.isSignIn = isSignIn;
        }
    }

    public final static class AgentSignInOutResponse implements Command {
        public AgentSignInOutResponse() {
        }
    }

    public final static class GetAgentCmd implements Command {
        public final int agentId;
        public final ActorRef<Agent.GetAgentResponse> replyTo;

        public GetAgentCmd(ActorRef<Agent.GetAgentResponse> replyTo, String agentId) {
            this.agentId = Integer.parseInt(agentId);
            this.replyTo = replyTo;
        }
    }

    /**
     * this command is used for getting details of an order
     */
    public final static class GetOrderCmd implements Command {
        public final int orderId;
        public final ActorRef<FulFillOrder.GetOrderResponse> replyTo;

        public GetOrderCmd(ActorRef<FulFillOrder.GetOrderResponse> replyTo, String orderId) {
            this.orderId = Integer.parseInt(orderId);
            this.replyTo = replyTo;
        }
    }

    /**
     * this command is used to make order
     */
    public final static class RequestOrder implements Command {
        public final ActorRef<RequestOrderResponse> replyTo;
        public final PlaceOrder placeOrder;

        public RequestOrder(ActorRef<RequestOrderResponse> replyTo, PlaceOrder placeOrder) {
            this.replyTo = replyTo;
            this.placeOrder = placeOrder;
        }
    }

    public final static class RequestOrderResponse {
        public final Integer orderId;

        public RequestOrderResponse(Integer orderId) {
            this.orderId = orderId;
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
        orderId = 1000;
        orderMap.forEach((k, v) -> {
            getContext().stop(v);
        });
        orderMap.clear();
        agentMap.forEach((k, v) -> v.tell(new Agent.SignInOut(false)));
        command.replyTo.tell(new ReInitializeResponse());
        return this;
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(ReInitialize.class, this::onReInitialize)
                .onMessage(RequestOrder.class, this::onRequestOrder)
                .onMessage(AgentSignInOutCommand.class, this::onAgentSignInOut)
                .onMessage(GetAgentCmd.class, this::onGetAgent)
                .onMessage(GetOrderCmd.class, this::onGetOrder)
                .build();
    }

    private Behavior<Command> onAgentSignInOut(AgentSignInOutCommand agentSignInCmd) {
        log.info("Agent with Id {} has signed in", agentSignInCmd.agentId);
        agentMap.get(agentSignInCmd.agentId).tell(new Agent.SignInOut(agentSignInCmd.isSignIn));
        agentSignInCmd.replyTo.tell(new AgentSignInOutResponse());
        return Behaviors.same();
    }

    /**
     * This method handles the request order command
     * 
     * @param reqOrder
     * @return orderId
     */
    private Behavior<Command> onRequestOrder(RequestOrder reqOrder) {
        ActorRef<FulFillOrder.Command> orderActor = getContext().spawn(
                FulFillOrder.create(reqOrder.placeOrder, orderId, agentMap),
                "Order-" + orderId);
        orderMap.put(orderId, orderActor);
        reqOrder.replyTo.tell(new RequestOrderResponse(orderId));
        orderId++;
        return this;
    }

    private Behavior<Command> onGetAgent(GetAgentCmd getAgentCmd) {
        agentMap.get(getAgentCmd.agentId).tell(new Agent.GetAgentCmd(getAgentCmd.replyTo));
        return this;
    }

    private Behavior<Command> onGetOrder(GetOrderCmd getOrderCmd) {
        if (orderMap.containsKey(getOrderCmd.orderId)) {
            orderMap.get(getOrderCmd.orderId).tell(new FulFillOrder.GetOrderCmd(getOrderCmd.replyTo));
        } else {
            getContext().getLog().info("Order with Id {} not found", getOrderCmd.orderId);
            getOrderCmd.replyTo.tell(new FulFillOrder.GetOrderResponse(null));
        }
        return this;
    }
}
