package com.rathod.wallet.service;

import com.rathod.wallet.CONSTANTS;
import com.rathod.wallet.entity.Customer;

import java.util.ArrayList;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ReadDB {
    

    // database text file location
    private final String DB_FILE;
    public ReadDB()
    {
    	DB_FILE = CONSTANTS.INIT_REC_FILE_PATH;
    }
    // read the text file and return the list of customers
        ArrayList<Customer> customers = new ArrayList<>();
        public ArrayList<Customer> readCustomerIDFromFile() {
        try {
            // read the text file
            String line;
            // use resource class to read the file
            Resource resource = new ClassPathResource(DB_FILE);
            // read the file
            java.io.BufferedReader br = new java.io.BufferedReader(
                    new java.io.InputStreamReader(resource.getInputStream()));
            int starCount  = 0;
            while ((line = br.readLine()) != null) {
                
                if (line.equals("****")) {
                    starCount++;
                    continue;
                }

                if (starCount == 2) {
                    Customer customer = new Customer(Integer.parseInt(line), 0);
                    customers.add(customer);
                }

                if (starCount == 3) {
                    Double amount = Double.parseDouble(line);
                    customers.stream().forEach(c -> c.setAmount(amount));
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return customers;
    }

}
