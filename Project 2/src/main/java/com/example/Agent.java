package com.example;

import com.example.FulFillOrder.ActorStatus;
import com.example.dto.DeliveryAgent;
import com.example.dto.DeliveryAgentStatus;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;


public class Agent extends AbstractBehavior<Agent.AgentCommand> {

    private final DeliveryAgent agent;
    private Integer orderId;

    public interface AgentCommand {}

    public final static class GetAgentResponse {
        final DeliveryAgent agent;

        public GetAgentResponse(DeliveryAgent agent) {
            this.agent = agent;
        }
    }

    public final static class GetAgentCmd implements AgentCommand {
        final ActorRef<GetAgentResponse> replyTo;

        public GetAgentCmd(ActorRef<GetAgentResponse> replyTo) {
            this.replyTo = replyTo;
        }
    }
    
    /**
     * this command is used to sign in and out the agent
     * isSignIn is true if agent is signing in
     * isSignIn is false if agent is signing out
     */
    public static final class SignInOut implements AgentCommand {
        final boolean isSignIn;
        
        SignInOut(boolean isSignIn) {
            this.isSignIn = isSignIn;
        }
    }

    public static final class SignInOutResponse implements AgentCommand {}

    public static final class AvailableRequest implements AgentCommand {
        final Integer orderId;
        final ActorRef<FulFillOrder.Command> replyTo;

        AvailableRequest(Integer orderId, ActorRef<FulFillOrder.Command> replyTo) {
            this.orderId = orderId;
            this.replyTo = replyTo;
        }
    }

    public static final class AvailableResponse implements AgentCommand {}

    public static final class ConfirmationRequest implements AgentCommand {
        final boolean isConfirmed;
        final Integer orderId;
        final ActorRef<FulFillOrder.Command> replyTo;

        ConfirmationRequest(Integer orderId, boolean isConfirmed ,ActorRef<FulFillOrder.Command> replyTo) {
            this.orderId = orderId;
            this.replyTo = replyTo;
            this.isConfirmed = isConfirmed;
        }
    }

    public Agent(ActorContext<AgentCommand> context, Integer agentId, String agentStatus) {
        super(context);
        getContext().getLog().info("Created Delivery Agent with Id - {}",agentId);
        agent = new DeliveryAgent(agentId,agentStatus);
    }

    @Override
    public Receive<AgentCommand> createReceive() {
        return newReceiveBuilder()
        .onMessage(AvailableRequest.class,this::onAvailableRequest)
        .onMessage(ConfirmationRequest.class,this::onConfirmationRequest)
        .onMessage(SignInOut.class, this::onSignInOut)
        .onMessage(GetAgentCmd.class, this::onGetAgent)
        .build();
    }
    
    private Behavior<AgentCommand> onSignInOut(SignInOut signIn) {
        if(signIn.isSignIn) {
            agent.setStatus(DeliveryAgentStatus.available);
            getContext().getLog().info("Agent with Id {} has signed in",agent.getAgentId());
        } else {
            agent.setStatus(DeliveryAgentStatus.signed_out);
            getContext().getLog().info("Agent with Id {} has signed out",agent.getAgentId());
        }
        return Behaviors.same();
    }
    
    private Behavior<AgentCommand> onAvailableRequest(AvailableRequest request) {
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

    private Behavior<AgentCommand> onConfirmationRequest(ConfirmationRequest request) {
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

    private Behavior<AgentCommand> onGetAgent(GetAgentCmd getAgentCmd) {
        getContext().getLog().info("Sending Agent with Id - {}",agent.getAgentId());
        getAgentCmd.replyTo.tell(new GetAgentResponse(agent));
        return Behaviors.same();
    }

    public static Behavior<AgentCommand> create(Integer agentId, String agentStatus) {
        return Behaviors.setup(context -> new Agent(context, agentId, agentStatus));
    }
}