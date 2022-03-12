package com.example;

import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Receive;

public class Agent extends AbstractBehavior<Agent.AgentCommand>{


    interface AgentCommand{}


    public Agent(ActorContext<AgentCommand> context) {
        super(context);
        //TODO Auto-generated constructor stub
    }

    @Override
    public Receive<AgentCommand> createReceive() {
        // TODO Auto-generated method stub
        return null;
    }
    
}