package com.rathod.delivery.service;

import com.rathod.delivery.CONSTANTS;
import com.rathod.delivery.entity.DeliveryAgent;
import com.rathod.delivery.entity.DeliveryAgentStatus;

import java.util.ArrayList;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ReadDB {

    private final String DB_FILE;

    public ReadDB() {
        DB_FILE = CONSTANTS.INIT_REC_FILE_PATH;
    }

    public ArrayList<DeliveryAgent> readDeliveryAgentIDFromFile() {
        ArrayList<DeliveryAgent> deliveryAgents = new ArrayList<>();
        try {
            // read the text file
            String line;
            java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(DB_FILE));
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
                    DeliveryAgent agent = new DeliveryAgent(Integer.parseInt(line), DeliveryAgentStatus.SIGNED_OUT);
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