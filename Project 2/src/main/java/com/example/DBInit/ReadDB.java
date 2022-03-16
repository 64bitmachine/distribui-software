
package com.example.DBInit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import com.example.dto.DeliveryAgent;
import com.example.dto.DeliveryAgentStatus;

public class ReadDB {

    private final String DB_FILE;

    public ReadDB() {
        DB_FILE = "Project 2/src/main/java/com/example/DBInit/initialData.txt";
    }

    public ArrayList<DeliveryAgent> readDeliveryAgentIDFromFile() {
        ArrayList<DeliveryAgent> deliveryAgents = new ArrayList<>();
        try {
            // read the text file
            String line;
            
            // read the file from filesystem and store it in a string
            File file = new File(DB_FILE);
            BufferedReader br = new BufferedReader(new FileReader(file));

            int starCount = 0;
            while ((line = br.readLine()) != null) {

                if (line.equals("****")) {
                    starCount++;
                    if (starCount == 2) {
                        break;
                    }
                    continue;
                }

                if (starCount == 1) {
                    DeliveryAgent agent = new DeliveryAgent();
                    agent.setAgentId(Integer.parseInt(line));
                    agent.setStatus(DeliveryAgentStatus.signed_out);
                    deliveryAgents.add(agent);
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return deliveryAgents;
    }

}