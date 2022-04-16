package com.example;

import java.util.HashMap;
import java.util.TreeMap;

import com.example.Agent.AgentCommand;

import akka.actor.typed.ActorRef;

public class Shared {
     static TreeMap<Integer, ActorRef<AgentCommand>> agentMap;
     static ActorRef<Delivery.Command> deliveryRef;
     static HashMap<Integer, String> agentStatusMap; // Status : Status : ['unassigned' , 'assigned', 'sign-out']
}