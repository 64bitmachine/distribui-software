package com.rathod.wallet;

import java.util.ArrayList;

public class ReadDB {
    

    // database text file location
    private static final String DB_FILE = "src/main/resources/initialData.txt";

    // read the text file and return the list of customers
    public static ArrayList<Customer> readDB() {
        ArrayList<Customer> customers = new ArrayList<>();
        try {
            // read the text file
            String line;
            java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(DB_FILE));
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
