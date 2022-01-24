package com.rathod.wallet;

import java.io.Serializable;

/**
 * @author rathod
 * custumer id, amount
 */
public class Customer implements Serializable {

    private static final long serialVersionUID = 1L;
    
    // custumer id
    private int custId;

    // amount
    private double amount;

    // constructor
    public Customer(int id, double amount) {
        this.custId = id;
        this.amount = amount;
    }
    
    /**
     * @return the id
     */
    public int getId() {
        return custId;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.custId = id;
    }

    /**
     * @return the amount
     */
    public double getAmount() {
        return amount;
    }

    /**
     * @param amount the amount to set
     */
    public void setAmount(double amount) {
        this.amount = amount;
    }
}
