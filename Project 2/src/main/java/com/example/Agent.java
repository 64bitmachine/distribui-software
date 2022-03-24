package com.example;

import com.example.FulFillOrder.AgentIsAvailableCmd;
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

    //Parent Interface Command
    public interface AgentCommand {}

    /**
     * GetAgentResponse : Agent Actor sends this command to Controller
     */
    public final static class GetAgentResponse {
        final DeliveryAgent agent;

        public GetAgentResponse(DeliveryAgent agent) {
            this.agent = agent;
        }
    }
    /**
     * GetAgentCmd : Delivery Actor sends this command to Agent Actor to return GetAgentResponse to Controller
     */
    public final static class GetAgentCmd implements AgentCommand {
        final ActorRef<GetAgentResponse> replyTo;

        public GetAgentCmd(ActorRef<GetAgentResponse> replyTo) {
            this.replyTo = replyTo;
        }
    }
    
    /**
     * SignInOut : DeliveryActor sends this command to Agent Actor
     * is used to sign in and out the agent
     * isSignIn is true if agent is signing in
     * isSignIn is false if agent is signing out
     */
    public static final class SignInOut implements AgentCommand {
        final boolean isSignIn;
        
        SignInOut(boolean isSignIn) {
            this.isSignIn = isSignIn;
        }
    }

    /**
     * SignInOutResponse : Agent Actor sends this command to Delivery Actor
     */
    //public static final class SignInOutResponse {}

    /**
     * AvailableRequest : FulFillOrder send this to Agent Actor to check if the agent is available 
     * and if agent is avialble , sends FulFillOrder.AgentIsAvailableCmd to fulfillOrder actor
     */
    public static final class AvailableRequest implements AgentCommand {
        final Integer orderId;
        final ActorRef<FulFillOrder.Command> replyTo;

        AvailableRequest(Integer orderId, ActorRef<FulFillOrder.Command> replyTo) {
            this.orderId = orderId;
            this.replyTo = replyTo;
        }
    }

    //public static final class AvailableResponse implements AgentCommand {}

    /**
     * ConfirmationRequest : FulFillOrder send this to Agent Actor to confirm with the agent that 
     * he is assigned to that particular FulFillOrder.
     * @args : isConfirmed : true if agent is confirmed to fulfill the order
     */
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
        
        if(agent.getStatus().equals(DeliveryAgentStatus.available)) {
            getContext().getLog().info("Sending AgentAvailableStatus from {} to FulFillOrder for order Id {}", agent.getAgentId(), orderId);
            orderId = request.orderId;
            agent.setStatus(DeliveryAgentStatus.unavailable);
            request.replyTo.tell(new AgentIsAvailableCmd(getContext().getSelf(), agent));
        }
        else {
            // agent is unavailable
            getContext().getLog().info("Agent with Id {} is not available",agent.getAgentId());
            request.replyTo.tell(new AgentIsAvailableCmd(getContext().getSelf(), null));
        }
        return Behaviors.same();
    }

    private Behavior<AgentCommand> onConfirmationRequest(ConfirmationRequest request) {
        /**If agent is not assigned to desired orderId then set the agent back
         * available.
        */
        System.out.println("reQUEST"+request);
        System.out.println(request.orderId);
        System.out.println(request.isConfirmed);
        if(!request.isConfirmed && orderId.equals(request.orderId)) {
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