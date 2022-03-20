package com.example;

import java.util.Map;

import com.example.Agent.AgentCommand;
import com.example.dto.DeliveryAgent;
import com.example.dto.OrderStatus;
import com.example.dto.PlaceOrder;
import com.typesafe.config.Config;
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
    private String  orderStatus;
    private Integer agentId;
    private ActorRef<Agent.AgentCommand> agentRef;

    public static final String WALLET_SERVICE = ConfigFactory.load().getConfig("my-app.wallet-server").getString("address");
    public static final String RESTAURANT_SERVICE = ConfigFactory.load().getConfig("my-app.restaurant-server").getString("address");

    public FulFillOrder(ActorContext<Command> context, PlaceOrder placeOrder,
            Integer orderId,
            Map<Integer, ActorRef<AgentCommand>> agentMap) {
        
        super(context);
        getContext().getLog().info("Creating Order with order Id {}", orderId);

        System.out.println("Wallet Service: " + WALLET_SERVICE);
        System.out.println("Restaurant Service: " + RESTAURANT_SERVICE);

        this.placeOrder = placeOrder;
        this.agentMap = agentMap;
        this.orderId = orderId;
        this.orderStatus = OrderStatus.unassigned; 
        this.agentId = null;
        this.agentRef = null;
        //@TODO Using hhtp client connect to wallet and restaurant
        // placeOrder();

        

        getContext().getLog().info("Order created with order Id {}, order status {}", orderId, orderStatus);
    }

    interface Command {}

    public static class ActorStatus implements Command {
        private ActorRef<Agent.AgentCommand> agentReplyTo;

        private DeliveryAgent agent;
        ActorStatus(ActorRef<Agent.AgentCommand> agentReplyTo, DeliveryAgent agent){
            this.agentReplyTo = agentReplyTo;
            this.agent = agent;
        }
    }
/**    public static class AgentAssignConfirm implements Command
    {
        private ActorRef<Agent.AgentCommand> agentReplyTo;
        private DeliveryAgent agent;
        private Boolean isAssigned;
        AgentAssignConfirm(ActorRef<Agent.AgentCommand> agentReplyTo, DeliveryAgent agent, Boolean isAssigned ){
            this.agentReplyTo = agentReplyTo;
            this.agent = agent;
            this.isAssigned = isAssigned;
        }
    }
*/
    public static class ActorStatusResponse{
        
    }

    public static class OrderDelivered implements Command {

    }

    public static Behavior<Command> create(PlaceOrder placeOrder, Integer orderId, Map<Integer, ActorRef<AgentCommand>> agentMap) {
        return Behaviors.setup(context -> new FulFillOrder(context, placeOrder, orderId, agentMap));
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
        .onMessage(ActorStatus.class, this::onActorStatus)
        //.onMessage(AgentAssignConfirm.class, this::onAgentAssignConfirm)
        .onMessage(OrderDelivered.class, this::onOrderDelivered)
        .build();
    }

    private Behavior<Command> onActorStatus(ActorStatus actorStatus)
    {
        //TODO Implement onActorStatus
        
        /** If we are recieving ActorStatus Message, it simply means that
         * actor is available while sending the ActorStatus Message to 
         * FulFill Order
         */
        if(orderStatus.equals(OrderStatus.unassigned))
        {
            getContext().getLog().info("FulFillOrder : Setting agentId to {} for order Id {}",actorStatus.agent.getAgentId(), orderId);
            actorStatus.agentReplyTo.tell(new Agent.ConfirmationRequest(orderId,true,getContext().getSelf()));
            agentId = actorStatus.agent.getAgentId();
            agentRef = actorStatus.agentReplyTo;
        }
        else{
            actorStatus.agentReplyTo.tell(new Agent.ConfirmationRequest(orderId,false,getContext().getSelf()));
        }
        return Behaviors.same();
    }

    // private Behavior<Command> onAgentAssignConfirm(AgentAssignConfirm message){
    //     // ? For now I am resetting the value if agent fail to assign. 
    //     //Need to discuss
    //     if(message.agent.getAgentId() ==agentId && message.isAssigned == false)
    //     {
    //         agentId = null;
    //         agentRef = null;
    //     }
    //     return this;
    // }


    private Behavior<Command> onOrderDelivered(OrderDelivered orderDelivered)
    {
        //TODO Implement OrderDelivered
        return null;
    }

    private boolean placeOrder() {
        probeAgents();
        return false;
    }

    private void probeAgents(){
        /** Will receive ActorStatus Message if agent is available */
        for(Integer id : agentMap.keySet())
        {
            getContext().getLog().info("Sending AvailableRequest to {} from FulFillOrder for order Id {}",id, orderId);

            agentMap.get(id).tell(new Agent.AvailableRequest(orderId,getContext().getSelf()));
        }
    }
}
