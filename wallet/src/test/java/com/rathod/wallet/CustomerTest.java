package com.rathod.wallet;

import org.junit.jupiter.api.Test;

public class CustomerTest {
    @Test
    void testGetAmount() {
        Customer customer = new Customer(1, 100.0);
        assert customer.getAmount() == 100.0;
    }

    @Test
    void testGetId() {
        Customer customer = new Customer(1, 100.0);
        assert customer.getId() == 1;
    }

    @Test
    void testSetAmount() {
        Customer customer = new Customer(1, 100.0);
        customer.setAmount(200.0);
        assert customer.getAmount() == 200.0;
    }

    @Test
    void testSetId() {
        Customer customer = new Customer(1, 100.0);
        customer.setId(2);
        assert customer.getId() == 2;
    }
}
