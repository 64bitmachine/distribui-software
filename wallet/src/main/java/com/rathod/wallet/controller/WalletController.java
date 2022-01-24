package com.rathod.wallet.controller;

import java.util.ArrayList;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.rathod.wallet.dto.Customer;
import com.rathod.wallet.service.ReadDB;
import com.rathod.wallet.service.WalletService;

import lombok.AllArgsConstructor;

/**
 * @author rathod
 * This class handle wallet operations like deposit, withdraw
 */
@RestController
@AllArgsConstructor
public class WalletController {
	

	private final WalletService walletService;
	private final ReadDB readDB;
	
    @RequestMapping(value = "/addBalance", method = RequestMethod.POST)
    public ResponseEntity<Customer> deposit(@RequestBody Customer customer) {
        walletService.updateBalance(customer);
        return new ResponseEntity<Customer>(HttpStatus.CREATED);
    }

    @RequestMapping(value = "/deductBalance", method = RequestMethod.POST)
    public ResponseEntity<Customer> withdraw(@RequestBody Customer customer) { 
    	
    	
    	boolean withdrawComplete = walletService.withdrawBalance(customer);
    	
        return withdrawComplete ? new ResponseEntity<Customer>(HttpStatus.CREATED) : 
        	new  ResponseEntity<Customer>(HttpStatus.GONE);
    }
    /**
     * quering the balance of a customer    
     * @param num
     * @return the customer with the given number
     */
    @RequestMapping(value = "/balance/{customerId}", method = RequestMethod.GET)
    public ResponseEntity<Customer> balance(@PathVariable("customerId") int customerId) {
        Customer customer = walletService.getCustomer(customerId);

        if (customer == null) {
            return new ResponseEntity<Customer>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<Customer>(customer, HttpStatus.OK);
    }

    @RequestMapping(value = "/reInitialize", method = RequestMethod.POST)
    public ResponseEntity<String> init() {

       // TODO: logic for reading from text file and initializing the customers
        walletService.getCustomerList();
        return new ResponseEntity<String>("User Db is updated",HttpStatus.CREATED);
    }

    
}