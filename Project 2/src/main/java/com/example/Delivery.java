package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final static Logger log = LoggerFactory.getLogger(Delivery.class);

    // actor protocol
    interface Command {
    }

    public Delivery(ActorContext<Command> context) {
        super(context);
        log.info("Delivery started");
    }

    public final static class ReInitializeResponse {
        public ReInitializeResponse() {
            log.info("ReInitializeResponse");
        }
    }

    public final static class ReInitialize implements Command {
        public final ActorRef<ReInitializeResponse> replyTo;

        public ReInitialize(ActorRef<ReInitializeResponse> replyTo) {
            this.replyTo = replyTo;
        }
    }

    public final static class ActionPerformed implements Command {
        public final String description;

        public ActionPerformed(String description) {
            this.description = description;
        }
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(Delivery::new);
    }

    private Behavior<Command> onReInitialize(ReInitialize command) {
        // Optional<User> maybeUser = users.stream()
        // .filter(user -> user.name.equals(command.name))
        // .findFirst();
        log.info("onReInitialize");
        command.replyTo.tell(new ReInitializeResponse());
        return this;
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(ReInitialize.class, this::onReInitialize)
                .build();
    }
}
