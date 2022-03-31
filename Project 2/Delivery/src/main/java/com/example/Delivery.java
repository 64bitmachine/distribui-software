package com.example;

import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import com.example.DBInit.ReadDB;
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

    private TreeMap<Integer, TrackOrder> orderMap;
    private Integer orderId;

    // Parent Command Interface
    public interface Command {
    }

    public Delivery(ActorContext<Command> context,String path) {
        super(context);
        orderId = 1000;
        // getContext().getLog().info("Delivery Actor created");

        // reading the database
        ReadDB readfile = new ReadDB(path);
        orderMap = new TreeMap<>();
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
    public final static class MarkFulFillOrder implements Command {
        public final Integer orderId;
        public final ActorRef<FulFillOrder.Command> orderRef;

        public MarkFulFillOrder(Integer orderId, ActorRef<FulFillOrder.Command> orderRef) {
            this.orderId = orderId;
            this.orderRef = orderRef;
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



    public static Behavior<Command> create(String path) {
        return Behaviors.setup(context -> new Delivery(context,path));
    }

    private Behavior<Command> onReInitialize(ReInitialize command) {
        orderId = 1000;
        orderMap.forEach((orderId, trackOrder) -> {
            getContext().stop(trackOrder.getOrderRef());
        });
        orderMap.clear();
        command.replyTo.tell(new ReInitializeResponse());
        return this;
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(ReInitialize.class, this::onReInitialize)
                .onMessage(RequestOrder.class, this::onRequestOrder)
                .onMessage(GetOrderCmd.class, this::onGetOrder)
                .onMessage(OrderDeliveredCmd.class, this::onOrderDeliveredCmd)
                //.onMessage(MarkFulFillOrder.class, this::onMarkFulFillOrder)
                .build();
    }


    // private Behavior<Command> onMarkFulFillOrder(MarkFulFillOrder deliveredOrder) {
    //     if(orderMap.get(deliveredOrder.orderId).toString().equals(deliveredOrder.orderRef.toString())) {
    //         orderMap.get(deliveredOrder.orderId).setIsAgentAssigned(true);
    //     }
    //     return Behaviors.same();
    // }

    private Behavior<Command> onOrderDeliveredCmd(OrderDeliveredCmd orderDeliveredCmd) {
        return Behaviors.same();
    }

    /**
     * This method handles the request order command
     * 
     * @param reqOrder
     * @return orderId
     */
    private Behavior<Command> onRequestOrder(RequestOrder reqOrder) {

        // spawn a FulfillOrder actor
        // TimeUnit.SECONDS.sleep(1);
        ActorRef<FulFillOrder.Command> orderActor = getContext().spawn(
                FulFillOrder.create(reqOrder.placeOrder, orderId, getContext().getSelf()),
                "Order-" + orderId);
        orderMap.put(orderId, new TrackOrder(orderActor, false));

        // generate a fresh order ID and returning the order ID to the client
        reqOrder.replyTo.tell(new RequestOrderResponse(orderId));

        orderId++;

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
