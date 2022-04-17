package com.rathod.wallet.service;

import java.util.ArrayList;

import org.springframework.stereotype.Service;

import com.rathod.wallet.entity.Customer;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class WalletService {

	private final ReadDB readDB;
    private ArrayList<Customer> customers = new ArrayList<>();
    
    
    public synchronized boolean withdrawBalance(Customer customer) {
        Customer c = getCustomer(customer.getCustId());
        if (c == null || c.getAmount() < customer.getAmount()) {
        	
        	return false;
            //return new ResponseEntity<Customer>(HttpStatus.GONE);
        }

        // invert the amount
        customer.setAmount(-customer.getAmount());
        updateBalance(customer);
        return true;
    }
	/**
     * update the balance of the customer
     * @param customer
     */
    public synchronized void updateBalance(Customer customer) {
        for (int i = 0; i < customers.size(); i++) {
            if (customers.get(i).getCustId() == customer.getCustId()) {
                customers.get(i).setAmount(customers.get(i).getAmount() + customer.getAmount());
                System.out.println(customers.get(i).getAmount());
            }
        }
    }

    /**
     * 
     * @param id
     * @return customer object if found else null
     */
    public synchronized Customer getCustomer(int id)
    {
        for (int i = 0; i < customers.size(); i++) {
            if (customers.get(i).getCustId() == id) {
                return customers.get(i);
            }
        }
        return null;
    }
    
    /**
     * @param null
     * @return customer list
     */
    
    public synchronized void getCustomerList()
    {
    	customers = readDB.readCustomerIDFromFile();
    	for(Customer customer : customers) {
    		System.out.println(customer.getCustId());
    	}
    }
}

