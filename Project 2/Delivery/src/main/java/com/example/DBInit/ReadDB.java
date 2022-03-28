
package com.example.DBInit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import com.example.dto.DeliveryAgent;
import com.example.dto.DeliveryAgentStatus;

/**
 * this class reads the data from initialData.txt
 * and initializes the agents
 */
public class ReadDB {

    private final String DB_FILE;

    public ReadDB(String path) {
        DB_FILE = path;
    }

    public ArrayList<DeliveryAgent> readDeliveryAgentIDFromFile() {
        ArrayList<DeliveryAgent> deliveryAgents = new ArrayList<>();
        try {
            // read the text file
            String line;
            
            // read the file from resources
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
            System.exit(1);
        }
        return deliveryAgents;
    }

}