package com.example;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.example.dto.Item;
import com.example.dto.OrderPlaced;
import com.example.dto.OrderStatus;
import com.example.dto.PlaceOrder;
import com.typesafe.config.ConfigFactory;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class FulFillOrder extends AbstractBehavior<FulFillOrder.Command> {

    private ActorRef<Delivery.Command> deliveryRef;
    private PlaceOrder placeOrder;
    private Integer orderId;
    private String orderStatus;
    private Item item;


    public static final String WALLET_SERVICE = ConfigFactory.load().getConfig("my-app.wallet-server")
            .getString("address");
    public static final String RESTAURANT_SERVICE = ConfigFactory.load().getConfig("my-app.restaurant-server")
            .getString("address");

    // parent Command Interface
    interface Command {
    }

    /**
     * GetOrderResponse : FullfillOrder Actor sends this command to Controller
     */
    public final static class GetOrderResponse {
        final OrderPlaced order;

        public GetOrderResponse(OrderPlaced order) {
            this.order = order;
        }
    }

    /**
     * GetOrderCmd : Delivery Actor sends this command to FullfillOrder Actor to
     * return the order to controller.
     */
    public final static class GetOrderCmd implements Command {
        final ActorRef<GetOrderResponse> replyTo;

        public GetOrderCmd(ActorRef<GetOrderResponse> replyTo) {
            this.replyTo = replyTo;
        }
    }


    /**
     * OrderIsDelivered : To be implemented
     */
    public static class OrderIsDelivered implements Command {
        // public final ActorRef<FulFillOrder.GetOrderDeliveredResp> replyTo;
        // OrderIsDelivered ( ActorRef<FulFillOrder.GetOrderDeliveredResp> replyTo )
        // {
        // this.replyTo = replyTo;
        // }
    }

    public FulFillOrder(ActorContext<Command> context, PlaceOrder placeOrder,
            Integer orderId,  ActorRef<Delivery.Command> deliveryRef) {

        super(context);
        // getContext().getLog().info("Creating Order with order Id {}", orderId);

        this.placeOrder = placeOrder;
        this.orderId = orderId;

        //  firstly setting its order status to unassigned
        this.orderStatus = OrderStatus.unassigned;
        this.item = null;                                           
        this.deliveryRef = deliveryRef;

        // http post request to restaurant service to place order
        try {
            URL url = new URL(RESTAURANT_SERVICE + "/acceptOrder");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // post request
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            String input = "{\"restId\": " + this.placeOrder.getRestId() + ", \"itemId\": "
                    + this.placeOrder.getItemId()
                    + ", \"qty\": " + this.placeOrder.getQty() + "}";

            // send request
            conn.getOutputStream().write(input.getBytes());
            conn.getOutputStream().flush();

            // get response code and response json object
            if (conn.getResponseCode() == 201) {
                InputStream inputStream = conn.getInputStream();
                StringBuffer response = new StringBuffer();
                int i;
                while ((i = inputStream.read()) != -1) {
                    response.append((char) i);
                }

                // parsing json object {"restId": num, "itemId": x, "qty": y}
                String[] tokens = response.toString().split(",");
                String[] restId = tokens[0].split(":");
                String[] itemId = tokens[1].split(":");
                String[] qty = tokens[2].split(":");

                // remove trailing } from qty
                String[] qtyTokens = qty[1].split("}");

                this.item = new Item(Integer.parseInt(restId[1].trim()), Integer.parseInt(itemId[1].trim()),
                        Integer.parseInt(qtyTokens[0].trim()));
                // getContext().getLog().info("restaurant service response: {}", response.toString());
            } else {
                // getContext().getLog().info("Order not placed");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // if item is not null then send request to wallet service to deduct amount
        if (this.item != null) {
            try {
                URL url = new URL(WALLET_SERVICE + "/deductBalance");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                // post request
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

                String input = "{\"custId\": " + this.placeOrder.getCustId() + ", \"amount\": "
                        + this.placeOrder.getQty() * item.getPrice() + "}";

                // send request
                conn.getOutputStream().write(input.getBytes());
                conn.getOutputStream().flush();

                // get response code and response json object
                if (conn.getResponseCode() == 201) {
                    // getContext().getLog().info("Order placed");
                    this.orderStatus = OrderStatus.delivered;
                    // this.deliveryRef.tell(new Delivery.MarkFulFillOrder(this.orderId, getContext().getSelf()));
                } else {

                    // getContext().getLog().info("Order not placed");
                    // revert the previous transaction on the restaurant service
                    try {

                        url = new URL(RESTAURANT_SERVICE + "/refillItem");
                        conn = (HttpURLConnection) url.openConnection();

                        // post request
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "application/json");
                        conn.setRequestProperty("Accept", "application/json");
                        conn.setDoOutput(true);

                        input = "{\"restId\": " + this.placeOrder.getRestId() + ", \"itemId\": "
                                + this.placeOrder.getItemId()
                                + ", \"qty\": " + this.placeOrder.getQty() + "}";

                        // send request
                        conn.getOutputStream().write(input.getBytes());
                        conn.getOutputStream().flush();

                        // get response code and response json object
                        if (conn.getResponseCode() == 201) {
                            // getContext().getLog().info("order items restored");

                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // getContext().getLog().info("Order created with order Id {}, order status {}", orderId, orderStatus);
    }

    public static Behavior<Command> create(PlaceOrder placeOrder, Integer orderId,
            ActorRef<Delivery.Command> deliveryRef) {
        return Behaviors.setup(context -> new FulFillOrder(context, placeOrder, orderId, deliveryRef));
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(GetOrderCmd.class, this::onGetOrderCmd)
                .build();
    }



    private Behavior<Command> onGetOrderCmd(GetOrderCmd getOrderCmd) {
        // getContext().getLog().info("FulFillOrder : Sending order details with id {} to {}", orderId,
        //        getOrderCmd.replyTo);
        getOrderCmd.replyTo.tell(new GetOrderResponse(new OrderPlaced(this.orderId, this.orderStatus)));
        return Behaviors.same();
    }
}
