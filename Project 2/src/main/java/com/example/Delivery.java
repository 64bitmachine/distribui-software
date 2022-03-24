package com.example;

import java.util.List;
import java.util.TreeMap;

import com.example.Agent.AgentCommand;
import com.example.DBInit.ReadDB;
import com.example.FulFillOrder.AgentIsAvailableCmd;
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
    private TreeMap<Integer, ActorRef<AgentCommand>> agentMap;
    private TreeMap<Integer, TrackOrder> orderMap;
    private Integer orderId;

    // Parent Command Interface
    interface Command {
    }

    public Delivery(ActorContext<Command> context) {
        super(context);
        orderId = 1000;
        log.info("Delivery Actor created");
       
        ReadDB readfile = new ReadDB();
        List<DeliveryAgent> deliveryAgents = readfile.readDeliveryAgentIDFromFile();
        agentMap = new TreeMap<>();
        orderMap = new TreeMap<>();
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

    /**
     * AgentAssigned : FullfillOrder Actor sends this command to Delivery Actor
     * when it has been assigned an agent 
    */
    public final static class AgentAssigned implements Command {
        public final Integer orderId;

        public AgentAssigned(Integer orderId) {
            this.orderId = orderId;
        }
    }

    /**
     * AgentSignInOutCommand : External Controller send this comand to sign in and sign out an agent.
     * @args isSignIn : true for sign in and false for sign out
     */
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

    /**
     * GetAgentCmd : Controller sends this command to Delivery Actor to get the appropriate agent
     */
    public final static class GetAgentCmd implements Command {
        public final int agentId;
        public final ActorRef<Agent.GetAgentResponse> replyTo;

        public GetAgentCmd(ActorRef<Agent.GetAgentResponse> replyTo, String agentId) {
            this.agentId = Integer.parseInt(agentId);
            this.replyTo = replyTo;
        }
    }

    /**
     * GetOrderCmd : Controller sends this command to Delivery Actor to get details of the appropriate order
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
     * RequestOrder : Controller sends this command to Delivery Actor to place an order
     */
    public final static class RequestOrder implements Command {
        public final ActorRef<RequestOrderResponse> replyTo;
        public final PlaceOrder placeOrder;

        public RequestOrder(ActorRef<RequestOrderResponse> replyTo, PlaceOrder placeOrder) {
            this.replyTo = replyTo;
            this.placeOrder = placeOrder;
        }
    }

    /**
     * RequestOrderResponse : Response to controller by Delivery Actor after receiving RequestOrder
     */
    public final static class RequestOrderResponse {
        public final Integer orderId;

        public RequestOrderResponse(Integer orderId) {
            this.orderId = orderId;
        }
    }

    /**
     * ReInitialize : Controller sends this command to Delivery Actor to re-initialize the system
     */
    public final static class ReInitialize implements Command {
        public final ActorRef<ReInitializeResponse> replyTo;

        public ReInitialize(ActorRef<ReInitializeResponse> replyTo) {
            this.replyTo = replyTo;
        }
    }

    /** 
     * ActionPerformed : Yet to be implemented
     */
   /*  public final static class ActionPerformed implements Command {
        public final String description;

        public ActionPerformed(String description) {
            this.description = description;
        }
    } */

    /**
     * AgentSignInOutResponse : Response to controller by Delivery Actor after receiving AgentSignInOutCommand
     */
    public final static class AgentSignInOutResponse{
        public AgentSignInOutResponse() {
        }
    }

    
    public static Behavior<Command> create() {
        return Behaviors.setup(Delivery::new);
    }

    private Behavior<Command> onReInitialize(ReInitialize command) {
        orderId = 1000;
        orderMap.forEach((orderId, trackOrder) -> {
            getContext().stop(trackOrder.getOrderRef());
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
                .onMessage(AgentAssigned.class, this::onAgentAssigned)
                .build();
    }

    private Behavior<Command> onAgentAssigned(AgentAssigned agentAssigned) {
        orderMap.get(agentAssigned.orderId).setIsAgentAssigned(true);
        return Behaviors.same();
    }

    private Behavior<Command> onAgentSignInOut(AgentSignInOutCommand agentSignInCmd) {
        log.info("Agent with Id {} has signed in", agentSignInCmd.agentId);
        agentMap.get(agentSignInCmd.agentId).tell(new Agent.SignInOut(agentSignInCmd.isSignIn));
        agentSignInCmd.replyTo.tell(new AgentSignInOutResponse());
        TrackOrder waitingOrder = getWaitingOrder();
        // if (waitingOrder != null) {
        //     waitingOrder.getOrderRef().tell(new AgentIsAvailableCmd());
        // }
        return Behaviors.same();
    }

    private TrackOrder getWaitingOrder() {
        for (Integer orderId : orderMap.keySet()) {
            TrackOrder order = orderMap.get(orderId);
            if (order.getIsAgentAssigned() == false)
                return order;
        }
        return null;
    }

    /**
     * This method handles the request order command
     * 
     * @param reqOrder
     * @return orderId
     */
    private Behavior<Command> onRequestOrder(RequestOrder reqOrder) {
        ActorRef<FulFillOrder.Command> orderActor = getContext().spawn(
                FulFillOrder.create(reqOrder.placeOrder, orderId, agentMap , getContext().getSelf()),
                "Order-" + orderId);
        orderMap.put(orderId, new TrackOrder(orderActor, false));
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
            ActorRef<FulFillOrder.Command> orderRef = orderMap.get(getOrderCmd.orderId).getOrderRef();
            orderRef.tell(new FulFillOrder.GetOrderCmd(getOrderCmd.replyTo));
        } else {
            getContext().getLog().info("Order with Id {} not found", getOrderCmd.orderId);
            getOrderCmd.replyTo.tell(new FulFillOrder.GetOrderResponse(null));
        }
        return this;
    }
}
