package com.example;

import com.example.FulFillOrder.ActorStatus;
/* import com.example.FulFillOrder.ActorStatusResponse;
import com.example.FulFillOrder.AgentAssignConfirm; */
import com.example.dto.DeliveryAgent;
import com.example.dto.DeliveryAgentStatus;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import scala.concurrent.impl.FutureConvertersImpl.P;


public class Agent extends AbstractBehavior<Agent.AgentCommand> {

    // private Integer agentId;
    // private String agentStatus;
    private DeliveryAgent agent;
    private Integer orderId;
    interface AgentCommand {
    }
    static class SignIn implements AgentCommand{
        
    }
    static class AvailableRequest implements AgentCommand {
        Integer orderId;
        ActorRef<FulFillOrder.Command> replyTo;
        AvailableRequest(Integer orderId,ActorRef<FulFillOrder.Command> replyTo)
        {
            this.orderId = orderId;
            this.replyTo = replyTo;
        }
    }
    static class ConfirmationRequest implements AgentCommand{
        boolean isConfirmed;
        Integer orderId = null;
        ActorRef<FulFillOrder.Command> replyTo;
        ConfirmationRequest(Integer orderId, boolean isConfirmed ,ActorRef<FulFillOrder.Command> replyTo)
        {
            this.orderId = orderId;
            this.replyTo = replyTo;
            this.isConfirmed = isConfirmed;
        }
    }
    static class AvailableResponse { 

    }

    public Agent(ActorContext<AgentCommand> context, Integer agentId, String agentStatus) {
        super(context);
        getContext().getLog().info("Starting Actor Agent {}",agentId);
        agent = new DeliveryAgent(agentId,agentStatus);
    }

    @Override
    public Receive<AgentCommand> createReceive() {
        return newReceiveBuilder()
        .onMessage(AvailableRequest.class,this::onAvailableRequest)
        .onMessage(ConfirmationRequest.class,this::onConfirmationRequest)
        .onMessage(SignIn.class, this::onSignIn)
        .build();
    }
    
    private Behavior<AgentCommand> onSignIn(SignIn signIn)
    {
        //TODO : NEED TO IMPLEMENT Agent Sign Notification Going back to Delivery Actor which will allot agent to
        //Pending Order based on OrderId
        agent.setStatus(DeliveryAgentStatus.available);
        return Behaviors.same();
    }
    private Behavior<AgentCommand> onAvailableRequest(AvailableRequest request)
    {
        //TODO: Do Something on receiving Available Request

        /** If Agent is available,send the message. */
        if(agent.getStatus().equals(DeliveryAgentStatus.available))
        {
            getContext().getLog().info("Sending ActorStatus from {} to FulFillOrder for order Id {}",agent.getAgentId(), orderId);
            orderId = request.orderId;
            agent.setStatus(DeliveryAgentStatus.unavailable);
            request.replyTo.tell(new ActorStatus(getContext().getSelf(),
            agent));
        }
            
        
        /** If Agent is unavailable or offline, do not send
         *  any message
         */
        return Behaviors.same();
    }

    private Behavior<AgentCommand> onConfirmationRequest(ConfirmationRequest request)
    {
        //TODO: Do Something on receiving Confirmation Request
        /**If agent is not assigned to desired orderId then set the agent back
         * available.
        */
        // if(!agent.getStatus().equals(DeliveryAgentStatus.available))
        // {
        //     // request.replyTo.tell(new AgentAssignConfirm(getContext().getSelf(),agent,
        //     // false));
        // }

        if(!request.isConfirmed && orderId.equals(request.orderId))
        {
            getContext().getLog().info("Agent : Making Agent with agentId {} avialable as order Id {} is already assigned",agent.getAgentId(), orderId);
            agent.setStatus(DeliveryAgentStatus.available);
            orderId = null;
        }
        return Behaviors.same();
    }
    public static Behavior<AgentCommand> create(Integer agentId, String agentStatus) {
        return Behaviors.setup(context -> new Agent(context, agentId, agentStatus));
    }

}