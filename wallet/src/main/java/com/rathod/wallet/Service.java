package com.rathod.wallet;

import java.util.ArrayList;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author rathod
 * This class handle wallet operations like deposit, withdraw
 */
@RestController
public class Service {

    private ArrayList<Customer> customers = new ArrayList<>();

    @RequestMapping(value = "/addBalance", method = RequestMethod.POST)
    public ResponseEntity<Customer> deposit(@RequestBody Customer customer) {
        updateBalance(customer);
        return new ResponseEntity<Customer>(HttpStatus.CREATED);
    }

    @RequestMapping(value = "/deductBalance", method = RequestMethod.POST)
    public ResponseEntity<Customer> withdraw(@RequestBody Customer customer) {
        
        Customer c = getCustomer(customer.getId());
        if (c.getAmount() < customer.getAmount()) {
            return new ResponseEntity<Customer>(HttpStatus.GONE);
        }

        // invert the amount
        customer.setAmount(-customer.getAmount());
        updateBalance(customer);

        return new ResponseEntity<Customer>(HttpStatus.CREATED);
    }

    /**
     * quering the balance of a customer    
     * @param num
     * @return the customer with the given number
     */
    @RequestMapping(value = "/balance/{num}", method = RequestMethod.GET)
    public ResponseEntity<Customer> balance(@PathVariable("num") int num) {
        Customer customer = getCustomer(num);

        if (customer == null) {
            return new ResponseEntity<Customer>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<Customer>(customer, HttpStatus.OK);
    }

    @RequestMapping(value = "/reInitialize", method = RequestMethod.POST)
    public ResponseEntity<String> init() {

        // TODO: logic for reading from text file and initializing the customers
        
        return new ResponseEntity<String>(HttpStatus.CREATED);
    }

    /**
     * update the balance of the customer
     * @param customer
     */
    public void updateBalance(Customer customer) {
        for (int i = 0; i < customers.size(); i++) {
            if (customers.get(i).getId() == customer.getId()) {
                customers.get(i).setAmount(customers.get(i).getAmount() + customer.getAmount());
            }
        }
    }

    /**
     * 
     * @param id
     * @return customer object if found else null
     */
    public Customer getCustomer(int id) {
        for (int i = 0; i < customers.size(); i++) {
            if (customers.get(i).getId() == id) {
                return customers.get(i);
            }
        }
        return null;
    }
}