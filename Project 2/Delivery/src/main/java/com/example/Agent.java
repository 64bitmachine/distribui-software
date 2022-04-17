package com.example;

import com.example.FulFillOrder.AgentIsAvailableCmd;
import com.example.dto.DeliveryAgent;
import com.example.dto.DeliveryAgentStatus;
import com.example.Delivery.AgentSignInOutResponse;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;

import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class Agent extends AbstractBehavior<Agent.AgentCommand> {

    private final Integer agentId;
    private Integer orderId;

    private ActorRef<Delivery.Command> deliveryRef;

    // Parent Interface Command
    public interface AgentCommand {
    }

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
     * GetAgentCmd : Delivery Actor sends this command to Agent Actor to return
     * GetAgentResponse to Controller
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
        ActorRef<AgentSignInOutResponse> replyTo;
        SignInOut(ActorRef<AgentSignInOutResponse> replyTo,boolean isSignIn) {
            this.isSignIn = isSignIn;
            this.replyTo = replyTo;
        }
    }
    public static final class Reset implements AgentCommand {}
    /**
     * SignInOutResponse : Agent Actor sends this command to Delivery Actor
     */
    // public static final class SignInOutResponse {}

    /**
     * AvailableRequest : FulFillOrder send this to Agent Actor to check if the
     * agent is available
     * and if agent is avialble , sends FulFillOrder.AgentIsAvailableCmd to
     * fulfillOrder actor
     */
    public static final class AvailableRequest implements AgentCommand {
        final Integer orderId;
        final ActorRef<FulFillOrder.Command> replyTo;

        AvailableRequest(Integer orderId, ActorRef<FulFillOrder.Command> replyTo) {
            this.orderId = orderId;
            this.replyTo = replyTo;
        }
    }

    /**
     * Free : FulFillOrder send Free to Agent to make it free
     */
    public static final class Free implements AgentCommand {
        public Integer orderId;

        public Free(Integer orderId) {
            this.orderId = orderId;
        }
    }
    // public static final class AvailableResponse implements AgentCommand {}

    /**
     * ConfirmationRequest : FulFillOrder send this to Agent Actor to confirm with
     * the agent that
     * he is assigned to that particular FulFillOrder.
     * 
     * @args : isConfirmed : true if agent is confirmed to fulfill the order
     */
    public static final class ConfirmationRequest implements AgentCommand {
        final boolean isConfirmed;
        final Integer orderId;
        final ActorRef<FulFillOrder.Command> replyTo;

        ConfirmationRequest(Integer orderId, boolean isConfirmed, ActorRef<FulFillOrder.Command> replyTo) {
            this.orderId = orderId;
            this.replyTo = replyTo;
            this.isConfirmed = isConfirmed;
        }
    }

    public Agent(ActorContext<AgentCommand> context, ActorRef<Delivery.Command> deliveryRef, Integer agentId,
            String agentStatus) {
        super(context);
        // getContext().getLog().info("Created Delivery Agent with Id - {}", agentId);
        this.deliveryRef = deliveryRef;
        this.agentId = agentId;
        Shared.agentStatusMap.put(agentId,agentStatus);
        orderId = null;
    }

    @Override
    public Receive<AgentCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(AvailableRequest.class, this::onAvailableRequest)
                .onMessage(ConfirmationRequest.class, this::onConfirmationRequest)
                .onMessage(SignInOut.class, this::onSignInOut)
                .onMessage(GetAgentCmd.class, this::onGetAgent)
                .onMessage(Free.class, this::onFree)
                .onMessage(Reset.class, this::onReset)
                .build();
    }

    private Behavior<AgentCommand> onReset(Reset reset)
    {
        Shared.agentStatusMap.put(agentId,DeliveryAgentStatus.signed_out);
        orderId = null;
        return Behaviors.same();
    }
    private Behavior<AgentCommand> onFree(Free free) {

        // getContext().getLog().info("Agent with agent id {} having orderId {} is free from orderId {} ",agent.getAgentId(),orderId, free.orderId);

        if (orderId != null && orderId.equals(free.orderId)) {
            Shared.agentStatusMap.put(agentId,DeliveryAgentStatus.available);
            orderId = null;
        }
        /** Inform Delivery Agent here */
        deliveryRef.tell(new Delivery.AgentIsFree(agentId, getContext().getSelf()));
        return Behaviors.same();
    }

    private Behavior<AgentCommand> onSignInOut(SignInOut signIn) {
        if (signIn.isSignIn) {
            Shared.agentStatusMap.put(agentId,DeliveryAgentStatus.available);
            // getContext().getLog().info("Agent with Id {} has signed in", agent.getAgentId());
        } else {
            if (Shared.agentStatusMap.get(agentId).equals(DeliveryAgentStatus.available)) {
                // agent.setStatus(DeliveryAgentStatus.signed_out);
                Shared.agentStatusMap.put(agentId,DeliveryAgentStatus.signed_out);
                // getContext().getLog().info("Agent with Id {} has signed out", agent.getAgentId());
            }
        }
        signIn.replyTo.tell(new AgentSignInOutResponse());
        return Behaviors.same();
    }

    private Behavior<AgentCommand> onAvailableRequest(AvailableRequest request) {

        if (Shared.agentStatusMap.get(agentId).equals(DeliveryAgentStatus.available)) {
            // getContext().getLog().info("Sending AgentAvailableStatus from {} to FulFillOrder for order Id {}",
                   //agent.getAgentId(), orderId);
            orderId = request.orderId;
           //agent.setStatus(DeliveryAgentStatus.unavailable);
            Shared.agentStatusMap.put(agentId,DeliveryAgentStatus.unavailable);
            request.replyTo.tell(new AgentIsAvailableCmd(agentId,true));
        } else {
            // agent is unavailable
            // getContext().getLog().info("Agent with Id {} is not available", agent.getAgentId());
            request.replyTo.tell(new AgentIsAvailableCmd( agentId,false));
        }
        return Behaviors.same();
    }

    private Behavior<AgentCommand> onConfirmationRequest(ConfirmationRequest request) {
        /**
         * If agent is not assigned to desired orderId then set the agent back
         * available.
         */

        if (!request.isConfirmed && orderId.equals(request.orderId)) {
            // getContext().getLog().info(
            //        "Agent : Making Agent with agentId {} avialable as order Id {} is already assigned",
            //        agent.getAgentId(), orderId);
            //agent.setStatus(DeliveryAgentStatus.available);
            Shared.agentStatusMap.put(agentId,DeliveryAgentStatus.available);
            orderId = null;
        }
        return Behaviors.same();
    }

    private Behavior<AgentCommand> onGetAgent(GetAgentCmd getAgentCmd) {
        // getContext().getLog().info("Sending Agent with Id - {}", agent.getAgentId());
        getAgentCmd.replyTo.tell(new GetAgentResponse(new DeliveryAgent(agentId,Shared.agentStatusMap.get(agentId))));
        return Behaviors.same();
    }

    public static Behavior<AgentCommand> create(ActorRef<Delivery.Command> deliveryRef, Integer agentId,
            String agentStatus) {
        return Behaviors.setup(context -> new Agent(context, deliveryRef, agentId, agentStatus));
    }
}