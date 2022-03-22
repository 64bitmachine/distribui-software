package com.example;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import com.example.Agent.AgentCommand;
import com.example.dto.DeliveryAgent;
import com.example.dto.Item;
import com.example.dto.OrderPlaced;
import com.example.dto.OrderStatus;
import com.example.dto.PlaceOrder;
import com.typesafe.config.ConfigFactory;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class FulFillOrder extends AbstractBehavior<FulFillOrder.Command> {
    private Map<Integer, ActorRef<AgentCommand>> agentMap;
    private PlaceOrder placeOrder;
    private Integer orderId;
    private String orderStatus;
    private Integer agentId;
    private Item item;
    private ActorRef<Agent.AgentCommand> agentRef;

    public static final String WALLET_SERVICE = ConfigFactory.load().getConfig("my-app.wallet-server")
            .getString("address");
    public static final String RESTAURANT_SERVICE = ConfigFactory.load().getConfig("my-app.restaurant-server")
            .getString("address");

    interface Command {
    }

    public final static class GetOrderResponse {
        final OrderPlaced order;

        public GetOrderResponse(OrderPlaced order) {
            this.order = order;
        }
    }

    public final static class GetOrderCmd implements Command {
        final ActorRef<GetOrderResponse> replyTo;

        public GetOrderCmd(ActorRef<GetOrderResponse> replyTo) {
            this.replyTo = replyTo;
        }
    }

    public FulFillOrder(ActorContext<Command> context, PlaceOrder placeOrder,
            Integer orderId, Map<Integer, ActorRef<AgentCommand>> agentMap) {

        super(context);
        getContext().getLog().info("Creating Order with order Id {}", orderId);

        this.placeOrder = placeOrder;
        this.agentMap = agentMap;
        this.orderId = orderId;
        this.orderStatus = OrderStatus.unassigned;
        this.agentId = -1;
        this.agentRef = null;
        this.item = null;

        // http post request to restaurant service to place order
        try {
            URL url = new URL(RESTAURANT_SERVICE + "/acceptOrder");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // post request
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            String input = "{\"restId\": " + placeOrder.getRestId() + ", \"itemId\": " + placeOrder.getItemId()
                    + ", \"qty\": " + placeOrder.getQty() + "}";

            // send request
            conn.getOutputStream().write(input.getBytes());
            conn.getOutputStream().flush();

            // get response code and response json object
            if (conn.getResponseCode() == 201) {
                InputStream inputStream = conn.getInputStream();
                StringBuffer response = new StringBuffer();
                int i;
                while ((i = inputStream.read()) != -1) {
                    response.append((char) i);
                }

                // parsing json object {"restId": num, "itemId": x, "qty": y}
                String[] tokens = response.toString().split(",");
                String[] restId = tokens[0].split(":");
                String[] itemId = tokens[1].split(":");
                String[] qty = tokens[2].split(":");

                // remove trailing } from qty
                String[] qtyTokens = qty[1].split("}");

                this.item = new Item(Integer.parseInt(restId[1].trim()), Integer.parseInt(itemId[1].trim()),
                        Integer.parseInt(qtyTokens[0].trim()));
                getContext().getLog().info("restaurant service response: {}", response.toString());
            } else {
                getContext().getLog().info("Order not placed");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // if item is not null then send request to wallet service to deduct amount
        if (this.item != null) {
            try {
                URL url = new URL(WALLET_SERVICE + "/deductBalance");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                // post request
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

                String input = "{\"custId\": " + placeOrder.getCustId() + ", \"amount\": "
                        + placeOrder.getQty() * item.getPrice() + "}";

                // send request
                conn.getOutputStream().write(input.getBytes());
                conn.getOutputStream().flush();

                // get response code and response json object
                if (conn.getResponseCode() == 201) {
                    getContext().getLog().info("Order placed");
                } else {
                    getContext().getLog().info("Order not placed");
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // @TODO Using hhtp client connect to wallet and restaurant
        // placeOrder();

        getContext().getLog().info("Order created with order Id {}, order status {}", orderId, orderStatus);
    }

    public static class ActorStatus implements Command {
        private ActorRef<Agent.AgentCommand> agentReplyTo;

        private DeliveryAgent agent;

        ActorStatus(ActorRef<Agent.AgentCommand> agentReplyTo, DeliveryAgent agent) {
            this.agentReplyTo = agentReplyTo;
            this.agent = agent;
        }
    }

    /**
     * public static class AgentAssignConfirm implements Command
     * {
     * private ActorRef<Agent.AgentCommand> agentReplyTo;
     * private DeliveryAgent agent;
     * private Boolean isAssigned;
     * AgentAssignConfirm(ActorRef<Agent.AgentCommand> agentReplyTo, DeliveryAgent
     * agent, Boolean isAssigned ){
     * this.agentReplyTo = agentReplyTo;
     * this.agent = agent;
     * this.isAssigned = isAssigned;
     * }
     * }
     */
    public static class ActorStatusResponse {
    }

    public static class OrderDelivered implements Command {
    }

    public static Behavior<Command> create(PlaceOrder placeOrder, Integer orderId,
            Map<Integer, ActorRef<AgentCommand>> agentMap) {
        return Behaviors.setup(context -> new FulFillOrder(context, placeOrder, orderId, agentMap));
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(ActorStatus.class, this::onActorStatus)
                // .onMessage(AgentAssignConfirm.class, this::onAgentAssignConfirm)
                .onMessage(OrderDelivered.class, this::onOrderDelivered)
                .onMessage(GetOrderCmd.class, this::onGetOrderCmd)
                .build();
    }

    private Behavior<Command> onActorStatus(ActorStatus actorStatus) {
        // TODO Implement onActorStatus

        /**
         * If we are recieving ActorStatus Message, it simply means that
         * actor is available while sending the ActorStatus Message to
         * FulFill Order
         */
        if (orderStatus.equals(OrderStatus.unassigned)) {
            getContext().getLog().info("FulFillOrder : Setting agentId to {} for order Id {}",
                    actorStatus.agent.getAgentId(), orderId);
            actorStatus.agentReplyTo.tell(new Agent.ConfirmationRequest(orderId, true, getContext().getSelf()));
            agentId = actorStatus.agent.getAgentId();
            agentRef = actorStatus.agentReplyTo;
        } else {
            actorStatus.agentReplyTo.tell(new Agent.ConfirmationRequest(orderId, false, getContext().getSelf()));
        }
        return Behaviors.same();
    }

    // private Behavior<Command> onAgentAssignConfirm(AgentAssignConfirm message){
    // // ? For now I am resetting the value if agent fail to assign.
    // //Need to discuss
    // if(message.agent.getAgentId() ==agentId && message.isAssigned == false)
    // {
    // agentId = null;
    // agentRef = null;
    // }
    // return this;
    // }

    private Behavior<Command> onOrderDelivered(OrderDelivered orderDelivered) {
        // TODO Implement OrderDelivered
        return null;
    }

    private boolean placeOrder() {
        probeAgents();
        return false;
    }

    private void probeAgents() {
        /** Will receive ActorStatus Message if agent is available */
        for (Integer id : agentMap.keySet()) {
            getContext().getLog().info("Sending AvailableRequest to {} from FulFillOrder for order Id {}", id, orderId);

            agentMap.get(id).tell(new Agent.AvailableRequest(orderId, getContext().getSelf()));
        }
    }

    private Behavior<Command> onGetOrderCmd(GetOrderCmd getOrderCmd) {
        getContext().getLog().info("FulFillOrder : Sending order details with id {} to {}", orderId, getOrderCmd.replyTo);
        getOrderCmd.replyTo.tell(new GetOrderResponse(new OrderPlaced(this.orderId, this.orderStatus, this.agentId)));
        return Behaviors.same();
    }
}
