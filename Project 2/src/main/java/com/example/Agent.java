package com.example;

import com.example.FulFillOrder.ActorStatus;
import com.example.FulFillOrder.ActorStatusResponse;
import com.example.FulFillOrder.AgentAssignConfirm;
import com.example.dto.DeliveryAgent;
import com.example.dto.DeliveryAgentStatus;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import ch.qos.logback.core.Context;

public class Agent extends AbstractBehavior<Agent.AgentCommand> {

    // private Integer agentId;
    // private String agentStatus;
    private DeliveryAgent agent;
    private Integer orderId;
    interface AgentCommand {
    }

    static class AvailableRequest implements AgentCommand {
        ActorRef<FulFillOrder.Command> replyTo;
        AvailableRequest(ActorRef<FulFillOrder.Command> replyTo)
        {
            this.replyTo = replyTo;
        }
    }
    static class AssignRequest implements AgentCommand{
        Integer orderId = null;
        ActorRef<FulFillOrder.Command> replyTo;
        AssignRequest(Integer orderId,ActorRef<FulFillOrder.Command> replyTo)
        {
            this.orderId = orderId;
            this.replyTo = replyTo;
        }
    }
    static class AvailableResponse { 
    }

    public Agent(ActorContext<AgentCommand> context, Integer agentId, String agentStatus) {
        super(context);
        agent = new DeliveryAgent(agentId,agentStatus);
    }

    @Override
    public Receive<AgentCommand> createReceive() {
        return newReceiveBuilder()
        .onMessage(AvailableRequest.class,this::onAvailableRequest)
        .onMessage(AssignRequest.class,this::onAssignRequest)
        .build();
    }
    private Behavior<AgentCommand> onAvailableRequest(AvailableRequest request)
    {
        //TODO: Do Something on receiving Available Request

        /** If Agent is available,send the message. */
        if(agent.getStatus().equals(DeliveryAgentStatus.available))
            request.replyTo.tell(new ActorStatus(getContext().getSelf(),
            agent));
        
        /** If Agent is unavailable or offline, do not send
         *  any message
         */
        return Behaviors.same();
    }

    private Behavior<AgentCommand> onAssignRequest(AssignRequest request)
    {
        //TODO: Do Something on receiving Assign Request
        /**IF agent is unavailble then assign orderId to agent and make
        * the agent unavailable and send response to OrderId to get the
        * order assigned.
        */
        if(!agent.getStatus().equals(DeliveryAgentStatus.available))
        {
            request.replyTo.tell(new AgentAssignConfirm(getContext().getSelf(),agent,
            false));

        }
        return Behaviors.same();
    }
    public static Behavior<AgentCommand> create(Integer agentId, String agentStatus) {
        return Behaviors.setup(context -> new Agent(context, agentId, agentStatus));
    }

}