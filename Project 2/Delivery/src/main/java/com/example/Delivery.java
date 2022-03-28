package com.example;

import java.util.List;
import java.util.TreeMap;

import com.example.Agent.AgentCommand;
import com.example.DBInit.ReadDB;
import com.example.dto.DeliveryAgent;
import com.example.dto.OrderDelivered;
import com.example.dto.PlaceOrder;

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

    private TreeMap<Integer, ActorRef<AgentCommand>> agentMap;
    private TreeMap<Integer, TrackOrder> orderMap;
    private Integer orderId;

    // Parent Command Interface
    public interface Command {
    }

    public Delivery(ActorContext<Command> context) {
        super(context);
        orderId = 1000;
        // getContext().getLog().info("Delivery Actor created");

        // reading the database
        ReadDB readfile = new ReadDB();
        List<DeliveryAgent> deliveryAgents = readfile.readDeliveryAgentIDFromFile();
        agentMap = new TreeMap<>();
        orderMap = new TreeMap<>();

        // Delivery Agent actors should also be spawned before the first http request is
        // received
        for (DeliveryAgent agent : deliveryAgents) {
            ActorRef<Agent.AgentCommand> agentRef = context.spawn(
                    Agent.create(getContext().getSelf(), agent.getAgentId(), agent.getStatus()),
                    "agent-" + agent.getAgentId());
            agentMap.put(agent.getAgentId(), agentRef);
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
     * AgentSignInOutCommand : External Controller send this comand to sign in and
     * sign out an agent.
     * 
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
     * GetAgentCmd : Controller sends this command to Delivery Actor to get the
     * appropriate agent
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
     * GetOrderCmd : Controller sends this command to Delivery Actor to get details
     * of the appropriate order
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
     * RequestOrder : Controller sends this command to Delivery Actor to place an
     * order
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
     * RequestOrderResponse : Response to controller by Delivery Actor after
     * receiving RequestOrder
     */
    public final static class RequestOrderResponse {
        public final Integer orderId;

        public RequestOrderResponse(Integer orderId) {
            this.orderId = orderId;
        }
    }

    public final static class ReInitializeResponse {
    }

    /**
     * ReInitialize : Controller sends this command to Delivery Actor to
     * re-initialize the system
     */
    public final static class ReInitialize implements Command {
        public final ActorRef<ReInitializeResponse> replyTo;

        public ReInitialize(ActorRef<ReInitializeResponse> replyTo) {
            this.replyTo = replyTo;
        }
    }

    /**
     * OrderDeliveredCmd : Controller sends this command to Deliver Actor to inform
     * it about Delivered Order
     */
    public final static class OrderDeliveredCmd implements Command {
        public final OrderDelivered order;
        public final ActorRef<GetOrderDeliveredResp> replyTo;

        public OrderDeliveredCmd(ActorRef<GetOrderDeliveredResp> replyTo, OrderDelivered order) {
            this.replyTo = replyTo;
            this.order = order;
        }
    }

    /**
     * DestroyFulFillOrder : FulFillOrder Send this to Delivery to destroy it once
     * the order is delivered.
     */
    public final static class DestroyFulFillOrder implements Command {
        public final Integer orderId;
        public final ActorRef<FulFillOrder.Command> orderRef;

        public DestroyFulFillOrder(Integer orderId, ActorRef<FulFillOrder.Command> orderRef) {
            this.orderId = orderId;
            this.orderRef = orderRef;
        }
    }

    /**
     * AgentIsFree : Agent Actor send this to Delivery on getting freed from the
     * assigned order upon its delivery
     */
    public final static class AgentIsFree implements Command {
        public final Integer agentId;
        public final ActorRef<Agent.AgentCommand> agentRef;

        public AgentIsFree(Integer agentId, ActorRef<Agent.AgentCommand> agentRef) {
            this.agentId = agentId;
            this.agentRef = agentRef;
        }
    }

    /**
     * GetOrderDeliveredResp : Delivery Actor sends this command to Controller as a
     * reply to OrderDelivered Endpoint Request
     */

    public final static class GetOrderDeliveredResp {
        final Integer orderId;

        public GetOrderDeliveredResp(Integer orderId) {
            this.orderId = orderId;
        }
    }
    /**
     * ActionPerformed : Yet to be implemented
     */
    /*
     * public final static class ActionPerformed implements Command {
     * public final String description;
     * 
     * public ActionPerformed(String description) {
     * this.description = description;
     * }
     * }
     */

    /**
     * AgentSignInOutResponse : Response to controller by Delivery Actor after
     * receiving AgentSignInOutCommand
     */
    public final static class AgentSignInOutResponse {
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
        agentMap.forEach((k, v) -> v.tell(new Agent.Reset()));
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
                .onMessage(OrderDeliveredCmd.class, this::onOrderDeliveredCmd)
                .onMessage(AgentIsFree.class, this::onAgentIsFree)
                .onMessage(DestroyFulFillOrder.class, this::onDestroyFulFillOrder)
                .build();
    }

    private Behavior<Command> onAgentIsFree(AgentIsFree agentIsFree) {
        if (agentIsFree.agentRef.toString().equals(agentMap.get(agentIsFree.agentId).toString())) {
            TrackOrder waitingOrder = getWaitingOrder();
            if (waitingOrder != null) {
                // getContext().getLog().info("DeliveryAgent : Agent {} is Free", agentIsFree.agentId);
                waitingOrder.getOrderRef().tell(new FulFillOrder.AssignAgent(agentIsFree.agentId));
            }
        }
        return Behaviors.same();
    }

    private Behavior<Command> onDestroyFulFillOrder(DestroyFulFillOrder destroy) {
        return Behaviors.same();
    }

    private Behavior<Command> onOrderDeliveredCmd(OrderDeliveredCmd orderDeliveredCmd) {
        
        Integer deliveredOrderId = orderDeliveredCmd.order.getOrderId();

        // getContext().getLog().info("Delivery: Sending OrderIsDelivered to FulFillOrder for orderId {}",
         //       deliveredOrderId);
        
                // log.info("Delivery : Order is assigned {}",
        // orderMap.get(deliveredOrderId).getIsAgentAssigned());
        if (orderMap.get(deliveredOrderId).getIsAgentAssigned() && orderMap.containsKey(deliveredOrderId))
            orderMap.get(deliveredOrderId).getOrderRef().tell(new FulFillOrder.OrderIsDelivered());

        // respond to the client
        orderDeliveredCmd.replyTo.tell(new GetOrderDeliveredResp(orderDeliveredCmd.order.getOrderId()));
        return Behaviors.same();
    }

    private Behavior<Command> onAgentAssigned(AgentAssigned agentAssigned) {
        orderMap.get(agentAssigned.orderId).setIsAgentAssigned(true);
        return Behaviors.same();
    }

    private Behavior<Command> onAgentSignInOut(AgentSignInOutCommand agentSignInCmd) {

        agentMap.get(agentSignInCmd.agentId).tell(new Agent.SignInOut(agentSignInCmd.isSignIn));

        // providing the response to the client
        agentSignInCmd.replyTo.tell(new AgentSignInOutResponse());

        // checking if request is signin and tryign to notify waiting order about the agent signin
        if (agentSignInCmd.isSignIn) {
            TrackOrder waitingOrder = getWaitingOrder();

            if (waitingOrder != null) {
                // getContext().getLog().info(
                 //       "Notifying a waiting fulfillorder actor about DeliveryAgent : Agent {} is Available",
                 //       agentSignInCmd.agentId);
                waitingOrder.getOrderRef().tell(new FulFillOrder.AssignAgent(agentSignInCmd.agentId));
            }
        }

        return Behaviors.same();
    }

    private TrackOrder getWaitingOrder() {
        for (Integer orderId : orderMap.keySet()) {
            TrackOrder order = orderMap.get(orderId);
            // System.out.println("############" + orderId);
            if (order.getIsAgentAssigned() == false) {
                return order;
            }
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

        // generate a fresh order ID and returning the order ID to the client
        reqOrder.replyTo.tell(new RequestOrderResponse(orderId));

        // spawn a FulfillOrder actor
        ActorRef<FulFillOrder.Command> orderActor = getContext().spawn(
                FulFillOrder.create(reqOrder.placeOrder, orderId, agentMap, getContext().getSelf()),
                "Order-" + orderId);
        orderMap.put(orderId, new TrackOrder(orderActor, false));
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
            // getContext().getLog().info("Order with Id {} not found", getOrderCmd.orderId);
            getOrderCmd.replyTo.tell(new FulFillOrder.GetOrderResponse(null));
        }
        return this;
    }
}
