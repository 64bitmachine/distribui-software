package com.rathod.delivery.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "DeliveryAgent")
public class DeliveryAgent {

    @Id
    @GeneratedValue
    private int id;

    private int agentId;
    private String status;
}