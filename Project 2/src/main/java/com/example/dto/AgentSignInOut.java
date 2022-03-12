package com.example.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AgentSignInOut {
    private int agentId;

    @JsonCreator
    public AgentSignInOut(@JsonProperty("agentId") int agentId) {
        this.agentId = agentId;
    }
}