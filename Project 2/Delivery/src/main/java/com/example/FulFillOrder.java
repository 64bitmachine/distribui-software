package com.example;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.example.Delivery.OrderReject;
import com.example.Delivery.RequestOrder;
import com.example.Delivery.RequestOrderResponse;
import com.example.dto.DeliveryAgentStatus;
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
    
    private RequestOrder reqOrder;
    private Integer orderId;
    private String orderStatus;
    private Integer agentId;
    private Item item;
    private List<Integer> agentReqList;
    private boolean servingAssignAgentReq = false;
    public static final String WALLET_SERVICE = ConfigFactory.load().getConfig("my-app.wallet-server")
            .getString("address");
    public static final String RESTAURANT_SERVICE = ConfigFactory.load().getConfig("my-app.restaurant-server")
            .getString("address");

    // parent Command Interface
    interface Command {
    }

    /**
     * GetOrderResponse : FullfillOrder Actor sends this command to Controller
     */
    public final static class GetOrderResponse {
        final OrderPlaced order;

        public GetOrderResponse(OrderPlaced order) {
            this.order = order;
        }
    }

    /**
     * GetOrderCmd : Delivery Actor sends this command to FullfillOrder Actor to
     * return the order to controller.
     */
    public final static class GetOrderCmd implements Command {
        final ActorRef<GetOrderResponse> replyTo;

        public GetOrderCmd(ActorRef<GetOrderResponse> replyTo) {
            this.replyTo = replyTo;
        }
    }

    /**
     * AgentIsAvailableCmd : Agent Actor sends this to Fulfillorder if it is
     * avialable.
     */
    public final static class AgentIsAvailableCmd implements Command {
        // private final ActorRef<Agent.AgentCommand> agentReplyTo;
        private final Integer agentId;
        private final boolean available;
        AgentIsAvailableCmd(Integer agentId,boolean available) {
            this.agentId = agentId;
            this.available = available;
        }
    }

    /**
     * AssignAgent : Delivery Actor sends this command to FullfillOrder Actor to
     * assign the agent to the order.
     */
    public final static class AssignAgent implements Command {
        // private final ActorRef<Agent.AgentCommand> agentReplyTo;
        private final Integer agentId;

        AssignAgent(Integer agentId) {
            // this.agentReplyTo = agentReplyTo;
            this.agentId = agentId;
        }
    }

    /**
     * ActorStatusResponse : To be implemented
     */
    public static class ActorStatusResponse {
    }

    /**
     * OrderIsDelivered : To be implemented
     */
    public static class OrderIsDelivered implements Command {
        // public final ActorRef<FulFillOrder.GetOrderDeliveredResp> replyTo;
        // OrderIsDelivered ( ActorRef<FulFillOrder.GetOrderDeliveredResp> replyTo )
        // {
        // this.replyTo = replyTo;
        // }
    }

    public FulFillOrder(ActorContext<Command> context, RequestOrder reqOrder, Integer orderId) {

        super(context);
        // getContext().getLog().info("Creating Order with order Id {}", orderId);

        this.reqOrder = reqOrder;
        this.orderId = orderId;

        //  firstly setting its order status to unassigned
        this.orderStatus = OrderStatus.unassigned;
        
        this.agentId = -1;
        this.item = null;
        this.agentReqList = new ArrayList<>();

        // http post request to restaurant service to place order
        try {
            URL url = new URL(RESTAURANT_SERVICE + "/acceptOrder");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // post request
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            String input = "{\"restId\": " + this.reqOrder.placeOrder.getRestId() + ", \"itemId\": "
                    + this.reqOrder.placeOrder.getItemId()
                    + ", \"qty\": " + this.reqOrder.placeOrder.getQty() + "}";

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
                // getContext().getLog().info("restaurant service response: {}", response.toString());
            } else {
                // getContext().getLog().info("Order not placed");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(this.item == null){
            orderStatus = OrderStatus.rejected;
            Shared.deliveryRef.tell(new OrderReject(orderId));
        }
        // if item is not null then send request to wallet service to deduct amount
        else {
            try {
                URL url = new URL(WALLET_SERVICE + "/deductBalance");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                // post request
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

                String input = "{\"custId\": " + this.reqOrder.placeOrder.getCustId() + ", \"amount\": "
                        + this.reqOrder.placeOrder.getQty() * item.getPrice() + "}";

                // send request
                conn.getOutputStream().write(input.getBytes());
                conn.getOutputStream().flush();

                // get response code and response json object
                if (conn.getResponseCode() == 201) {
                    // getContext().getLog().info("Order placed");

                    // contacting delivery agents
                    probeAgents();
                } else {

                    // getContext().getLog().info("Order not placed");
                    orderStatus = OrderStatus.rejected;
                    Shared.deliveryRef.tell(new OrderReject(orderId));
                    // revert the previous transaction on the restaurant service
                    try {

                        url = new URL(RESTAURANT_SERVICE + "/refillItem");
                        conn = (HttpURLConnection) url.openConnection();

                        // post request
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "application/json");
                        conn.setRequestProperty("Accept", "application/json");
                        conn.setDoOutput(true);

                        input = "{\"restId\": " + this.reqOrder.placeOrder.getRestId() + ", \"itemId\": "
                                + this.reqOrder.placeOrder.getItemId()
                                + ", \"qty\": " + this.reqOrder.placeOrder.getQty() + "}";

                        // send request
                        conn.getOutputStream().write(input.getBytes());
                        conn.getOutputStream().flush();

                        // get response code and response json object
                        if (conn.getResponseCode() == 201) {
                            // getContext().getLog().info("order items restored");
                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        reqOrder.replyTo.tell(new RequestOrderResponse(orderId));
        // getContext().getLog().info("Order created with order Id {}, order status {}", orderId, orderStatus);
    }

    
    // public static Behavior<Command> create(PlaceOrder placeOrder, Integer orderId) {
    //     return Behaviors.setup(context -> new FulFillOrder(context, placeOrder, orderId));
    // }
    public static Behavior<Command> create(RequestOrder reqOrder, Integer orderId) {
        return Behaviors.setup(context -> new FulFillOrder(context, reqOrder, orderId));
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(AgentIsAvailableCmd.class, this::onAgentIsAvailableCmd)
                .onMessage(OrderIsDelivered.class, this::onOrderIsDelivered)
                .onMessage(GetOrderCmd.class, this::onGetOrderCmd)
                .onMessage(AssignAgent.class, this::onAssignAgent)
                .onMessage(OrderIsDelivered.class, this::onOrderIsDelivered)
                .build();
    }

    private Behavior<Command> onOrderIsDelivered(OrderIsDelivered deliveredOrder) {

        // getContext().getLog().info("FulFillOrder : Freeing Agent with {} with agentRef{} on delivered order orderId {}",
        //        agentId,agentRef, orderId);
                
        orderStatus = OrderStatus.delivered;
        // notify the agent that they are free
        Shared.agentMap.get(agentId).tell(new Agent.Free(orderId));
        return Behaviors.same();
    }

    private Behavior<Command> onAssignAgent(AssignAgent assignAgent) {
        // getContext().getLog().info("FulFillOrder: Trying Assigning the agent {} for order {} on delivery'suggestion",
        //        assignAgent.agentId, orderId);
        servingAssignAgentReq = true;
        for (Integer agentid : Shared.agentMap.keySet()) {
            System.out.println("on assignAgent " + agentid + " status " + Shared.agentStatusMap.get(agentid));
        }
        // wait for 1 sec
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (orderStatus.equals(OrderStatus.unassigned)) {
            if(Shared.agentStatusMap.get(assignAgent.agentId).equals(DeliveryAgentStatus.available))
            {
                if (!this.agentReqList.contains(assignAgent.agentId)) {
                    // getContext().getLog().info("FulFillOrder : Sending ActorStatus to agent {} for order Id {}",
                    //        agentid, orderId);
                    Shared.agentMap.get(assignAgent.agentId).tell(new Agent.AvailableRequest(orderId, getContext().getSelf()));
                }
            }
        }
        else if(orderStatus.equals(OrderStatus.assigned)){
            Shared.deliveryRef.tell(new Delivery.AgentAssigned(orderId,null));
        }
        return Behaviors.same();
    }

    private Behavior<Command> onAgentIsAvailableCmd(AgentIsAvailableCmd res) {

        if (res.available) {
            if (orderStatus.equals(OrderStatus.unassigned)) {
                // getContext().getLog().info("FulFillOrder : Setting agentId to {} for order Id {}",
                //        agentAvailableStatus.agent.getAgentId(), orderId);
                // res.agentReplyTo.tell(new Agent.ConfirmationRequest(orderId, true, getContext().getSelf()));

                Shared.agentMap.get(res.agentId).tell(new Agent.ConfirmationRequest(orderId, true, getContext().getSelf()));      
                this.agentId = res.agentId;
                this.agentReqList.clear();
                this.orderStatus = OrderStatus.assigned;

                // Informing Delivery Actor that order has been assigned to an agent
                Shared.deliveryRef.tell(new Delivery.AgentAssigned(orderId,this.agentId));
            }
        }
        else if(!res.available && servingAssignAgentReq){
            Shared.deliveryRef.tell(new Delivery.AgentAssigned(orderId,null)); 
        } 
        else {
            // previously requested agent is not available, so request for another agent
            // agentAvailableStatus.agentReplyTo.tell(new Agent.ConfirmationRequest(orderId,
            // false, getContext().getSelf()));
            probeAgents();
        }
        return Behaviors.same();
    }

    private void probeAgents() {

        // getContext().getLog().info("FulFillOrder : Probing agents for order Id {}", orderId);

        for(Integer aId: Shared.agentStatusMap.keySet())
        {
            if(Shared.agentStatusMap.get(aId).equals(DeliveryAgentStatus.available))
            {
                if (!this.agentReqList.contains(aId)) {
                    // getContext().getLog().info("FulFillOrder : Sending ActorStatus to agent {} for order Id {}",
                    //        agentid, orderId);
                    Shared.agentMap.get(aId).tell(new Agent.AvailableRequest(orderId, getContext().getSelf()));
                    this.agentReqList.add(aId);
                    break;
                }
            }
        }


        // for (Integer agentid : Shared.agentMap.keySet()) {
        //     if (!this.agentReqList.contains(agentid)) {
        //         // getContext().getLog().info("FulFillOrder : Sending ActorStatus to agent {} for order Id {}",
        //         //        agentid, orderId);
        //         Shared.agentMap.get(agentid).tell(new Agent.AvailableRequest(orderId, getContext().getSelf()));
        //         this.agentReqList.add(agentid);
        //         break;
        //     }
        // }
    }

    private Behavior<Command> onGetOrderCmd(GetOrderCmd getOrderCmd) {
        // getContext().getLog().info("FulFillOrder : Sending order details with id {} to {}", orderId,
        //        getOrderCmd.replyTo);
        // System.out.println("FulFillOrder : Sending order details with id " + this.orderId + " status " + this.orderStatus
        //  + " agent " + this.agentId);
        
        // for (Integer agentid : Shared.agentMap.keySet()) {
        //     System.out.println("Agent " + agentid + " status " + Shared.agentStatusMap.get(agentid));
        // }

        getOrderCmd.replyTo.tell(new GetOrderResponse(new OrderPlaced(this.orderId, this.orderStatus, this.agentId)));
        return Behaviors.same();
    }
}
